package com.mewna.catnip.entity.message;

import com.mewna.catnip.entity.Entity;
import com.mewna.catnip.entity.misc.Emoji;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author amy
 * @since 10/6/18.
 */
public interface ReactionUpdate extends Entity {
    @Nonnull
    String userId();
    
    @Nonnull
    String channelId();
    
    @Nonnull
    String messageId();
    
    @Nullable
    String guildId();
    
    @Nonnull
    Emoji emoji();
}
