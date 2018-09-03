package com.mewna.catnip.shard.session;

/**
 * @author amy
 * @since 8/16/18.
 */
public interface SessionManager {
    void session(int shardId, String session);
    
    String session(int shardId);
    
    void seqnum(int shardId, int seqnum);
    
    int seqnum(int shardId);
    
    void clearSession(int shardId);
    
    void clearSeqnum(int shardId);
}
