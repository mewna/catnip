package com.mewna.catnip.shard.manager;

import com.mewna.catnip.Catnip;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * @author amy
 * @since 8/15/18.
 */
public interface ShardManager {
    int shardCount();
    
    void start();
    
    void addToConnectQueue(@Nonnegative int shard);
    
    @Nonnull
    Catnip catnip();
    
    ShardManager catnip(@Nonnull Catnip catnip);
}
