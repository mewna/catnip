package com.mewna.catnip.shard;

import static com.mewna.catnip.shard.EventTypeImpl.event;

/**
 * @author amy
 * @since 10/17/18.
 */
public interface LifecycleEvent {
    // @formatter:off
    /**
     * Fired when the shard is created and is about to connect to the websocket
     * gateway. The payload is a shard id / total pair.
     */
    EventType<ShardInfo> CONNECTING   = event(Raw.CONNECTING, ShardInfo.class);
    /**
     * Fired when the shard has connected to the websocket gateway, but has not
     * yet sent an IDENTIFY payload. The payload is a shard id / total pair.
     */
    EventType<ShardInfo> CONNECTED    = event(Raw.CONNECTED, ShardInfo.class);
    /**
     * Fired when the shard has disconnected from the websocket gateway, and
     * will (hopefully) be reconnecting. The payload is a shard id / total
     * pair.
     */
    EventType<ShardInfo> DISCONNECTED = event(Raw.DISCONNECTED, ShardInfo.class);
    /**
     * Fired when the shard has successfully IDENTIFYd with the websocket
     * gateway. This is effectively the same as listening on
     * {@link DiscordEvent#READY}. The payload is a shard id / total pair.
     */
    EventType<ShardInfo> IDENTIFIED   = event(Raw.IDENTIFIED, ShardInfo.class);
    /**
     * Fired when the shard has successfully RESUMEd with the websocket
     * gateway. The payload is a shard id / total pair.
     */
    EventType<ShardInfo> RESUMED      = event(Raw.RESUMED, ShardInfo.class);
    // @formatter:on
    
    interface Raw {
        // @formatter:off
        String CONNECTING   = "CONNECTING";
        String CONNECTED    = "CONNECTED";
        String DISCONNECTED = "DISCONNECTED";
        String IDENTIFIED   = "IDENTIFIED";
        String RESUMED      = "RESUMED";
        // @formatter:on
    }
}
