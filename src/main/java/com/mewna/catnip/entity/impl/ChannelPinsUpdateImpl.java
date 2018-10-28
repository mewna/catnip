package com.mewna.catnip.entity.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.RequiresCatnip;
import com.mewna.catnip.entity.Timestamped;
import com.mewna.catnip.entity.channel.ChannelPinsUpdate;
import lombok.*;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;

/**
 * @author amy
 * @since 10/9/18.
 */
@Getter(onMethod_ = @JsonProperty)
@Setter(onMethod_ = @JsonProperty)
@Builder
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public class ChannelPinsUpdateImpl implements ChannelPinsUpdate, RequiresCatnip, Timestamped {
    @JsonIgnore
    private transient Catnip catnip;
    
    private String channelId;
    private String lastPinTimestamp;
    
    @Nullable
    @Override
    public OffsetDateTime lastPinTimestamp() {
        return parseTimestamp(lastPinTimestamp);
    }
    
    @Override
    public void catnip(@Nonnull final Catnip catnip) {
        this.catnip = catnip;
    }
}
