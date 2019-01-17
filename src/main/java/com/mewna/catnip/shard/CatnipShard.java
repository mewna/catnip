/*
 * Copyright (c) 2018 amy, All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.mewna.catnip.shard;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.impl.PresenceImpl;
import com.mewna.catnip.entity.user.Presence;
import com.mewna.catnip.extension.Extension;
import com.mewna.catnip.extension.hook.CatnipHook;
import com.mewna.catnip.shard.LifecycleEvent.Raw;
import com.mewna.catnip.shard.manager.AbstractShardManager;
import com.mewna.catnip.shard.manager.DefaultShardManager;
import com.mewna.catnip.util.BufferOutputStream;
import com.mewna.catnip.util.JsonUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebSocketFrame;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

import static com.mewna.catnip.shard.CatnipShard.ShardConnectState.*;
import static com.mewna.catnip.shard.ShardAddress.*;

/**
 * A catnip shard encapsulates a single websocket connection to Discord's
 * real-time gateway. Shards are implemented as vert.x verticles and should be
 * un/deployed as such; see {@link DefaultShardManager} and
 * {@link AbstractShardManager} for more.
 * <p/>
 * Shards are controlled by sending messages over the vert.x event bus; it is
 * NOT recommended that you store a reference to a shard verticle anywhere. To
 * send a message to a shard:
 * <ol>
 *     <li>Get the shard's control address with {@code ShardAddress.computeAddress(ShardAddress.CONTROL, id)}.</li>
 *     <li>Send a {@link ShardControlMessage} to it.</li>
 * </ol>
 *
 * @author amy
 * @since 8/31/18.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class CatnipShard extends AbstractVerticle {
    public static final int ZLIB_SUFFIX = 0x0000FFFF;
    public static final int LARGE_THRESHOLD = 250;
    
    private final Catnip catnip;
    private final int id;
    private final int limit;
    private final Presence presence;
    
    private final HttpClient client;
    
    private final byte[] decompressBuffer = new byte[1024];
    private final Deque<JsonObject> messageQueue = new ArrayDeque<>();
    private final Deque<PresenceImpl> presenceQueue = new ArrayDeque<>();
    
    private volatile ShardState state;
    private volatile Presence currentPresence;
    private volatile boolean heartbeatAcked = true;
    private volatile long lastHeartbeat = -1; //use System.nanoTime() as that is monotonic
    private volatile long lastHeartbeatLatency = -1;
    private volatile boolean presenceRateLimitRecheckQueued;
    private volatile boolean sendRateLimitRecheckQueued;
    private volatile List<String> trace = Collections.emptyList();
    private volatile boolean clientClose;
    private volatile long heartbeatTask;
    
    public CatnipShard(@Nonnull final Catnip catnip, @Nonnegative final int id, @Nonnegative final int limit,
                       @Nullable final Presence presence) {
        this.catnip = catnip;
        this.id = id;
        this.limit = limit;
        this.presence = presence;
        
        client = catnip.vertx().createHttpClient(new HttpClientOptions()
                .setMaxWebsocketFrameSize(Integer.MAX_VALUE)
                .setMaxWebsocketMessageSize(Integer.MAX_VALUE));
    }
    
    public static JsonObject basePayload(@Nonnull final GatewayOp op) {
        return basePayload(op, (JsonObject) null);
    }
    
    public static JsonObject basePayload(@Nonnull final GatewayOp op, @Nullable final JsonObject payload) {
        return new JsonObject()
                .put("op", op.opcode())
                .put("d", payload)
                ;
    }
    
    public static JsonObject basePayload(@Nonnull final GatewayOp op, @Nonnull @Nonnegative final Integer payload) {
        return new JsonObject()
                .put("op", op.opcode())
                .put("d", payload)
                ;
    }
    
    @Override
    public void start() {
        catnip.eventBus().consumer(computeAddress(CONTROL, id), this::handleControlMessage);
        catnip.eventBus().consumer(computeAddress(WEBSOCKET_SEND, id), this::handleSocketSend);
        catnip.eventBus().consumer(computeAddress(WEBSOCKET_QUEUE, id), this::handleSocketQueue);
        catnip.eventBus().consumer(computeAddress(PRESENCE_UPDATE_QUEUE, id), this::handlePresenceUpdateQueue);
        catnip.eventBus().consumer(computeAddress(PRESENCE_UPDATE_REQUEST, id), this::handlePresenceUpdate);
        catnip.eventBus().consumer(computeAddress(PRESENCE_UPDATE_POLL, id), presence -> {
            if(state != null) {
                while(!presenceQueue.isEmpty()) {
                    if(catnip.gatewayRatelimiter().checkRatelimit(computeAddress(PRESENCE_UPDATE_REQUEST, id), 60_000L, 5).left) {
                        if(!presenceRateLimitRecheckQueued) {
                            presenceRateLimitRecheckQueued = true;
                            catnip.vertx().setTimer(1000, __ -> catnip.eventBus().publish(computeAddress(PRESENCE_UPDATE_POLL, id), null));
                        }
                        break;
                    }
                    presenceRateLimitRecheckQueued = false;
                    final PresenceImpl update = presenceQueue.pop();
                    final JsonObject object = basePayload(GatewayOp.STATUS_UPDATE, update.asJson());
                    catnip.eventBus().publish(computeAddress(WEBSOCKET_SEND, id), object);
                    currentPresence = update;
                }
            }
        });
        catnip.eventBus().<JsonObject>consumer(computeAddress(VOICE_STATE_UPDATE_QUEUE, id),
                state -> catnip.eventBus().send(computeAddress(WEBSOCKET_QUEUE, id), basePayload(
                        GatewayOp.VOICE_STATE_UPDATE,
                        state.body()
                )));
        catnip.eventBus().consumer(computeAddress(WEBSOCKET_POLL, id), msg -> {
            if(state != null) {
                while(!messageQueue.isEmpty()) {
                    // We only do up to 110 messages/min, to allow for room just in case
                    final ImmutablePair<Boolean, Long> check = catnip.gatewayRatelimiter()
                            .checkRatelimit("catnip:gateway:" + id + ":outgoing-send", 60_000L, 110);
                    if(check.left) {
                        // We got ratelimited, stop sending and try again in 1s
                        if(!sendRateLimitRecheckQueued) {
                            sendRateLimitRecheckQueued = true;
                            catnip.vertx().setTimer(1000, __ -> catnip.eventBus().publish(computeAddress(WEBSOCKET_POLL, id), null));
                        }
                        break;
                    }
                    sendRateLimitRecheckQueued = false;
                    final JsonObject payload = messageQueue.pop();
                    catnip.eventBus().publish(computeAddress(WEBSOCKET_SEND, id), payload);
                }
            }
        });
    }
    
    @Override
    public void stop() {
    }
    
    private void handlePresenceUpdate(final Message<PresenceImpl> message) {
        final PresenceImpl impl = message.body();
        if(impl == null) {
            message.reply(currentPresence);
            return;
        }
        catnip.eventBus().publish(computeAddress(PRESENCE_UPDATE_QUEUE, id), impl);
    }
    
    private void handleControlMessage(final Message<ShardControlMessage> msg) {
        final ShardControlMessage body = msg.body();
        switch(body) {
            case START: {
                doStart(msg);
                break;
            }
            case STOP: {
                shutdownShard();
                break;
            }
            case SHUTDOWN: {
                shutdownShard();
                catnip.vertx().undeploy(deploymentID());
                break;
            }
            case TRACE: {
                msg.reply(new JsonArray(trace));
                break;
            }
            case CONNECTED: {
                msg.reply(state != null && state.socketOpen());
                break;
            }
            case LATENCY: {
                msg.reply(lastHeartbeatLatency);
                break;
            }
            default: {
                catnip.logAdapter().warn("Shard {}/{}: Got unknown control message: {}", id, limit, body.name());
                break;
            }
        }
    }
    
    private void doStart(final Message<ShardControlMessage> msg) {
        connectSocket(msg);
    }
    
    private void shutdownShard() {
        if(state != null) {
            clientClose = true;
            state.socket().close((short) 4000);
        }
        messageQueue.clear();
        presenceQueue.clear();
        heartbeatAcked = true;
    }
    
    private void connectSocket(final Message<ShardControlMessage> msg) {
        catnip.eventBus().publish(Raw.CONNECTING, shardInfo());
        
        //noinspection ConstantConditions
        client.websocketAbs(catnip.gatewayInfo().url(), null, null, null,
                socket -> {
                    catnip.eventBus().publish(Raw.CONNECTED, shardInfo());
                    socket.frameHandler(frame -> handleSocketFrame(msg, frame))
                            .closeHandler(this::handleSocketClose)
                            .exceptionHandler(t -> catnip.logAdapter().error("Shard {}/{}: Exception in Websocket", id, limit, t));
                    state = new ShardState(socket);
                    state.socketOpen(true);
                },
                failure -> {
                    state = null;
                    catnip.logAdapter().error("Shard {}/{}: Couldn't connect socket:", id, limit, failure);
                    // If we totally fail to connect socket, don't need to worry as much
                    catnip.vertx().setTimer(500L, __ -> msg.reply(FAILED));
                });
    }
    
    private void handleBinaryData(final Message<ShardControlMessage> msg, final Buffer binary) {
        final ShardState state = this.state;
        if(state == null) {
            return;
        }
        final boolean isEnd = binary.getInt(binary.length() - 4) == ZLIB_SUFFIX;
        if(!isEnd || state.readBufferPosition() > 0) {
            final int position = state.readBufferPosition();
            state.readBuffer().setBuffer(position, binary);
            state.readBufferPosition(position + binary.length());
        }
        if(isEnd) {
            final Buffer decompressed = state.decompressBuffer();
            final Buffer dataToDecompress = state.readBufferPosition() > 0 ? state.readBuffer() : binary;
            try(final InflaterOutputStream ios = new InflaterOutputStream(new BufferOutputStream(decompressed, 0), state.inflater())) {
                synchronized(decompressBuffer) {
                    final int length = Math.max(state.readBufferPosition(), binary.length());
                    int r = 0;
                    while(r < length) {
                        //how many bytes we can read
                        final int read = Math.min(decompressBuffer.length, length - r);
                        dataToDecompress.getBytes(r, r + read, decompressBuffer);
                        //decompress
                        ios.write(decompressBuffer, 0, read);
                        r += read;
                    }
                }
                handleSocketData(msg, decompressed.toJsonObject());
            } catch(final IOException e) {
                catnip.logAdapter().error("Shard {}/{}: Error decompressing payload", id, limit, e);
            } finally {
                state.readBufferPosition(0);
            }
        }
    }
    
    private void handleSocketFrame(final Message<ShardControlMessage> msg, final WebSocketFrame frame) {
        try {
            if(frame.isText()) {
                handleSocketData(msg, new JsonObject(frame.textData()));
            }
            if(frame.isBinary()) {
                handleBinaryData(msg, frame.binaryData());
            }
            if(frame.isClose()) {
                final short closeCode = frame.closeStatusCode();
                if(closeCode == GatewayCloseCode.INVALID_SEQ.code() || closeCode == GatewayCloseCode.SESSION_TIMEOUT.code()) {
                    // These two close codes invalidate your session (and afaik do not send an OP9).
                    catnip.sessionManager().clearSeqnum(id);
                    catnip.sessionManager().clearSession(id);
                }
                if(clientClose) {
                    catnip.logAdapter().info("Shard {}/{}: We closed the websocket with code {}", id, limit, closeCode);
                } else {
                    if(closeCode >= 4000) {
                        final GatewayCloseCode code = GatewayCloseCode.byId(closeCode);
                        if(code != null) {
                            catnip.logAdapter().warn("Shard {}/{}: gateway websocket closed with code {}: {}: {}",
                                    id, limit, closeCode, code.name(), code.message());
                        } else {
                            catnip.logAdapter().warn("Shard {}/{}: gateway websocket closing with code {}: {}",
                                    id, limit, closeCode, frame.closeReason());
                        }
                    } else {
                        catnip.logAdapter().warn("Shard {}/{}: gateway websocket closing with code {}: {}",
                                id, limit, closeCode, frame.closeReason());
                    }
                }
                state.socketOpen(false);
            }
        } catch(final Exception e) {
            catnip.logAdapter().error("Shard {}/{}: Failed to handle socket frame", id, limit, e);
        }
    }
    
    private void handleSocketData(final Message<ShardControlMessage> msg, JsonObject payload) {
        for(final Extension extension : catnip.extensionManager().extensions()) {
            for(final CatnipHook hook : extension.hooks()) {
                payload = hook.rawGatewayReceiveHook(payload);
            }
        }
        
        final GatewayOp op = GatewayOp.byId(payload.getInteger("op"));
        // We pass `msg` for consistency (and for the off-chance it's
        // needed), but REALLY you don't wanna do anything with it. It
        // gets passed *entirely* so that we can reply to the shard
        // manager directly.
        switch(op) {
            case HELLO: {
                handleHello(msg, payload);
                break;
            }
            case DISPATCH: {
                handleDispatch(msg, payload);
                break;
            }
            case HEARTBEAT: {
                handleHeartbeat(msg, payload);
                break;
            }
            case HEARTBEAT_ACK: {
                handleHeartbeatAck(msg, payload);
                break;
            }
            case INVALID_SESSION: {
                handleInvalidSession(msg, payload);
                break;
            }
            case RECONNECT: {
                handleReconnectRequest(msg, payload);
                break;
            }
            default: {
                break;
            }
        }
    }
    
    private void handleSocketClose(final Void __) {
        vertx.cancelTimer(heartbeatTask);
        catnip.eventBus().publish(Raw.DISCONNECTED, shardInfo());
        catnip.logAdapter().warn("Shard {}/{}: Socket closing!", id, limit);
        try {
            state = null;
            requeue();
        } catch(final Exception e) {
            catnip.logAdapter().error("Shard {}/{}: Failure closing socket:", id, limit, e);
        }
    }
    
    private void requeue() {
        catnip.shardManager().addToConnectQueue(id);
    }
    
    private void handleSocketQueue(final Message<JsonObject> msg) {
        messageQueue.addLast(msg.body());
        catnip.eventBus().publish(computeAddress(WEBSOCKET_POLL, id), null);
    }
    
    private void handlePresenceUpdateQueue(final Message<PresenceImpl> msg) {
        presenceQueue.addLast(msg.body());
        catnip.eventBus().publish(computeAddress(PRESENCE_UPDATE_POLL, id), null);
    }
    
    private void handleSocketSend(final Message<JsonObject> msg) {
        if(state != null && state.socket() != null && state.socketOpen()) {
            JsonObject payload = msg.body();
            for(final Extension extension : catnip.extensionManager().extensions()) {
                for(final CatnipHook hook : extension.hooks()) {
                    payload = hook.rawGatewaySendHook(payload);
                }
            }
            state.socket().writeTextMessage(payload.encode());
        }
    }
    
    private void handleHello(final Message<ShardControlMessage> msg, final JsonObject event) {
        final JsonObject payload = event.getJsonObject("d");
        trace = JsonUtil.toStringList(payload.getJsonArray("_trace"));
        
        catnip.vertx().setPeriodic(payload.getInteger("heartbeat_interval"), timerId -> {
            heartbeatTask = timerId;
            
            final ShardState shardState = state;
            if(shardState != null && shardState.socket() != null && shardState.socketOpen()) {
                if(!heartbeatAcked) {
                    // Zombie
                    catnip.logAdapter().warn("Shard {}/{}: Heartbeat zombie, queueing reconnect!", id, limit);
                    catnip.vertx().cancelTimer(heartbeatTask);
                    catnip.eventBus().publish(computeAddress(CONTROL, id), ShardControlMessage.STOP);
                    return;
                }
                catnip.eventBus().publish(computeAddress(WEBSOCKET_SEND, id),
                        basePayload(GatewayOp.HEARTBEAT, catnip.sessionManager().seqnum(id)));
                lastHeartbeat = System.nanoTime();
                heartbeatAcked = false;
            } else {
                catnip.vertx().cancelTimer(heartbeatTask);
            }
        });
        
        // Check if we can RESUME instead
        if(catnip.sessionManager().session(id) != null && catnip.sessionManager().seqnum(id) > 0) {
            catnip.eventBus().publish(computeAddress(WEBSOCKET_SEND, id), resume());
        } else {
            catnip.eventBus().publish(computeAddress(WEBSOCKET_SEND, id), identify());
        }
    }
    
    private void handleDispatch(final Message<ShardControlMessage> msg, final JsonObject event) {
        // Should be safe to ignore
        if(event.getValue("d") instanceof JsonArray) {
            return;
        }
        final JsonObject data = event.getJsonObject("d");
        final String type = event.getString("t");
        
        // Update trace and seqnum as needed
        if(data.getJsonArray("_trace", null) != null) {
            trace = JsonUtil.toStringList(data.getJsonArray("_trace"));
        } else {
            trace = Collections.emptyList(); //remove any old value
        }
        
        if(event.getValue("s", null) != null) {
            catnip.sessionManager().seqnum(id, event.getInteger("s"));
        }
        
        switch(type) {
            case "READY": {
                catnip.sessionManager().session(id, data.getString("session_id"));
                // Reply after IDENTIFY ratelimit
                msg.reply(READY);
                catnip.eventBus().publish(Raw.IDENTIFIED, shardInfo());
                break;
            }
            case "RESUMED": {
                // RESUME is fine, just reply immediately
                msg.reply(RESUMED);
                catnip.eventBus().publish(Raw.RESUMED, shardInfo());
                break;
            }
            default: {
                break;
            }
        }
        
        // This allows a buffer to know WHERE an event is coming from, so that
        // it can be accurate in the case of ex. buffering events until a shard
        // has finished booting.
        event.put("shard", new JsonObject().put("id", id).put("limit", limit));
        catnip.eventBuffer().buffer(event);
    }
    
    private void handleHeartbeat(final Message<ShardControlMessage> msg, final JsonObject event) {
        //heartbeatAcked.set(false);
        catnip.eventBus().publish(computeAddress(WEBSOCKET_SEND, id),
                basePayload(GatewayOp.HEARTBEAT, catnip.sessionManager().seqnum(id)));
    }
    
    private void handleHeartbeatAck(final Message<ShardControlMessage> msg, final JsonObject event) {
        heartbeatAcked = true;
        lastHeartbeatLatency = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - lastHeartbeat);
    }
    
    private void handleInvalidSession(final Message<ShardControlMessage> msg, final JsonObject event) {
        if(event.getBoolean("d")) {
            // Can resume
            if(state != null) {
                clientClose = true;
                state.socket().close();
            } else {
                // Realistically this shouldn't ever be called, but this is more of a
                // "for my own sanity" thing than anything.
                requeue();
            }
        } else {
            catnip.logAdapter().info("Session invalidated (OP 9), clearing shard data and reconnecting");
            // Can't resume, clear old data
            if(state != null) {
                clientClose = true;
                state.socket().close();
                catnip.cacheWorker().invalidateShard(id);
                catnip.sessionManager().clearSession(id);
                catnip.sessionManager().clearSeqnum(id);
            } else {
                // Realistically this shouldn't ever be called, but this is more of a
                // "for my own sanity" thing than anything.
                requeue();
            }
        }
    }
    
    private void handleReconnectRequest(final Message<ShardControlMessage> msg, final JsonObject event) {
        // Just immediately disconnect
        if(state != null) {
            clientClose = true;
            state.socket().close();
        }
    }
    
    private JsonObject identify() {
        final JsonObject data = new JsonObject()
                .put("token", catnip.token())
                .put("compress", false)
                .put("large_threshold", LARGE_THRESHOLD)
                .put("shard", new JsonArray().add(id).add(limit))
                .put("properties", new JsonObject()
                        .put("$os", "JVM")
                        .put("$browser", "catnip")
                        .put("$device", "catnip")
                );
        if(presence != null) {
            data.put("presence", ((PresenceImpl) presence).asJson());
        }
        return basePayload(GatewayOp.IDENTIFY, data);
    }
    
    private JsonObject resume() {
        return basePayload(GatewayOp.RESUME, new JsonObject()
                .put("token", catnip.token())
                .put("compress", false)
                .put("session_id", catnip.sessionManager().session(id))
                .put("seq", catnip.sessionManager().seqnum(id))
                .put("properties", new JsonObject()
                        .put("$os", "JVM")
                        .put("$browser", "catnip")
                        .put("$device", "catnip")
                )
        );
    }
    
    private ShardInfo shardInfo() {
        return new ShardInfo(id, limit);
    }
    
    public enum ShardConnectState {
        FAILED,
        READY,
        RESUMED,
    }
    
    @Accessors(fluent = true)
    private static final class ShardState {
        @Getter
        private final WebSocket socket;
        @Getter
        private final Inflater inflater;
        @Getter
        private final Buffer readBuffer = Buffer.buffer();
        @Getter
        private final Buffer decompressBuffer = Buffer.buffer();
        @Getter
        @Setter
        private volatile boolean socketOpen;
        @Getter
        @Setter
        private int readBufferPosition;
        
        ShardState(final WebSocket socket) {
            this(socket, new Inflater());
        }
        
        ShardState(final WebSocket socket, final Inflater inflater) {
            this.socket = socket;
            this.inflater = inflater;
        }
    }
}
