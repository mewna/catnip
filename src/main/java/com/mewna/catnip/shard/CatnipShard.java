package com.mewna.catnip.shard;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.internal.CatnipImpl;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebSocketFrame;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.mewna.catnip.shard.CatnipShard.ShardConnectState.*;

/**
 * @author amy
 * @since 8/31/18.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class CatnipShard extends AbstractVerticle {
    private final Catnip catnip;
    private final int id;
    private final int limit;
    
    private final HttpClient client;
    
    private final AtomicReference<WebSocket> socketRef = new AtomicReference<>(null);
    private final AtomicBoolean heartbeatAcked = new AtomicBoolean(true);
    
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private final DispatchEmitter emitter;
    
    private final Deque<JsonObject> messageQueue = new ConcurrentLinkedDeque<>();
    
    public CatnipShard(final Catnip catnip, final int id, final int limit) {
        this.catnip = catnip;
        this.id = id;
        this.limit = limit;
        
        client = catnip.vertx().createHttpClient(new HttpClientOptions()
                .setMaxWebsocketFrameSize(Integer.MAX_VALUE)
                .setMaxWebsocketMessageSize(Integer.MAX_VALUE));
        emitter = new DispatchEmitter(catnip);
    }
    
    /**
     * Shard control - start, stop, etc
     *
     * @param id Shard ID
     *
     * @return Control address for the given shard
     */
    public static String getControlAddress(final int id) {
        return String.format("catnip:shard:%s:control", id);
    }
    
    public static JsonObject getBasePayload(final GatewayOp op) {
        return getBasePayload(op, (JsonObject) null);
    }
    
    public static JsonObject getBasePayload(final GatewayOp op, final JsonObject payload) {
        return new JsonObject()
                .put("op", op.opcode())
                .put("d", payload)
                ;
    }
    
    public static JsonObject getBasePayload(final GatewayOp op, final Integer payload) {
        return new JsonObject()
                .put("op", op.opcode())
                .put("d", payload)
                ;
    }
    
    @Override
    public void start() {
        catnip.eventBus().consumer(getControlAddress(id), this::handleControlMessage);
        catnip.eventBus().consumer(websocketMessageSendAddress(), this::handleSocketSend);
        catnip.eventBus().consumer(websocketMessageQueueAddress(), this::handleSocketQueue);
        catnip.eventBus().consumer(websocketMessagePollAddress(), msg -> {
            if(socketRef.get() != null) {
                if(!messageQueue.isEmpty()) {
                    // We only do up to 110 messages/min, to allow for room just in case
                    final ImmutablePair<Boolean, Long> check = catnip.gatewayRatelimiter()
                            .checkRatelimit(String.format("catnip:gateway:%s:outgoing-send", id), 60_000L, 110);
                    if(!check.left) {
                        // We're good, send now
                        final JsonObject payload = messageQueue.pop();
                        catnip.eventBus().send(websocketMessageSendAddress(), payload);
                    }
                }
            }
            catnip.eventBus().send(websocketMessagePollAddress(), null);
        });
        // Start gateway poll
        catnip.eventBus().send(websocketMessagePollAddress(), null);
    }
    
    @Override
    public void stop() {
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
            default: {
                // TODO: Logging
                break;
            }
        }
    }
    
    private void doStart(final Message<JsonObject> msg) {
        connectSocket(msg);
    }
    
    private void doStop() {
        if(socketRef.get() != null) {
            socketRef.get().close((short) 4000);
        }
        messageQueue.clear();
        heartbeatAcked.set(true);
    }
    
    // Payload handling
    
    private void connectSocket(final Message<JsonObject> msg) {
        client.websocketAbs(CatnipImpl.getGatewayUrl(), null, null, null,
                socket -> {
                    socket.frameHandler(frame -> handleSocketFrame(msg, frame))
                            .closeHandler(this::handleSocketClose)
                            .exceptionHandler(Throwable::printStackTrace);
                    socketRef.set(socket);
                },
                failure -> {
                    socketRef.set(null);
                    logger.error("Couldn't connect socket:", failure);
                    catnip.eventBus().<JsonObject>send("RAW_STATUS", new JsonObject().put("status", "down:fail-connect").put("shard", id));
                    // If we totally fail to connect socket, don't need to worry as much
                    catnip.vertx().setTimer(500L, __ -> msg.reply(new JsonObject().put("state", FAILED.name())));
                });
    }
    
    private void handleSocketFrame(final Message<JsonObject> msg, final WebSocketFrame frame) {
        // TODO: Support zlib
        try {
            if(frame.isText()) {
                final JsonObject event = new JsonObject(frame.textData());
                final GatewayOp op = GatewayOp.byId(event.getInteger("op"));
                // We pass `msg` for consistency (and for the off-chance it's
                // needed), but REALLY you don't wanna do anything with it. It
                // gets passed *entirely* so that we can reply to the shard
                // manager directly.
                switch(op) {
                    case HELLO: {
                        handleHello(msg, event);
                        break;
                    }
                    case DISPATCH: {
                        handleDispatch(msg, event);
                        break;
                    }
                    case HEARTBEAT: {
                        handleHeartbeat(msg, event);
                        break;
                    }
                    case HEARTBEAT_ACK: {
                        handleHeartbeatAck(msg, event);
                        break;
                    }
                    case INVALID_SESSION: {
                        handleInvalidSession(msg, event);
                        break;
                    }
                    case RECONNECT: {
                        handleReconnectRequest(msg, event);
                        break;
                    }
                    default: {
                        break;
                    }
                }
                // Emit messages for subconsumers
                catnip.eventBus().<JsonObject>send(websocketMessageRecvAddress(op), event);
                catnip.eventBus().<JsonObject>send("RAW_WS", event);
            }
        } catch(final Exception e) {
            e.printStackTrace();
        }
    }
    
    private void handleSocketClose(final Void __) {
        logger.warn("Socket closing!");
        try {
            catnip.eventBus().<JsonObject>send("RAW_STATUS", new JsonObject().put("status", "down:socket-close").put("shard", id));
            socketRef.set(null);
            catnip.shardManager().addToConnectQueue(id);
        } catch(final Exception e) {
            logger.error("Failure closing socket:", e);
        }
    }
    
    private void handleSocketQueue(final Message<JsonObject> msg) {
        messageQueue.addLast(msg.body());
    }
    
    private void handleSocketSend(final Message<JsonObject> msg) {
        final WebSocket webSocket = socketRef.get();
        if(webSocket != null) {
            webSocket.writeTextMessage(msg.body().encode());
        }
    }
    
    private void handleHello(final Message<JsonObject> msg, final JsonObject event) {
        final JsonObject payload = event.getJsonObject("d");
        // TODO: Handle trace here
        
        catnip.vertx().setPeriodic(payload.getInteger("heartbeat_interval"), timerId -> {
            if(socketRef.get() != null) {
                if(!heartbeatAcked.get()) {
                    // Zombie
                    logger.warn("Shard {} zombied, queueing reconnect!", id);
                    catnip.eventBus().send(getControlAddress(id), new JsonObject().put("mode", "STOP"));
                    return;
                }
                catnip.eventBus().send(websocketMessageSendAddress(),
                        getBasePayload(GatewayOp.HEARTBEAT, catnip.sessionManager().seqnum(id)));
                heartbeatAcked.set(false);
            } else {
                catnip.vertx().cancelTimer(timerId);
            }
        });
        
        // Check if we can RESUME instead
        if(catnip.sessionManager().session(id) != null && catnip.sessionManager().seqnum(id) > 0) {
            catnip.eventBus().send(websocketMessageSendAddress(), resume());
        } else {
            catnip.eventBus().send(websocketMessageSendAddress(), identify());
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
        // TODO: Update trace here
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
                break;
            }
            case "RESUMED": {
                // RESUME is fine, just reply immediately
                msg.reply(new JsonObject().put("state", RESUMED.name()));
                break;
            }
            default: {
                break;
            }
        }
        
        emitter.emit(event);
        catnip.eventBus().<JsonObject>send("RAW_DISPATCH", event);
        //catnip.eventBus().send(type, data);
    }
    
    // Addresses
    
    private void handleHeartbeat(final Message<JsonObject> msg, final JsonObject event) {
        //heartbeatAcked.set(false);
        catnip.eventBus().send(websocketMessageSendAddress(),
                getBasePayload(GatewayOp.HEARTBEAT, catnip.sessionManager().seqnum(id)));
    }
    
    private void handleHeartbeatAck(final Message<JsonObject> msg, final JsonObject event) {
        heartbeatAcked.set(true);
    }
    
    private void handleInvalidSession(final Message<JsonObject> msg, final JsonObject event) {
        if(event.getBoolean("d")) {
            // Can resume
            if(socketRef.get() != null) {
                socketRef.get().close();
            }
        } else {
            // Can't resume, clear old data
            if(socketRef.get() != null) {
                socketRef.get().close();
                catnip.sessionManager().clearSession(id);
                catnip.sessionManager().clearSeqnum(id);
            }
        }
    }
    
    // Payloads
    
    private void handleReconnectRequest(final Message<JsonObject> msg, final JsonObject event) {
        // Just immediately disconnect
        if(socketRef.get() != null) {
            socketRef.get().close();
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
        return String.format("catnip:gateway:ws-incoming:%s:%s", id, op);
    }
    
    public String websocketMessageSendAddress() {
        return String.format("catnip:gateway:ws-outgoing:%s", id);
    }
    
    public String websocketMessageQueueAddress() {
        return String.format("catnip:gateway:ws-outgoing:%s:queue", id);
    }
    
    private String websocketMessagePollAddress() {
        return String.format("catnip:gateway:ws-outgoing:%s:poll", id);
    }
    
    private JsonObject identify() {
        return getBasePayload(GatewayOp.IDENTIFY, new JsonObject()
                .put("token", catnip.token())
                .put("compress", false)
                .put("large_threshold", 250)
                .put("shard", new JsonArray().add(id).add(limit))
                .put("properties", new JsonObject()
                        .put("$os", "JVM")
                        .put("$browser", "catnip")
                        .put("$device", "catnip")
                )
        );
    }
    
    private JsonObject resume() {
        return getBasePayload(GatewayOp.RESUME, new JsonObject()
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
    
    // Other
    
    public enum ShardConnectState {
        FAILED,
        READY,
        RESUMED,
    }
}
