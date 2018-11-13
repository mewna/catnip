package com.mewna.catnip.entity.channel;

import com.mewna.catnip.entity.Entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;

/**
 * Fired over the event bus when a channel's pins are updated.
 *
 * @author amy
 * @since 10/9/18.
 */
public interface ChannelPinsUpdate extends Entity {
    /**
     * @return The id of the channel that pins were updated in. Will never be
     * {@code null}.
     */
    @Nonnull
    String channelId();
    
    /**
     * @return The timestamp of the last pinned message in the channel. May be
     * {@code null}.
     */
    @Nullable
    OffsetDateTime lastPinTimestamp();
}
