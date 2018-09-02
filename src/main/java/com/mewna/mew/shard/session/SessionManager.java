package com.mewna.mew.shard.session;

/**
 * @author amy
 * @since 8/16/18.
 */
public interface SessionManager {
    void storeSession(int shardId, String session);
    
    String getSession(int shardId);
    
    void storeSeqnum(int shardId, int seqnum);
    
    int getSeqnum(int shardId);
    
    void clearSession(int shardId);
    
    void clearSeqnum(int shardId);
}
