package com.mewna.catnip.entity;

import javax.annotation.Nonnull;
import java.time.OffsetDateTime;
import java.util.Set;

/**
 * @author amy
 * @since 9/4/18.
 */
@SuppressWarnings("unused")
public interface Member {
    @Nonnull
    String id();
    
    @Nonnull
    String nick();
    
    @Nonnull
    Set<String> roles();
    
    boolean mute();
    
    boolean deaf();
    
    @Nonnull
    OffsetDateTime joinedAt();
}
