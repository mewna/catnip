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
    STOP,
    SHUTDOWN,
    TRACE,
    CONNECTED,
    LATENCY,
}
