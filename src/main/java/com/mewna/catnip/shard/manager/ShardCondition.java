package com.mewna.catnip.shard.manager;

import com.mewna.catnip.shard.CatnipShard.ShardConnectState;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

/**
 * A shard condition is an async function that determines whether or not the
 * current shard manager instance can start sharding, ie. all shard conditions
 * must be {@code true} for the shard manager to start shards.
 *
 * @author amy
 * @since 11/14/18.
 */
public interface ShardCondition {
    /**
     * Get the future for this shard condition. This function is called
     * ASYNCHRONOUSLY and must be ASYNCHRONOUS to avoid blocking the vert.x
     * event loop threads.
     * <p/>
     * This is run prior to sharding.
     *
     * @return The future for this shard condition.
     */
    CompletableFuture<Boolean> preshard();
    
    /**
     * Run once sharding has finished. This function is called SYNCHRONOUSLY.
     *
     * @param state The state the shard connected with
     */
    void postshard(@Nonnull ShardConnectState state);
}
