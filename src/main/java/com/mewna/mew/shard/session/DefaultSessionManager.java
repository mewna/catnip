package com.mewna.mew.shard.session;

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
    public void storeSession(final int shardId, final String session) {
        sessions.put(shardId, session);
    }
    
    @Override
    public String getSession(final int shardId) {
        return sessions.get(shardId);
    }
    
    @Override
    public void storeSeqnum(final int shardId, final int seqnum) {
        seqnums.put(shardId, seqnum);
    }
    
    @Override
    public int getSeqnum(final int shardId) {
        return seqnums.get(shardId);
    }
}
