package com.mewna.catnip.shard.manager;

import com.mewna.catnip.Catnip;
import io.vertx.core.Future;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author amy
 * @since 8/15/18.
 */
public interface ShardManager {
    int shardCount();
    
    void start();
    
    void addToConnectQueue(@Nonnegative int shard);
    
    @Nonnull
    Future<List<String>> trace(@Nonnegative int shard);
    
    @Nonnull
    Catnip catnip();
    
    @Nonnull
    ShardManager catnip(@Nonnull Catnip catnip);
}
