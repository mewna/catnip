package com.mewna.catnip.entity.message;

import com.mewna.catnip.entity.Entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Fired over the event bus when all reactions are removed from a message.
 *
 * @author amy
 * @since 10/6/18.
 */
public interface BulkRemovedReactions extends Entity {
    /**
     * @return The id of the channel where reactions were removed.
     */
    @Nonnull
    String channelId();
    
    /**
     * @return The id of the messages whose reactions were removed.
     */
    @Nonnull
    String messageId();
    
    /**
     * @return The id of the guild where reactions were removed, if applicable.
     */
    @Nullable
    String guildId();
}
