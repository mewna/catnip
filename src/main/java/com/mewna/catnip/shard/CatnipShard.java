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
import com.mewna.catnip.entity.misc.GatewayInfo;
import com.mewna.catnip.entity.user.Presence;
import com.mewna.catnip.extension.Extension;
import com.mewna.catnip.extension.hook.CatnipHook;
import com.mewna.catnip.shard.LifecycleEvent.Raw;
import com.mewna.catnip.shard.manager.AbstractShardManager;
import com.mewna.catnip.shard.manager.DefaultShardManager;
import com.mewna.catnip.util.BufferOutputStream;
import com.mewna.catnip.util.JsonUtil;
import com.mewna.catnip.util.task.GatewayTask;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

import static com.mewna.catnip.shard.LifecycleState.*;
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
 * <li>Get the shard's control address with {@code ShardAddress.computeAddress(ShardAddress.CONTROL, id)}.</li>
 * <li>Send a {@link ShardControlMessage} to it.</li>
 * </ol>
 *
 * @author amy
 * @since 8/31/18.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class CatnipShard extends AbstractVerticle implements Listener {
    public static final int ZLIB_SUFFIX = 0x0000FFFF;
    public static final int LARGE_THRESHOLD = 250;
    
    private final Catnip catnip;
    private final int id;
    private final int limit;
    private final Presence presence;
    
    private final HttpClient client;
    
    private final Collection<MessageConsumer> consumers = new HashSet<>();
    
    // This is an AtomicLong instead of a volatile long because IntelliJ got
    // A N G E R Y because I guess longs don't get written atomically.
    private final AtomicLong heartbeatTask = new AtomicLong(-1L);
    private final Buffer readBuffer = Buffer.buffer();
    private final Buffer decompressBuffer = Buffer.buffer();
    private final StringBuffer socketInputBuffer = new StringBuffer(); //Using a StringBuffer instead of a StringBuilder due to async-friendly synchronizations.
    private final byte[] decompress = new byte[1024];
    // aka memory golfing
    private final String control;
    private final String websocketQueue;
    private final String websocketSend;
    private final String presenceUpdateRequest;
    private final String voiceStateUpdateQueue;
    private final GatewayTask<JsonObject> sendTask;
    private final GatewayTask<PresenceImpl> presenceTask;
    private volatile Presence currentPresence;
    private volatile boolean heartbeatAcked = true;
    private volatile long lastHeartbeat = -1; //use System.nanoTime() as that is monotonic
    private volatile long lastHeartbeatLatency = -1;
    private volatile boolean presenceRateLimitRecheckQueued;
    private volatile boolean sendRateLimitRecheckQueued;
    private volatile List<String> trace = Collections.emptyList();
    private volatile boolean connected;
    private volatile boolean socketOpen;
    private volatile boolean closedByClient;
    private WebSocket socket;
    private Inflater inflater;
    private int readBufferPosition;
    private Message<ShardControlMessage> message;
    private LifecycleState lifecycleState;
    
    public CatnipShard(@Nonnull final Catnip catnip, @Nonnegative final int id, @Nonnegative final int limit,
                       @Nullable final Presence presence) {
        this.catnip = catnip;
        this.id = id;
        this.limit = limit;
        this.presence = presence;
    
        client = HttpClient.newHttpClient();
        
        control = computeAddress(CONTROL, id);
        websocketQueue = computeAddress(WEBSOCKET_QUEUE, id);
        websocketSend = computeAddress(WEBSOCKET_SEND, id);
        presenceUpdateRequest = computeAddress(PRESENCE_UPDATE_REQUEST, id);
        voiceStateUpdateQueue = computeAddress(VOICE_STATE_UPDATE_QUEUE, id);
        
        sendTask = GatewayTask.gatewaySendTask(catnip, "catnip:gateway:" + id + ":outgoing-send",
                object -> catnip.eventBus().publish(websocketSend, object));
        presenceTask = GatewayTask.gatewayPresenceTask(catnip, presenceUpdateRequest, update -> {
            catnip.eventBus().publish(websocketSend,
                    basePayload(GatewayOp.STATUS_UPDATE, update.asJson()));
            currentPresence = update;
        });
        lifecycleState = CREATED;
    }
    
    public static JsonObject basePayload(@Nonnull final GatewayOp op) {
        return basePayload(op, (JsonObject) null);
    }
    
    public static JsonObject basePayload(@Nonnull final GatewayOp op, @Nullable final JsonObject payload) {
        return new JsonObject()
                .put("op", op.opcode())
                .put("d", payload);
    }
    
    public static JsonObject basePayload(@Nonnull final GatewayOp op, @Nonnull @Nonnegative final Integer payload) {
        return new JsonObject()
                .put("op", op.opcode())
                .put("d", payload);
    }
    
    @Override
    public void start() {
        final EventBus eventBus = catnip.eventBus();
        Collections.addAll(consumers,
                eventBus.consumer(control, this::handleControlMessage),
                eventBus.consumer(websocketQueue, this::handleSocketQueue),
                eventBus.consumer(websocketSend, this::handleSocketSend),
                eventBus.consumer(presenceUpdateRequest, this::handlePresenceUpdate),
                eventBus.consumer(voiceStateUpdateQueue, this::handleVoiceStateUpdateQueue)
        );
        lifecycleState = DEPLOYED;
    }
    
    @Override
    public void stop() {
        stateReply(ShardConnectState.CANCEL);
        lifecycleState = DISCONNECTED;
        consumers.forEach(MessageConsumer::unregister);
        consumers.clear();
        
        if(socket != null) {
            closedByClient = true;
            if(socketOpen) {
                socket.sendClose(4000, "Shutdown");
            }
            socketOpen = false;
        }
        heartbeatAcked = true;
        
        catnip.vertx().cancelTimer(heartbeatTask.get());
    }
    
    private void handleVoiceStateUpdateQueue(final Message<JsonObject> message) {
        sendTask.offer(basePayload(GatewayOp.VOICE_STATE_UPDATE, message.body()));
        sendTask.run();
    }
    
    private void handlePresenceUpdate(final Message<PresenceImpl> message) {
        final PresenceImpl impl = message.body();
        if(impl == null) {
            message.reply(currentPresence);
            return;
        }
        presenceTask.offer(impl);
        presenceTask.run();
    }
    
    private void handleControlMessage(final Message<ShardControlMessage> msg) {
        final ShardControlMessage body = msg.body();
        switch(body) {
            case TRACE: {
                msg.reply(new JsonArray(trace));
                break;
            }
            case CONNECTED: {
                msg.reply(socket != null && socketOpen);
                break;
            }
            case LIFECYCLE_STATE: {
                msg.reply(lifecycleState);
                break;
            }
            case LATENCY: {
                msg.reply(lastHeartbeatLatency);
                break;
            }
            case CONNECT: {
                if(connected) {
                    msg.fail(1000, "Cannot connect shard twice, redeploy it.");
                    return;
                }
                connected = true;
                message = msg;
                connectSocket();
                break;
            }
            default: {
                catnip.logAdapter().warn("Shard {}/{}: Got unknown control message: {}", id, limit, body.name());
                break;
            }
        }
    }
    
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void connectSocket() {
        lifecycleState = CONNECTING;
        catnip.eventBus().publish(Raw.CONNECTING, shardInfo());
        
        final GatewayInfo info = catnip.gatewayInfo();
        if(info != null) {
            connectSocket(info.url());
        } else {
            catnip.fetchGatewayInfo().doOnSuccess(i -> connectSocket(i.url()));
        }
    }
    
    private void stateReply(@Nonnull final ShardConnectState state) {
        if(message != null) {
            message.reply(state);
            message = null;
        }
    }
    
    @SuppressWarnings("squid:HiddenFieldCheck")
    private void connectSocket(final String url) {
        client.newWebSocketBuilder().buildAsync(URI.create(url), this).thenAcceptAsync(ws -> {
            lifecycleState = CONNECTED;
            socket = ws;
            socketOpen = true;
            catnip.eventBus().publish(Raw.CONNECTED, shardInfo());
        }).exceptionally(t -> {
            lifecycleState = DISCONNECTED;
            socket = null;
            socketOpen = false;
            catnip.logAdapter().error("Shard {}/{}: Couldn't connect socket:", id, limit, t);
            catnip.eventBus().publish(Raw.CLOSED, shardInfo());
            stateReply(ShardConnectState.FAILED);
            return null;
        });
    }
    
    private void handleBinaryData(final Buffer binary) {
        if(socket == null) {
            return;
        }
        final boolean isEnd = binary.getInt(binary.length() - 4) == ZLIB_SUFFIX;
        if(!isEnd || readBufferPosition > 0) {
            final int position = readBufferPosition;
            readBuffer.setBuffer(position, binary);
            readBufferPosition = position + binary.length();
        }
        if(isEnd) {
            final Buffer decompressed = decompressBuffer;
            final Buffer dataToDecompress = readBufferPosition > 0 ? readBuffer : binary;
            try(final InflaterOutputStream ios = new InflaterOutputStream(new BufferOutputStream(decompressed, 0), inflater)) {
                synchronized(decompressBuffer) {
                    final int length = Math.max(readBufferPosition, binary.length());
                    int r = 0;
                    while(r < length) {
                        // How many bytes we can read
                        final int read = Math.min(decompress.length, length - r);
                        dataToDecompress.getBytes(r, r + read, decompress);
                        // Decompress
                        ios.write(decompress, 0, read);
                        r += read;
                    }
                }
                handleSocketData(decompressed.toJsonObject());
            } catch(final IOException e) {
                catnip.logAdapter().error("Shard {}/{}: Error decompressing payload", id, limit, e);
                stateReply(ShardConnectState.FAILED);
            } finally {
                readBufferPosition = 0;
            }
        }
    }
    
    private void handleSocketData(JsonObject payload) {
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
                handleHello(payload);
                break;
            }
            case DISPATCH: {
                handleDispatch(payload);
                break;
            }
            case HEARTBEAT: {
                handleHeartbeat();
                break;
            }
            case HEARTBEAT_ACK: {
                handleHeartbeatAck();
                break;
            }
            case INVALID_SESSION: {
                handleInvalidSession(payload);
                break;
            }
            case RECONNECT: {
                handleReconnectRequest();
                break;
            }
            default: {
                break;
            }
        }
    }
    
    // Unsure if we need special impl.
    // @Override public void onOpen(final WebSocket webSocket) {}
    
    @Override
    public CompletionStage<?> onText(final WebSocket webSocket, final CharSequence data, final boolean last) {
        if(socket == null) {
            //Socket is too quick!
            socket = webSocket;
            socketOpen = true;
        }
        //This assertion should only trip if the same 'CatnipShard' instance is bound to two sockets, which should *never* happen under normal conditions.
        //If this does become an issue, we can add logic to ensure one of the sockets die, and persist the other.
        assert webSocket == socket : id + " expected " + socket + "; got" + webSocket;
        if(last) {
            try {
                handleSocketData(new JsonObject(socketInputBuffer.length() > 0 ? socketInputBuffer.append(data).toString() : data.toString()));
            } finally {
                socketInputBuffer.setLength(0);
            }
        } else {
            socketInputBuffer.append(data);
        }
        webSocket.request(1L);
        return null;
    }
    
    @Override
    public CompletionStage<?> onBinary(final WebSocket webSocket, final ByteBuffer data, final boolean last) {
        if(socket == null) {
            //Socket is too quick!
            socket = webSocket;
            socketOpen = true;
        }
        //This assertion should only trip if the same 'CatnipShard' instance is bound to two sockets, which should *never* happen under normal conditions.
        //If this does become an issue, we can add logic to ensure one of the sockets die, and persist the other.
        assert webSocket == socket : id + " expected " + socket + "; got" + webSocket;
    
        //This may need revising, due to the tendency of the socket splitting frames. Although, the method does have a built in handler, so :shrug:
        handleBinaryData(Buffer.buffer(data.array()));
        webSocket.request(1L);
        return null;
    }
    
    @Override
    public void onError(final WebSocket webSocket, final Throwable error) {
        //This assertion should only trip if the same 'CatnipShard' instance is bound to two sockets, which should *never* happen under normal conditions.
        //If this does become an issue, we can add logic to ensure one of the sockets die, and persist the other.
        assert webSocket == socket : id + " expected " + socket + "; got" + webSocket;
        
        socket = null;
        socketOpen = false;
        catnip.logAdapter().error("Shard {}/{}: Couldn't connect socket:", id, limit, error);
        catnip.eventBus().publish(Raw.CLOSED, shardInfo());
        stateReply(ShardConnectState.FAILED);
    }
    
    //@SuppressWarnings("squid:S1172")
    @Override
    public CompletionStage<?> onClose(final WebSocket webSocket, final int closeCode, final String reason) {
        //This assertion should only trip if the same 'CatnipShard' instance is bound to two sockets, which should *never* happen under normal conditions.
        //If this does become an issue, we can add logic to ensure one of the sockets die, and persist the other.
        assert webSocket == socket : id + " expected " + socket + "; got" + webSocket;
        
        //Since the socket closed, lets cancel the timer.
        final boolean cancel = vertx.cancelTimer(heartbeatTask.get());
        catnip.logAdapter().debug("Canceled timer task from socket close: {}", cancel);
        catnip.eventBus().publish(Raw.DISCONNECTED, shardInfo());
        catnip.logAdapter().warn("Shard {}/{}: Socket closing! {} - {}", id, limit, closeCode, reason);
        try {
            socket = null;
            socketOpen = false;
            closedByClient = false;
            catnip.eventBus().publish(Raw.CLOSED, shardInfo());
        } catch(final Exception e) {
            catnip.logAdapter().error("Shard {}/{}: Failure closing socket:", id, limit, e);
            stateReply(ShardConnectState.FAILED);
        }
        
        if(closeCode == GatewayCloseCode.INVALID_SEQ.code() || closeCode == GatewayCloseCode.SESSION_TIMEOUT.code()) {
            // These two close codes invalidate your session (and afaik do not send an OP9).
            catnip.sessionManager().clearSeqnum(id);
            catnip.sessionManager().clearSession(id);
        }
        if(closedByClient) {
            catnip.logAdapter().info("Shard {}/{}: We closed the websocket with code {}", id, limit, closeCode);
        } else {
            if(closeCode >= 4000) {
                final GatewayCloseCode code = GatewayCloseCode.byId(closeCode);
                if(code != null) {
                    catnip.logAdapter().warn("Shard {}/{}: gateway websocket closed with code {}: {}: {}",
                            id, limit, closeCode, code.name(), code.message());
                } else {
                    catnip.logAdapter().warn("Shard {}/{}: gateway websocket closing with code {}: {}",
                            id, limit, closeCode, reason);
                }
            } else {
                catnip.logAdapter().warn("Shard {}/{}: gateway websocket closing with code {}: {}",
                        id, limit, closeCode, reason);
            }
        }
        webSocket.request(1L);
        return null;
    }
    
    private void handleSocketQueue(final Message<JsonObject> msg) {
        sendTask.offer(msg.body());
        sendTask.run();
    }
    
    private void handleSocketSend(final Message<JsonObject> msg) {
        if(socket != null && socketOpen) {
            JsonObject payload = msg.body();
            for(final Extension extension : catnip.extensionManager().extensions()) {
                for(final CatnipHook hook : extension.hooks()) {
                    payload = hook.rawGatewaySendHook(payload);
                }
            }
            socket.sendText(payload.encode(), true);
        }
    }
    
    private void handleHello(final JsonObject event) {
        final JsonObject payload = event.getJsonObject("d");
        trace = JsonUtil.toStringList(payload.getJsonArray("_trace"));
        
        final long taskId = catnip.vertx().setPeriodic(payload.getInteger("heartbeat_interval"), timerId -> {
            if(socket != null && socketOpen) {
                if(!heartbeatAcked) {
                    // Zombie
                    catnip.logAdapter().warn("Shard {}/{}: Heartbeat zombie, queueing reconnect!", id, limit);
                    closedByClient = true;
                    socket.sendClose(4000, "Heartbeat zombie");
                    return;
                }
                catnip.eventBus().publish(websocketSend, basePayload(GatewayOp.HEARTBEAT, catnip.sessionManager().seqnum(id)));
                lastHeartbeat = System.nanoTime();
                heartbeatAcked = false;
            } else {
                final boolean cancel = catnip.vertx().cancelTimer(heartbeatTask.get());
                catnip.logAdapter().debug("Canceled timer task inside of itself: {}", cancel);
            }
        });
        heartbeatTask.set(taskId);
        
        // Check if we can RESUME instead
        if(catnip.sessionManager().session(id) != null && catnip.sessionManager().seqnum(id) > 0) {
            // Some useful notes on how RESUME works, based off of some
            // commentary from Jake in DAPI.
            // tldr, RESUME works as long as you're not trying to RESUME too
            // late. According to Jake, there's a 3-minute window in which you
            // can RESUME; after this, your session goes poof. Beyond that,
            // there is ALSO an internal buffer of events that gets held onto
            // while you're disconnected, which is what gets replayed to you on
            // RESUME. If this buffer fills up in less than that 3-minute
            // window, your session is no longer resumable.
            // See: https://discordapp.com/channels/81384788765712384/381887113391505410/584900930525200386
            lifecycleState = RESUMING;
            catnip.eventBus().publish(websocketSend, resume());
        } else {
            lifecycleState = IDENTIFYING;
            catnip.eventBus().publish(websocketSend, identify());
        }
    }
    
    private void handleDispatch(final JsonObject event) {
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
            trace = Collections.emptyList();
        }
        
        if(event.getValue("s", null) != null) {
            catnip.sessionManager().seqnum(id, event.getInteger("s"));
        }
        
        switch(type) {
            case "READY": {
                lifecycleState = LOGGED_IN;
                catnip.sessionManager().session(id, data.getString("session_id"));
                // Reply after IDENTIFY ratelimit
                catnip.eventBus().publish(Raw.IDENTIFIED, shardInfo());
                stateReply(ShardConnectState.READY);
                break;
            }
            case "RESUMED": {
                lifecycleState = LOGGED_IN;
                // RESUME is fine, just reply immediately
                catnip.eventBus().publish(Raw.RESUMED, shardInfo());
                stateReply(ShardConnectState.RESUMED);
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
    
    private void handleHeartbeat() {
        catnip.eventBus().publish(websocketSend, basePayload(GatewayOp.HEARTBEAT, catnip.sessionManager().seqnum(id)));
    }
    
    private void handleHeartbeatAck() {
        heartbeatAcked = true;
        lastHeartbeatLatency = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - lastHeartbeat);
    }
    
    private void handleInvalidSession(final JsonObject event) {
        if(!event.getBoolean("d")) {
            catnip.logAdapter().info("Session invalidated (OP 9), clearing shard data and reconnecting");
            
            catnip.cacheWorker().invalidateShard(id);
            catnip.sessionManager().clearSession(id);
            catnip.sessionManager().clearSeqnum(id);
            
            closedByClient = true;
        }
    
        stateReply(ShardConnectState.INVALID);
        
        if(socket != null && socketOpen) {
            socket.sendClose(1000, "Reconnecting...");
        }
    }
    
    private void handleReconnectRequest() {
        // Just immediately disconnect
        if(socket != null) {
            closedByClient = true;
            if(socketOpen) {
                socket.sendClose(1000, "Reconnecting...");
            }
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
}
