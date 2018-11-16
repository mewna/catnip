package com.mewna.catnip.entity.user;

import com.mewna.catnip.entity.Snowflake;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Fired when a user starts typing in a channel.
 *
 * @author amy
 * @since 10/6/18.
 */
public interface TypingUser extends Snowflake {
    /**
     * @return The id of the channel being typed in.
     */
    @Nonnull
    String channelId();
    
    /**
     * @return The id of the guild being typed in, if applicable.
     */
    @Nullable
    String guildId();
    
    /**
     * @return The time the typing started at.
     */
    @Nonnegative
    long timestamp();
}
