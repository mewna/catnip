package com.mewna.catnip.entity.user;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mewna.catnip.entity.Entity;
import com.mewna.catnip.entity.guild.Member;
import com.mewna.catnip.entity.impl.VoiceStateImpl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A user's voice state.
 *
 * @author amy
 * @since 9/21/18.
 */
@JsonDeserialize(as = VoiceStateImpl.class)
public interface VoiceState extends Entity {
    /**
     * @return The id of the guild this voice state is for, if applicable.
     */
    @Nullable
    String guildId();
    
    /**
     * @return The channel the user is connected to, if applicable.
     */
    @Nullable
    String channelId();
    
    /**
     * @return The user's id.
     */
    @Nonnull
    String userId();
    
    /**
     * @return The guild member who owns the voice state.
     */
    @Nullable
    Member member();
    
    /**
     * @return The session id for this voice state. Only known for the current
     * user.
     */
    @Nullable
    String sessionId();
    
    /**
     * @return Whether the user has been deafened.
     */
    boolean deaf();
    
    /**
     * @return Whether the user has been muted.
     */
    boolean mute();
    
    /**
     * @return Whether the user has deafened themself.
     */
    boolean selfDeaf();
    
    /**
     * @return Whether the user has muted themself.
     */
    boolean selfMute();
    
    /**
     * @return Whether the user has been suppressed.
     */
    boolean suppress();
}
