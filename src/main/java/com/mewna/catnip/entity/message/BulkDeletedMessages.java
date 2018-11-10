package com.mewna.catnip.entity.message;

import com.mewna.catnip.entity.Entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Fired over the event bus when messages are bulk deleted.
 *
 * @author amy
 * @since 10/4/18.
 */
public interface BulkDeletedMessages extends Entity {
    /**
     * @return The ids of the messages that were deleted.
     */
    @Nonnull
    List<String> ids();
    
    /**
     * @return The id of the channel the messages were deleted in.
     */
    @Nonnull
    String channelId();
    
    /**
     * @return The id of the guild the messages were deleted in, if applicable.
     */
    @Nullable
    String guildId();
}
