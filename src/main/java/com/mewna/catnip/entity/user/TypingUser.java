package com.mewna.catnip.entity.user;

import com.mewna.catnip.entity.Snowflake;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author amy
 * @since 10/6/18.
 */
public interface TypingUser extends Snowflake {
    @Nonnull
    String channelId();
    
    @Nullable
    String guildId();
    
    @Nonnegative
    long timestamp();
}
