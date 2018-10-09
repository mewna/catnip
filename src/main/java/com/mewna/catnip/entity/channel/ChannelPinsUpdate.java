package com.mewna.catnip.entity.channel;

import com.mewna.catnip.entity.Entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;

/**
 * @author amy
 * @since 10/9/18.
 */
public interface ChannelPinsUpdate extends Entity {
    @Nonnull
    String channelId();
    
    @Nullable
    OffsetDateTime lastPinTimestamp();
}
