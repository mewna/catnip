package com.mewna.catnip.shard.manager;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.internal.CatnipImpl;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * @author amy
 * @since 8/15/18.
 */
public interface ShardManager {
    String CONNECT_QUEUE = "flow:gateway:connect-queue";
    
    int getShardCount();
    
    void start();
    
    void addToConnectQueue(@Nonnegative int shard);
    
    Catnip getCatnip();
    
    void setCatnip(@Nonnull Catnip catnip);
}
