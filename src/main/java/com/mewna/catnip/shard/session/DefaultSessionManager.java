package com.mewna.catnip.shard.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author amy
 * @since 8/16/18.
 */
public class DefaultSessionManager implements SessionManager {
    private final Map<Integer, String> sessions = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> seqnums = new ConcurrentHashMap<>();
    
    @Override
    public void session(final int shardId, final String session) {
        sessions.put(shardId, session);
    }
    
    @Override
    public String session(final int shardId) {
        return sessions.get(shardId);
    }
    
    @Override
    public void seqnum(final int shardId, final int seqnum) {
        seqnums.put(shardId, seqnum);
    }
    
    @Override
    public int seqnum(final int shardId) {
        return seqnums.get(shardId);
    }
    
    @Override
    public void clearSession(final int shardId) {
        sessions.remove(shardId);
    }
    
    @Override
    public void clearSeqnum(final int shardId) {
        seqnums.remove(shardId);
    }
}
