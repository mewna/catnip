package com.mewna.catnip.entity.impl;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.RequiresCatnip;
import com.mewna.catnip.entity.channel.ChannelPinsUpdate;
import lombok.*;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import java.time.OffsetDateTime;

/**
 * @author amy
 * @since 10/9/18.
 */
@Getter
@Setter
@Builder
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public class ChannelPinsUpdateImpl implements ChannelPinsUpdate, RequiresCatnip {
    private transient Catnip catnip;
    
    private String channelId;
    private OffsetDateTime lastPinTimestamp;
    
    @Override
    public void catnip(@Nonnull final Catnip catnip) {
        this.catnip = catnip;
    }
}
