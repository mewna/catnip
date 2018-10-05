package com.mewna.catnip.entity;

import com.mewna.catnip.entity.impl.RequiresCatnip;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author amy
 * @since 10/4/18.
 */
public interface DeletedMessage extends Snowflake, RequiresCatnip {
    @Nonnull
    String channelId();
    
    @Nullable
    String guildId();
}
