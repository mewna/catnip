package com.mewna.catnip.entity.message;

import com.mewna.catnip.entity.Snowflake;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author amy
 * @since 10/4/18.
 */
public interface DeletedMessage extends Snowflake {
    @Nonnull
    String channelId();
    
    @Nullable
    String guildId();
}
