package com.mewna.catnip.entity.message;

import com.mewna.catnip.entity.Entity;
import com.mewna.catnip.entity.misc.Emoji;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Fired when a reaction is updated.
 *
 * @author amy
 * @since 10/6/18.
 */
public interface ReactionUpdate extends Entity {
    /**
     * @return The id of the user whose reaction was updated.
     */
    @Nonnull
    String userId();
    
    /**
     * @return The id of the channel the update is from.
     */
    @Nonnull
    String channelId();
    
    /**
     * @return The id of the message the update is from.
     */
    @Nonnull
    String messageId();
    
    /**
     * @return The id of the guild the update is from, if applicable.
     */
    @Nullable
    String guildId();
    
    /**
     * @return The emoji from the updated reaction.
     */
    @Nonnull
    Emoji emoji();
}
