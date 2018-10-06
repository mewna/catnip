package com.mewna.catnip.entity.message;

import com.mewna.catnip.entity.Entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author amy
 * @since 10/6/18.
 */
public interface BulkRemovedReactions extends Entity {
    @Nonnull
    String channelId();
    
    @Nonnull
    String messageId();
    
    @Nullable
    String guildId();
}
