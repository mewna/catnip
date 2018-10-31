package com.mewna.catnip.entity.user;

import com.mewna.catnip.entity.Entity;
import com.mewna.catnip.entity.guild.Member;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author amy
 * @since 9/21/18.
 */
public interface VoiceState extends Entity {
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
