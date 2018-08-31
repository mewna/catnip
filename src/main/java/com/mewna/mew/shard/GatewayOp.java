package com.mewna.mew.shard;

import lombok.Getter;

/**
 * @author amy
 * @since 8/15/18.
 */
public enum GatewayOp {
    DISPATCH(0),
    HEARTBEAT(1),
    IDENTIFY(2),
    STATUS_UPDATE(3),
    VOICE_STATE_UPDATE(4),
    VOICE_SERVER_PING(5),
    RESUME(6),
    RECONNECT(7),
    REQUEST_GUILD_MEMBERS(8),
    INVALID_SESSION(9),
    HELLO(10),
    HEARTBEAT_ACK(11),
    ;
    @Getter
    private final int op;
    
    GatewayOp(final int op) {
        this.op = op;
    }
    
    @Override
    public String toString() {
        return name();
    }
    
    public static GatewayOp getById(final int id) {
        return values()[id];
    }
}
