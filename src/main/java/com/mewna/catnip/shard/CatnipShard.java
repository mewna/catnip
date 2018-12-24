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
import com.mewna.catnip.util.BufferOutputStream;
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
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

import static com.mewna.catnip.shard.CatnipShard.ShardConnectState.*;

/**
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
    
    private final AtomicReference<ShardState> stateRef = new AtomicReference<>(null);
    private final AtomicReference<Presence> currentPresence = new AtomicReference<>(null);
    private final AtomicBoolean heartbeatAcked = new AtomicBoolean(true);
    private final byte[] decompressBuffer = new byte[1024];
    
    private final Deque<JsonObject> messageQueue = new ConcurrentLinkedDeque<>();
    private final Deque<PresenceImpl> presenceQueue = new ConcurrentLinkedDeque<>();
    
    private final AtomicBoolean presenceRateLimitRecheckQueued = new AtomicBoolean();
    private final AtomicBoolean sendRateLimitRecheckQueued = new AtomicBoolean();
    
    private List<String> trace = new ArrayList<>();
    
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
    
    /**
     * Shard control - start, stop, etc
     *
     * @param id Shard ID
     *
     * @return Control address for the given shard
     */
    public static String controlAddress(final int id) {
        return "catnip:shard:" + id + ":control";
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
    
    public static <T> String websocketMessageQueueAddress(@Nonnull final T id) {
        return "catnip:gateway:ws-outgoing:" + id + ":queue";
    }
    
    public static <T> String websocketMessagePresenceUpdateAddress(@Nonnull final T id) {
        return "catnip:gateway:ws-outgoing:" + id + ":presence-update";
    }
    
    public static <T> String websocketMessageVoiceStateUpdateQueueAddress(@Nonnull final T id) {
        return "catnip:gateway:ws-outgoing:" + id + ":voice-state-update:queue";
    }
    
    @Override
    public void start() {
        catnip.eventBus().consumer(controlAddress(id), this::handleControlMessage);
        catnip.eventBus().consumer(websocketMessageSendAddress(), this::handleSocketSend);
        catnip.eventBus().consumer(websocketMessageQueueAddress(), this::handleSocketQueue);
        catnip.eventBus().consumer(websocketMessagePresenceUpdateQueueAddress(), this::handlePresenceUpdateQueue);
        catnip.eventBus().consumer(websocketMessagePresenceUpdateAddress(), this::handlePresenceUpdate);
        catnip.eventBus().consumer(websocketMessagePresenceUpdatePollAddress(), presence -> {
            if(stateRef.get() != null) {
                while(!presenceQueue.isEmpty()) {
                    if(catnip.gatewayRatelimiter().checkRatelimit(websocketMessagePresenceUpdateAddress(), 60_000L, 5).left) {
                        if(!presenceRateLimitRecheckQueued.get()) {
                            presenceRateLimitRecheckQueued.set(true);
                            catnip.vertx().setTimer(1000, __ -> catnip.eventBus().publish(websocketMessagePresenceUpdatePollAddress(), null));
                        }
                        break;
                    }
                    presenceRateLimitRecheckQueued.set(false);
                    final PresenceImpl update = presenceQueue.pop();
                    final JsonObject object = new JsonObject()
                            .put("op", GatewayOp.STATUS_UPDATE.opcode())
                            .put("d", update.asJson());
                    catnip.eventBus().publish(websocketMessageSendAddress(), object);
                    currentPresence.set(update);
                }
            }
        });
        catnip.eventBus().<JsonObject>consumer(websocketMessageVoiceStateUpdateQueueAddress(),
                state -> catnip.eventBus().send(websocketMessageQueueAddress(), basePayload(
                        GatewayOp.VOICE_STATE_UPDATE,
                        state.body()
                )));
        catnip.eventBus().consumer(websocketMessagePollAddress(), msg -> {
            if(stateRef.get() != null) {
                while(!messageQueue.isEmpty()) {
                    // We only do up to 110 messages/min, to allow for room just in case
                    final ImmutablePair<Boolean, Long> check = catnip.gatewayRatelimiter()
                            .checkRatelimit("catnip:gateway:" + id + ":outgoing-send", 60_000L, 110);
                    if(check.left) {
                        // We got ratelimited, stop sending and try again in 1s
                        if(!sendRateLimitRecheckQueued.get()) {
                            sendRateLimitRecheckQueued.set(true);
                            catnip.vertx().setTimer(1000, __ -> catnip.eventBus().publish(websocketMessagePollAddress(), null));
                        }
                        break;
                    }
                    sendRateLimitRecheckQueued.set(false);
                    final JsonObject payload = messageQueue.pop();
                    catnip.eventBus().publish(websocketMessageSendAddress(), payload);
                }
            }
        });
        // Start gateway poll
        //catnip.eventBus().publish(websocketMessagePollAddress(), null);
        //catnip.eventBus().publish(websocketMessagePresenceUpdatePollAddress(), null);
    }
    
    @Override
    public void stop() {
    }
    
    private void handlePresenceUpdate(final Message<PresenceImpl> message) {
        final PresenceImpl impl = message.body();
        if(impl == null) {
            message.reply(currentPresence.get());
            return;
        }
        catnip.eventBus().publish(websocketMessagePresenceUpdateQueueAddress(), impl);
    }
    
    private void handleControlMessage(final Message<JsonObject> msg) {
        final JsonObject body = msg.body();
        final String mode = body.getString("mode");
        switch(mode.toUpperCase()) {
            case "START": {
                doStart(msg);
                break;
            }
            case "STOP": {
                doStop();
                break;
            }
            case "SHUTDOWN": {
                doStop();
                catnip.vertx().undeploy(deploymentID());
                break;
            }
            case "TRACE": {
                msg.reply(new JsonArray(trace));
                break;
            }
            case "CONNECTED": {
                msg.reply(stateRef.get().socketOpen().get());
                break;
            }
            default: {
                catnip.logAdapter().warn("Shard {} Got unknown control message: {}", id, body.encodePrettily());
                break;
            }
        }
    }
    
    private void doStart(final Message<JsonObject> msg) {
        connectSocket(msg);
    }
    
    private void doStop() {
        if(stateRef.get() != null) {
            stateRef.get().socket().close((short) 4000);
            stateRef.get().socketOpen().set(false);
        }
        messageQueue.clear();
        presenceQueue.clear();
        heartbeatAcked.set(true);
    }
    
    private void connectSocket(final Message<JsonObject> msg) {
        catnip.eventBus().publish(Raw.CONNECTING, shardInfo());
    
        //noinspection ConstantConditions
        client.websocketAbs(catnip.getGatewayInfo().url(), null, null, null,
                socket -> {
                    catnip.eventBus().publish(Raw.CONNECTED, shardInfo());
                    socket.frameHandler(frame -> handleSocketFrame(msg, frame))
                            .closeHandler(this::handleSocketClose)
                            .exceptionHandler(Throwable::printStackTrace);
                    stateRef.set(new ShardState(socket));
                    stateRef.get().socketOpen().set(true);
                },
                failure -> {
                    stateRef.set(null);
                    catnip.logAdapter().error("Couldn't connect socket:", failure);
                    catnip.eventBus().publish("RAW_STATUS", new JsonObject().put("status", "down:fail-connect")
                            .put("shard", id));
                    // If we totally fail to connect socket, don't need to worry as much
                    catnip.vertx().setTimer(500L, __ -> msg.reply(new JsonObject().put("state", FAILED.name())));
                });
    }
    
    private void handleBinaryData(final Message<JsonObject> msg, final Buffer binary) {
        final ShardState state = stateRef.get();
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
                catnip.logAdapter().error("Error decompressing payload", e);
            } finally {
                state.readBufferPosition(0);
            }
        }
    }
    
    private void handleSocketFrame(final Message<JsonObject> msg, final WebSocketFrame frame) {
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
                if(closeCode >= 4000) {
                    final GatewayCloseCode code = GatewayCloseCode.byId(closeCode);
                    if(code != null) {
                        catnip.logAdapter().warn("Shard {}: gateway websocket closed with code {}: {}: {}",
                                id, closeCode, code.name(), code.message());
                    } else {
                        catnip.logAdapter().warn("Shard {}: gateway websocket closing with code {}: {}",
                                id, closeCode, frame.closeReason());
                    }
                } else {
                    catnip.logAdapter().warn("Shard {}: gateway websocket closing with code {}: {}",
                            id, closeCode, frame.closeReason());
                }
                stateRef.get().socketOpen().set(false);
            }
        } catch(final Exception e) {
            e.printStackTrace();
        }
    }
    
    private void handleSocketData(final Message<JsonObject> msg, JsonObject payload) {
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
        // Emit messages for subconsumers
        catnip.eventBus().publish(websocketMessageRecvAddress(op), payload);
        catnip.eventBus().publish("RAW_WS", payload);
    }
    
    private void handleSocketClose(final Void __) {
        catnip.eventBus().publish(Raw.DISCONNECTED, shardInfo());
        catnip.logAdapter().warn("Socket closing!");
        try {
            catnip.eventBus().publish("RAW_STATUS", new JsonObject().put("status", "down:socket-close")
                    .put("shard", id));
            stateRef.set(null);
            catnip.shardManager().addToConnectQueue(id);
        } catch(final Exception e) {
            catnip.logAdapter().error("Failure closing socket:", e);
        }
    }
    
    private void handleSocketQueue(final Message<JsonObject> msg) {
        messageQueue.addLast(msg.body());
        catnip.eventBus().publish(websocketMessagePollAddress(), null);
    }
    
    private void handlePresenceUpdateQueue(final Message<PresenceImpl> msg) {
        presenceQueue.addLast(msg.body());
        catnip.eventBus().publish(websocketMessagePresenceUpdatePollAddress(), null);
    }
    
    private void handleSocketSend(final Message<JsonObject> msg) {
        final ShardState shardState = stateRef.get();
        if(shardState != null && shardState.socket() != null && shardState.socketOpen().get()) {
            JsonObject payload = msg.body();
            for(final Extension extension : catnip.extensionManager().extensions()) {
                for(final CatnipHook hook : extension.hooks()) {
                    payload = hook.rawGatewaySendHook(payload);
                }
            }
            shardState.socket().writeTextMessage(payload.encode());
        }
    }
    
    private void handleHello(final Message<JsonObject> msg, final JsonObject event) {
        final JsonObject payload = event.getJsonObject("d");
        trace = payload.getJsonArray("_trace").stream().map(e -> (String) e).collect(Collectors.toList());
        
        catnip.vertx().setPeriodic(payload.getInteger("heartbeat_interval"), timerId -> {
            final ShardState shardState = stateRef.get();
            if(shardState != null && shardState.socket() != null && shardState.socketOpen().get()) {
                if(!heartbeatAcked.get()) {
                    // Zombie
                    catnip.logAdapter().warn("Shard {} zombied, queueing reconnect!", id);
                    catnip.vertx().cancelTimer(timerId);
                    catnip.eventBus().publish(controlAddress(id), new JsonObject().put("mode", "STOP"));
                    return;
                }
                catnip.eventBus().publish(websocketMessageSendAddress(),
                        basePayload(GatewayOp.HEARTBEAT, catnip.sessionManager().seqnum(id)));
                heartbeatAcked.set(false);
            } else {
                catnip.vertx().cancelTimer(timerId);
            }
        });
        
        // Check if we can RESUME instead
        if(catnip.sessionManager().session(id) != null && catnip.sessionManager().seqnum(id) > 0) {
            catnip.eventBus().publish(websocketMessageSendAddress(), resume());
        } else {
            catnip.eventBus().publish(websocketMessageSendAddress(), identify());
        }
    }
    
    private void handleDispatch(final Message<JsonObject> msg, final JsonObject event) {
        // Should be safe to ignore
        if(event.getValue("d") instanceof JsonArray) {
            return;
        }
        final JsonObject data = event.getJsonObject("d");
        final String type = event.getString("t");
        
        // Update trace and seqnum as needed
        if(data.getJsonArray("_trace", null) != null) {
            trace = data.getJsonArray("_trace").stream().map(e -> (String) e).collect(Collectors.toList());
        }
        if(event.getValue("s", null) != null) {
            catnip.sessionManager().seqnum(id, event.getInteger("s"));
        }
        
        switch(type) {
            case "READY": {
                // Reply after IDENTIFY ratelimit
                catnip.sessionManager().session(id, data.getString("session_id"));
                if(id == limit - 1) {
                    // No need to delay
                    msg.reply(new JsonObject().put("state", READY.name()));
                } else {
                    // More shards to go, delay
                    catnip.vertx().setTimer(5500L, __ -> msg.reply(new JsonObject().put("state", READY.name())));
                }
                catnip.eventBus().publish(Raw.IDENTIFIED, shardInfo());
                break;
            }
            case "RESUMED": {
                // RESUME is fine, just reply immediately
                msg.reply(new JsonObject().put("state", RESUMED.name()));
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
        catnip.eventBus().publish("RAW_DISPATCH", event);
    }
    
    private void handleHeartbeat(final Message<JsonObject> msg, final JsonObject event) {
        //heartbeatAcked.set(false);
        catnip.eventBus().publish(websocketMessageSendAddress(),
                basePayload(GatewayOp.HEARTBEAT, catnip.sessionManager().seqnum(id)));
    }
    
    private void handleHeartbeatAck(final Message<JsonObject> msg, final JsonObject event) {
        heartbeatAcked.set(true);
    }
    
    private void handleInvalidSession(final Message<JsonObject> msg, final JsonObject event) {
        if(event.getBoolean("d")) {
            // Can resume
            if(stateRef.get() != null) {
                stateRef.get().socket().close();
            }
        } else {
            // Can't resume, clear old data
            if(stateRef.get() != null) {
                stateRef.get().socket().close();
                catnip.sessionManager().clearSession(id);
                catnip.sessionManager().clearSeqnum(id);
            }
        }
    }
    
    private void handleReconnectRequest(final Message<JsonObject> msg, final JsonObject event) {
        // Just immediately disconnect
        if(stateRef.get() != null) {
            stateRef.get().socket().close();
        }
    }
    
    /**
     * Incoming socket payloads - DISPATCH, HEARTBEAT, etc
     *
     * @param op Gateway op
     *
     * @return Socket payload recv. msg. address
     */
    public String websocketMessageRecvAddress(final GatewayOp op) {
        return "catnip:gateway:ws-incoming:" + id + ':' + op.name();
    }
    
    public String websocketMessageSendAddress() {
        return "catnip:gateway:ws-outgoing:" + id;
    }
    
    public String websocketMessageQueueAddress() {
        return websocketMessageQueueAddress(id);
    }
    
    private String websocketMessagePollAddress() {
        return "catnip:gateway:ws-outgoing:" + id + ":poll";
    }
    
    private String websocketMessagePresenceUpdateAddress() {
        return websocketMessagePresenceUpdateAddress(id);
    }
    
    private String websocketMessagePresenceUpdatePollAddress() {
        return "catnip:gateway:ws-outgoing:" + id + ":presence-update:poll";
    }
    
    private String websocketMessagePresenceUpdateQueueAddress() {
        return "catnip:gateway:ws-outgoing:" + id + ":presence-update:queue";
    }
    
    private String websocketMessageVoiceStateUpdateQueueAddress() {
        return websocketMessageVoiceStateUpdateQueueAddress(id);
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
        private final AtomicBoolean socketOpen = new AtomicBoolean(false);
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
