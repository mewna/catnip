package com.mewna.mew.shard;

import com.mewna.mew.Mew;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebSocketFrame;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.mewna.mew.shard.MewShard.ShardConnectState.*;

/**
 * @author amy
 * @since 8/31/18.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
@RequiredArgsConstructor
public class MewShard extends AbstractVerticle {
    private final Mew mew;
    private final int id;
    private final int limit;
    
    private final HttpClient client = Mew.vertx().createHttpClient();
    
    private final AtomicReference<WebSocket> socketRef = new AtomicReference<>(null);
    private final AtomicBoolean heartbeatAcked = new AtomicBoolean(true);
    
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private final DispatchEmitter emitter = new DispatchEmitter();
    
    @Override
    public void start() {
        Mew.eventBus().consumer(getControlAddress(id), this::handleControlMessage);
        Mew.eventBus().consumer(getWebsocketMessageSendAddress(), this::handleSocketSend);
    }
    
    @Override
    public void stop() {
    }
    
    // Control
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
        heartbeatAcked.set(true);
    }
    
    // Socket
    
    private void connectSocket(final Message<JsonObject> msg) {
        client.websocketAbs(Mew.getGatewayUrl(), null, null, null,
                socket -> {
                    socket.frameHandler(frame -> handleSocketFrame(msg, frame));
                    socket.closeHandler(this::handleSocketClose);
                    socketRef.set(socket);
                },
                failure -> {
                    socketRef.set(null);
                    Mew.vertx().setTimer(5500L, __ -> msg.reply(new JsonObject().put("state", FAILED.name())));
                });
    }
    
    private void handleSocketFrame(final Message<JsonObject> msg, final WebSocketFrame frame) {
        // TODO: Support zlib
        try {
            if(frame.isText()) {
                final JsonObject event = new JsonObject(frame.textData());
                final GatewayOp op = GatewayOp.getById(event.getInteger("op"));
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
                Mew.eventBus().<JsonObject>send(getWebsocketMessageRecvAddress(op), event);
            }
        } catch(final DecodeException e) {
            e.printStackTrace();
        }
    }
    
    private void handleSocketClose(final Void __) {
        socketRef.set(null);
        mew.shardManager().addToConnectQueue(id);
    }
    
    private void handleSocketSend(final Message<JsonObject> msg) {
        final WebSocket webSocket = socketRef.get();
        if(webSocket != null) {
            webSocket.writeTextMessage(msg.body().encode());
        }
    }
    
    // Payload handling
    
    private void handleHello(final Message<JsonObject> msg, final JsonObject event) {
        final JsonObject payload = event.getJsonObject("d");
        // TODO: Handle trace here
        
        Mew.vertx().setPeriodic(payload.getInteger("heartbeat_interval"), timerId -> {
            if(socketRef.get() != null) {
                if(!heartbeatAcked.get()) {
                    // Zombie
                    logger.warn("Shard {} zombied, queueing reconnect!", id);
                    Mew.eventBus().send(getControlAddress(id), new JsonObject().put("mode", "STOP"));
                    return;
                }
                Mew.eventBus().send(getWebsocketMessageSendAddress(),
                        getBasePayload(GatewayOp.HEARTBEAT, mew.sessionManager().getSeqnum(id)));
                heartbeatAcked.set(false);
            } else {
                Mew.vertx().cancelTimer(timerId);
            }
        });
        
        // Check if we can RESUME instead
        if(mew.sessionManager().getSession(id) != null && mew.sessionManager().getSeqnum(id) > 0) {
            Mew.eventBus().send(getWebsocketMessageSendAddress(), resume());
        } else {
            Mew.eventBus().send(getWebsocketMessageSendAddress(), identify());
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
            mew.sessionManager().storeSeqnum(id, event.getInteger("s"));
        }
        
        switch(type) {
            case "READY": {
                // Reply after IDENTIFY ratelimit
                mew.sessionManager().storeSession(id, data.getString("session_id"));
                Mew.vertx().setTimer(5500L, __ -> msg.reply(new JsonObject().put("state", READY.name())));
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
        //Mew.eventBus().send(type, data);
    }
    
    private void handleHeartbeat(final Message<JsonObject> msg, final JsonObject event) {
        //heartbeatAcked.set(false);
        Mew.eventBus().send(getWebsocketMessageSendAddress(),
                getBasePayload(GatewayOp.HEARTBEAT, mew.sessionManager().getSeqnum(id)));
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
                mew.sessionManager().clearSession(id);
                mew.sessionManager().clearSeqnum(id);
            }
        }
    }
    
    private void handleReconnectRequest(final Message<JsonObject> msg, final JsonObject event) {
        // Just immediately disconnect
        if(socketRef.get() != null) {
            socketRef.get().close();
        }
    }
    
    // Addresses
    
    /**
     * Shard control - start, stop, etc
     *
     * @param id Shard ID
     *
     * @return Control address for the given shard
     */
    public static String getControlAddress(final int id) {
        return String.format("mew:shard:%s:control", id);
    }
    
    /**
     * Incoming socket payloads - DISPATCH, HEARTBEAT, etc
     *
     * @param op Gateway op
     *
     * @return Socket payload recv. msg. address
     */
    public String getWebsocketMessageRecvAddress(final GatewayOp op) {
        return String.format("mew:gateway:ws-incoming:%s:%s", id, op);
    }
    
    public String getWebsocketMessageSendAddress() {
        return String.format("mew:gateway:ws-outgoing:%s", id);
    }
    
    // Payloads
    
    public static JsonObject getBasePayload(final GatewayOp op) {
        return getBasePayload(op, (JsonObject) null);
    }
    
    public static JsonObject getBasePayload(final GatewayOp op, final JsonObject payload) {
        return new JsonObject()
                .put("op", op.getOp())
                .put("d", payload)
                ;
    }
    
    public static JsonObject getBasePayload(final GatewayOp op, final Integer payload) {
        return new JsonObject()
                .put("op", op.getOp())
                .put("d", payload)
                ;
    }
    
    private JsonObject identify() {
        return getBasePayload(GatewayOp.IDENTIFY, new JsonObject()
                .put("token", mew.token())
                .put("compress", false)
                .put("large_threshold", 250)
                .put("shard", new JsonArray().add(id).add(limit))
                .put("properties", new JsonObject()
                        .put("$os", "JVM")
                        .put("$browser", "mew")
                        .put("$device", "mew")
                )
        );
    }
    
    private JsonObject resume() {
        return getBasePayload(GatewayOp.RESUME, new JsonObject()
                .put("token", mew.token())
                .put("compress", false)
                .put("session_id", mew.sessionManager().getSession(id))
                .put("seq", mew.sessionManager().getSeqnum(id))
                .put("properties", new JsonObject()
                        .put("$os", "JVM")
                        .put("$browser", "mew")
                        .put("$device", "mew")
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
