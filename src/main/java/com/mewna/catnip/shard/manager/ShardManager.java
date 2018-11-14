package com.mewna.catnip.shard.manager;

import com.mewna.catnip.Catnip;
import io.vertx.core.Future;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

/**
 * Manages the lifecycle of shards - starting, stopping, resuming, etc.
 *
 * @author amy
 * @since 8/15/18.
 */
public interface ShardManager {
    /**
     * @return The number of shards this shard manager owns.
     */
    @Nonnegative
    int shardCount();
    
    /**
     * @return The collection of shard ids owned by this shard manager
     * instance.
     */
    Collection<Integer> shardIds();
    
    /**
     * Starts booting shards.
     */
    void start();
    
    /**
     * Adds the given shard id to the connect queue.
     * @param shard The shard id to add.
     */
    void addToConnectQueue(@Nonnegative int shard);
    
    /**
     * Fetches the trace from the given shard.
     * @param shard The shard to fetch the trace from.
     * @return The shard's trace.
     */
    @Nonnull
    Future<List<String>> trace(@Nonnegative int shard);
    
    /**
     * @return The catnip instance this shard manager is for.
     */
    @Nonnull
    Catnip catnip();
    
    @Nonnull
    ShardManager catnip(@Nonnull Catnip catnip);
    
    /**
     * Shuts down all shards.
     */
    void shutdown();
}
