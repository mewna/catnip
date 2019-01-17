package com.mewna.catnip.shard;

/**
 * Sent over the event bus for controlling shards / getting shard data.
 * Replaces the old {@link io.vertx.core.json.JsonObject}-based system.
 *
 * @author amy
 * @since 12/25/18
 */
public enum ShardControlMessage {
    /**
     * Start the shard.
     */
    START,
    
    /**
     * Stop the shard. Will allow it to reconnect itself.
     */
    STOP,
    
    /**
     * Stop the shard and undeploy the verticle.
     */
    SHUTDOWN,
    
    /**
     * Get the shard's trace. Used for debugging w/ Discord.
     */
    TRACE,
    
    /**
     * Get whether or not the shard is currently connected.
     */
    CONNECTED,
    
    /**
     * Get the shard's current heartbeat latency.
     */
    LATENCY,
}
