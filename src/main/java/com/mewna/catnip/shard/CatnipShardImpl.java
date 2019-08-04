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

import com.grack.nanojson.*;
import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.impl.lifecycle.GatewayClosedImpl;
import com.mewna.catnip.entity.impl.lifecycle.GatewayConnectionFailedImpl;
import com.mewna.catnip.entity.impl.user.PresenceImpl;
import com.mewna.catnip.entity.misc.GatewayInfo;
import com.mewna.catnip.entity.user.Presence;
import com.mewna.catnip.extension.Extension;
import com.mewna.catnip.extension.hook.CatnipHook;
import com.mewna.catnip.internal.CatnipImpl;
import com.mewna.catnip.shard.LifecycleEvent.Raw;
import com.mewna.catnip.shard.manager.AbstractShardManager;
import com.mewna.catnip.shard.manager.DefaultShardManager;
import com.mewna.catnip.shard.manager.ShardManager;
import com.mewna.catnip.util.BufferOutputStream;
import com.mewna.catnip.util.JsonUtil;
import com.mewna.catnip.util.ReentrantLockWebSocket;
import com.mewna.catnip.util.task.GatewayTask;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.vertx.core.buffer.Buffer;
import lombok.Getter;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URI;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

import static com.mewna.catnip.shard.LifecycleState.*;

/**
 * A catnip shard encapsulates a single websocket connection to Discord's
 * real-time gateway. Shards should be deployed/undeployed by a {@link ShardManager};
 * see {@link DefaultShardManager} and {@link AbstractShardManager} for more.
 * <p/>
 * Shards are controlled by calling the methods exposed on the {@link CatnipShard};
 * it is NOT recommended that you store a reference to a shard object; and instead
 * poll the {@link ShardManager} for the shard when needed.
 *
 * @author amy
 * @since 8/31/18.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class CatnipShardImpl implements CatnipShard, Listener {
    public static final int ZLIB_SUFFIX = 0x0000FFFF;
    
    private final Catnip catnip;
    private final int id;
    private final int limit;
    private final Presence presence;
    
    // This is an AtomicLong instead of a volatile long because IntelliJ got
    // A N G E R Y because I guess longs don't get written atomically.
    private final AtomicLong heartbeatTask = new AtomicLong(-1L);
    private final Buffer readBuffer = Buffer.buffer();
    private final Buffer decompressBuffer = Buffer.buffer();
    private final StringBuffer socketInputBuffer = new StringBuffer(); //Using a StringBuffer instead of a StringBuilder due to async-friendly synchronizations.
    private final byte[] decompress = new byte[1024];
    private final GatewayTask<JsonObject> sendTask;
    private final GatewayTask<PresenceImpl> presenceTask;
    private volatile Presence currentPresence;
    private volatile boolean heartbeatAcked = true;
    private volatile long lastHeartbeat = -1; //use System.nanoTime() as that is monotonic
    private volatile long lastHeartbeatLatency = -1;
    private volatile boolean presenceRateLimitRecheckQueued;
    private volatile boolean sendRateLimitRecheckQueued;
    @Getter
    private volatile List<String> trace = Collections.emptyList();
    private volatile boolean connected;
    private volatile boolean socketOpen;
    private volatile boolean closedByClient;
    private WebSocket socket;
    private Inflater inflater;
    private int readBufferPosition;
    private SingleEmitter<ShardConnectState> message;
    private LifecycleState lifecycleState;
    
    public CatnipShardImpl(@Nonnull final Catnip catnip, @Nonnegative final int id, @Nonnegative final int limit,
                           @Nullable final Presence presence) {
        this.catnip = catnip;
        this.id = id;
        this.limit = limit;
        this.presence = presence;
        
        sendTask = GatewayTask.gatewaySendTask(catnip, "catnip:gateway:" + id + ":outgoing-send", this::sendToSocket);
        presenceTask = GatewayTask.gatewayPresenceTask(catnip, "catnip:gateway:ws-outgoing:" + id + ":presence-update",
                update -> {
                    sendToSocket(basePayload(GatewayOp.STATUS_UPDATE, update.asJson()));
                    currentPresence = update;
                });
        lifecycleState = CREATED;
    }
    
    public static JsonObject basePayload(@Nonnull final GatewayOp op) {
        return basePayload(op, (JsonObject) null);
    }
    
    public static JsonObject basePayload(@Nonnull final GatewayOp op, @Nullable final JsonObject payload) {
        return JsonObject.builder()
                .value("op", op.opcode())
                .value("d", payload)
                .done();
    }
    
    public static JsonObject basePayload(@Nonnull final GatewayOp op, @Nonnull @Nonnegative final Integer payload) {
        return JsonObject.builder()
                .value("op", op.opcode())
                .value("d", payload)
                .done();
    }
    
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void connectSocket() {
        lifecycleState = CONNECTING;
        catnip.dispatchManager().dispatchEvent(Raw.CONNECTING_TO_GATEWAY, shardInfo());
        
        final GatewayInfo info = catnip.gatewayInfo();
        if(info != null) {
            connectSocket(info.url());
        } else {
            catnip.fetchGatewayInfo().doOnSuccess(i -> connectSocket(i.url()));
        }
    }
    
    private void stateReply(@Nonnull final ShardConnectState state) {
        if(message != null) {
            message.onSuccess(state);
            message = null;
        }
    }
    
    @SuppressWarnings("squid:HiddenFieldCheck")
    private void connectSocket(final String url) {
        catnip.httpClient().newWebSocketBuilder().buildAsync(URI.create(url), this).thenAcceptAsync(ws -> {
            lifecycleState = CONNECTED;
            socket = new ReentrantLockWebSocket(ws);
            socketOpen = true;
            catnip.dispatchManager().dispatchEvent(Raw.CONNECTED_TO_GATEWAY, shardInfo());
        }).exceptionally(t -> {
            lifecycleState = DISCONNECTED;
            socket = null;
            socketOpen = false;
            if(catnip.logLifecycleEvents()) {
                catnip.logAdapter().error("Shard {}/{}: Couldn't connect socket:", id, limit, t);
            }
            catnip.dispatchManager().dispatchEvent(Raw.GATEWAY_WEBSOCKET_CONNECTION_FAILED,
                    new GatewayConnectionFailedImpl(shardInfo(), t, catnip));
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
                handleSocketData(JsonParser.object().from(decompressed.toString()));
            } catch(final IOException e) {
                catnip.logAdapter().error("Shard {}/{}: Error decompressing payload", id, limit, e);
                stateReply(ShardConnectState.FAILED);
            } catch(final JsonParserException e) {
                catnip.logAdapter().error("Shard {}/{}: Error parsing payload", id, limit, e);
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
        
        final GatewayOp op = GatewayOp.byId(payload.getInt("op"));
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
            // Socket is too quick!
            socket = new ReentrantLockWebSocket(webSocket);
            socketOpen = true;
        }
        if(last) {
            try {
                final var payload = socketInputBuffer.length() > 0 ? socketInputBuffer.append(data).toString() : data.toString();
                handleSocketData(JsonParser.object().from(payload));
            } catch(final JsonParserException e) {
                catnip.logAdapter().error("Shard {}/{}: Error parsing payload", id, limit, e);
                stateReply(ShardConnectState.FAILED);
            } finally {
                socketInputBuffer.setLength(0);
            }
        } else {
            socketInputBuffer.append(data);
        }
        socket.request(1L);
        return null;
    }
    
    @Override
    public CompletionStage<?> onBinary(final WebSocket webSocket, final ByteBuffer data, final boolean last) {
        if(socket == null) {
            // Socket is too quick!
            socket = new ReentrantLockWebSocket(webSocket);
            socketOpen = true;
        }
        // This may need revising, due to the tendency of the socket splitting
        // frames. Although, the method does have a built in handler, so
        // :shrug:
        handleBinaryData(Buffer.buffer(data.array()));
        socket.request(1L);
        return null;
    }
    
    @Override
    public void onError(final WebSocket webSocket, final Throwable error) {
        socket = null;
        socketOpen = false;
        if(catnip.logLifecycleEvents()) {
            catnip.logAdapter().error("Shard {}/{}: Couldn't connect socket:", id, limit, error);
        }
        catnip.dispatchManager().dispatchEvent(Raw.GATEWAY_WEBSOCKET_CONNECTION_FAILED,
                new GatewayConnectionFailedImpl(shardInfo(), error, catnip));
        stateReply(ShardConnectState.FAILED);
    }
    
    //@SuppressWarnings("squid:S1172")
    @Override
    public CompletionStage<?> onClose(final WebSocket webSocket, final int closeCode, final String reason) {
        // Since the socket closed, lets cancel the timer.
        final boolean cancel = catnip.taskScheduler().cancel(heartbeatTask.get());
        catnip.logAdapter().debug("Canceled timer task from socket close: {}", cancel);
        catnip.dispatchManager().dispatchEvent(Raw.DISCONNECTED_FROM_GATEWAY, shardInfo());
        if(catnip.logLifecycleEvents()) {
            catnip.logAdapter().warn("Shard {}/{}: Socket closing! {} - {}", id, limit, closeCode, reason);
        }
        try {
            socket = null;
            socketOpen = false;
            closedByClient = false;
            catnip.dispatchManager().dispatchEvent(Raw.GATEWAY_WEBSOCKET_CLOSED,
                    new GatewayClosedImpl(shardInfo(), closeCode, reason, catnip));
        } catch(final Exception e) {
            catnip.logAdapter().error("Shard {}/{}: Failure closing socket:", id, limit, e);
            stateReply(ShardConnectState.FAILED);
        }
        
        if(closeCode == GatewayCloseCode.INVALID_SEQ.code() || closeCode == GatewayCloseCode.SESSION_TIMEOUT.code()) {
            // These two close codes invalidate your session (and afaik do not send an OP9).
            catnip.sessionManager().clearSeqnum(id);
            catnip.sessionManager().clearSession(id);
            catnip.dispatchManager().dispatchEvent(Raw.SESSION_INVALIDATED, shardInfo());
        }
        if(closedByClient) {
            if(catnip.logLifecycleEvents()) {
                catnip.logAdapter().info("Shard {}/{}: We closed the websocket with code {}", id, limit, closeCode);
            }
        } else {
            if(closeCode >= 4000) {
                final GatewayCloseCode code = GatewayCloseCode.byId(closeCode);
                if(code != null) {
                    if(catnip.logLifecycleEvents()) {
                        catnip.logAdapter().warn("Shard {}/{}: gateway websocket closed with code {}: {}: {}",
                                id, limit, closeCode, code.name(), code.message());
                    }
                } else {
                    if(catnip.logLifecycleEvents()) {
                        catnip.logAdapter().warn("Shard {}/{}: gateway websocket closing with code {}: {}",
                                id, limit, closeCode, reason);
                    }
                }
            } else {
                if(catnip.logLifecycleEvents()) {
                    catnip.logAdapter().warn("Shard {}/{}: gateway websocket closing with code {}: {}",
                            id, limit, closeCode, reason);
                }
            }
        }
        return null;
    }
    
    @Override
    public boolean isConnected() {
        return socket != null && socketOpen;
    }
    
    @Nonnull
    @Override
    public LifecycleState getLifecycleState() {
        return lifecycleState;
    }
    
    @Override
    public long getLastHeartbeatLatency() {
        return lastHeartbeatLatency;
    }
    
    @Nonnull
    @Override
    public Single<ShardConnectState> connect() {
        if(connected) {
            return Single.error(new IllegalStateException("Cannot connect shard twice, redeploy it."));
        }
        connected = true;
        final var result = Single.<ShardConnectState>create(emitter -> message = emitter);
        connectSocket();
        return result;
    }
    
    @Override
    public void disconnect() {
        stateReply(ShardConnectState.CANCEL);
        lifecycleState = DISCONNECTED;
        
        if(socket != null) {
            closedByClient = true;
            if(socketOpen) {
                socket.sendClose(4000, "Shutdown");
            }
        }
        heartbeatAcked = true;
        
        catnip.taskScheduler().cancel(heartbeatTask.get());
    }
    
    @Override
    public void queueSendToSocket(@Nonnull final JsonObject payload) {
        sendTask.offer(payload);
        sendTask.run();
    }
    
    @Override
    public void sendToSocket(@Nonnull JsonObject payload) {
        if(socket != null && socketOpen) {
            for(final Extension extension : catnip.extensionManager().extensions()) {
                for(final CatnipHook hook : extension.hooks()) {
                    payload = hook.rawGatewaySendHook(payload);
                }
            }
            socket.sendText(JsonWriter.string(payload), true);
        }
    }
    
    @Override
    public void updatePresence(@Nonnull final PresenceImpl presence) {
        presenceTask.offer(presence);
        presenceTask.run();
    }
    
    @Nonnull
    @Override
    public Presence getPresence() {
        return currentPresence;
    }
    
    @Override
    public void queueVoiceStateUpdate(@Nonnull final JsonObject json) {
        sendTask.offer(basePayload(GatewayOp.VOICE_STATE_UPDATE, json));
        sendTask.run();
    }
    
    private void handleHello(final JsonObject event) {
        final JsonObject payload = event.getObject("d");
        trace = JsonUtil.toStringList(payload.getArray("_trace"));
        
        final long taskId = catnip.taskScheduler().setInterval(payload.getInt("heartbeat_interval"), timerId -> {
            if(socket != null && socketOpen) {
                if(!heartbeatAcked) {
                    // Zombie
                    catnip.logAdapter().warn("Shard {}/{}: Heartbeat zombie, queueing reconnect!", id, limit);
                    closedByClient = true;
                    socket.sendClose(4000, "Heartbeat zombie");
                    return;
                }
                sendToSocket(basePayload(GatewayOp.HEARTBEAT, catnip.sessionManager().seqnum(id)));
                lastHeartbeat = System.nanoTime();
                heartbeatAcked = false;
            } else {
                final boolean cancel = catnip.taskScheduler().cancel(heartbeatTask.get());
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
            sendToSocket(resume());
        } else {
            lifecycleState = IDENTIFYING;
            sendToSocket(identify());
        }
    }
    
    private void handleDispatch(final JsonObject event) {
        // Should be safe to ignore
        if(event.get("d") instanceof JsonArray) {
            return;
        }
        final JsonObject data = event.getObject("d");
        final String type = event.getString("t");
        
        // Update trace and seqnum as needed
        if(data.getArray("_trace", null) != null) {
            trace = JsonUtil.toStringList(data.getArray("_trace"));
        } else {
            trace = Collections.emptyList();
        }
        
        if(event.get("s") != null) {
            catnip.sessionManager().seqnum(id, event.getInt("s"));
        }
        
        switch(type) {
            case "READY": {
                lifecycleState = LOGGED_IN;
                catnip.sessionManager().session(id, data.getString("session_id"));
                // Reply after IDENTIFY ratelimit
                catnip.dispatchManager().dispatchEvent(Raw.IDENTIFIED, shardInfo());
                stateReply(ShardConnectState.READY);
                break;
            }
            case "RESUMED": {
                lifecycleState = LOGGED_IN;
                // RESUME is fine, just reply immediately
                catnip.dispatchManager().dispatchEvent(Raw.RESUMED, shardInfo());
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
        event.put("shard", JsonObject.builder().value("id", id).value("limit", limit).done());
        catnip.eventBuffer().buffer(event);
    }
    
    private void handleHeartbeat() {
        sendToSocket(basePayload(GatewayOp.HEARTBEAT, catnip.sessionManager().seqnum(id)));
    }
    
    private void handleHeartbeatAck() {
        heartbeatAcked = true;
        lastHeartbeatLatency = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - lastHeartbeat);
    }
    
    private void handleInvalidSession(final JsonObject event) {
        if(!event.getBoolean("d")) {
            if(catnip.logLifecycleEvents()) {
                catnip.logAdapter().info("Session invalidated (OP 9), clearing shard data and reconnecting");
            }
            
            catnip.cacheWorker().invalidateShard(id);
            catnip.sessionManager().clearSession(id);
            catnip.sessionManager().clearSeqnum(id);
            catnip.dispatchManager().dispatchEvent(Raw.SESSION_INVALIDATED, shardInfo());
            
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
        final JsonObject data = JsonObject.builder()
                .value("token", catnip.token())
                .value("compress", false)
                .value("guild_subscriptions", ((CatnipImpl) catnip).options().enableGuildSubscriptions())
                .value("large_threshold", catnip.largeThreshold())
                .array("shard")
                    .value(id)
                    .value(limit)
                .end()
                .object("properties")
                    .value("$os", "JVM")
                    .value("$browser", "catnip")
                    .value("$device", "catnip")
                .end()
                .done();
        if(presence != null) {
            data.put("presence", ((PresenceImpl) presence).asJson());
        }
        return basePayload(GatewayOp.IDENTIFY, data);
    }
    
    private JsonObject resume() {
        return JsonObject.builder()
                .value("token", catnip.token())
                .value("compress", false)
                .value("session_id", catnip.sessionManager().session(id))
                .value("seq", catnip.sessionManager().seqnum(id))
                .object("properties")
                    .value("$os", "JVM")
                    .value("$browser", "catnip")
                    .value("$device", "catnip")
                .end()
                .done();
    }
    
    private ShardInfo shardInfo() {
        return new ShardInfo(id, limit);
    }
}
