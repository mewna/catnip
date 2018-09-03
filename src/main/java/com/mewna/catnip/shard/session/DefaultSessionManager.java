package com.mewna.catnip.shard.session;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
    public void session(@Nonnegative final int shardId, @Nonnull final String session) {
        sessions.put(shardId, session);
    }
    
    @Override
    @Nullable
    public String session(@Nonnegative final int shardId) {
        return sessions.get(shardId);
    }
    
    @Override
    public void seqnum(@Nonnegative final int shardId, final int seqnum) {
        seqnums.put(shardId, seqnum);
    }
    
    @Override
    public int seqnum(@Nonnegative final int shardId) {
        return seqnums.get(shardId);
    }
    
    @Override
    public void clearSession(@Nonnegative final int shardId) {
        sessions.remove(shardId);
    }
    
    @Override
    public void clearSeqnum(@Nonnegative final int shardId) {
        seqnums.remove(shardId);
    }
}
