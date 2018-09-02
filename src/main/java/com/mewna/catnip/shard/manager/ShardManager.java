package com.mewna.catnip.shard.manager;

import com.mewna.catnip.Catnip;

/**
 * @author amy
 * @since 8/15/18.
 */
public interface ShardManager {
    String CONNECT_QUEUE = "flow:gateway:connect-queue";
    
    int getShardCount();
    
    void start();
    
    void addToConnectQueue(int shard);
    
    Catnip getCatnip();
    
    void setCatnip(Catnip flow);
}
