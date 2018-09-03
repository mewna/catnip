package com.mewna.catnip.shard.session;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author amy
 * @since 8/16/18.
 */
public interface SessionManager {
    void session(@Nonnegative int shardId, @Nonnull String session);
    
    @Nullable
    String session(@Nonnegative int shardId);
    
    void seqnum(@Nonnegative int shardId, int seqnum);
    
    int seqnum(@Nonnegative int shardId);
    
    void clearSession(@Nonnegative int shardId);
    
    void clearSeqnum(@Nonnegative int shardId);
}
