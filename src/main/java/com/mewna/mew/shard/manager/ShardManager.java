package com.mewna.mew.shard.manager;

import com.mewna.mew.Mew;

import java.util.Deque;

/**
 * @author amy
 * @since 8/15/18.
 */
public interface ShardManager {
    String CONNECT_QUEUE = "flow:gateway:connect-queue";
    
    int getShardCount();
    
    void start();
    
    void addToConnectQueue(int shard);
    
    Mew getMew();
    
    void setMew(Mew flow);
}
