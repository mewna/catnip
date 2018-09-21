package com.mewna.catnip.entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author amy
 * @since 9/21/18.
 */
public interface VoiceState {
    @Nullable
    String guildId();
    
    @Nonnull
    String channelId();
    
    @Nonnull
    String userId();
    
    @Nullable
    Member member();
    
    @Nullable
    String sessionId();
    
    boolean deaf();
    
    boolean mute();
    
    boolean selfDeaf();
    
    boolean selfMute();
    
    boolean suppress();
}
