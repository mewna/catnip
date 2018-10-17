package com.mewna.catnip.shard;

import static com.mewna.catnip.shard.EventTypeImpl.event;

/**
 * @author amy
 * @since 10/17/18.
 */
public interface LifecycleEvent {
    // @formatter:off
    EventType<ShardInfo> CONNECTING   = event(Raw.CONNECTING, ShardInfo.class);
    EventType<ShardInfo> CONNECTED    = event(Raw.CONNECTED, ShardInfo.class);
    EventType<ShardInfo> DISCONNECTED = event(Raw.DISCONNECTED, ShardInfo.class);
    EventType<ShardInfo> IDENTIFIED   = event(Raw.IDENTIFIED, ShardInfo.class);
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
