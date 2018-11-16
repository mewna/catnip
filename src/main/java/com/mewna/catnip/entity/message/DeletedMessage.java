package com.mewna.catnip.entity.message;

import com.mewna.catnip.entity.Snowflake;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Fired when a message is deleted.
 *
 * @author amy
 * @since 10/4/18.
 */
public interface DeletedMessage extends Snowflake {
    /**
     * @return The id of the channel where it was deleted.
     */
    @Nonnull
    String channelId();
    
    /**
     * @return The id of the guild where it was deleted, if applicable.
     */
    @Nullable
    String guildId();
}
