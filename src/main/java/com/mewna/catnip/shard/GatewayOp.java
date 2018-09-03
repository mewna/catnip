package com.mewna.catnip.shard;

import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * @author amy
 * @since 8/15/18.
 */
@Accessors(fluent = true)
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
    private final int opcode;
    
    GatewayOp(final int opcode) {
        this.opcode = opcode;
    }
    
    @Override
    public String toString() {
        return name();
    }
    
    public static GatewayOp byId(final int id) {
        return values()[id];
    }
}
