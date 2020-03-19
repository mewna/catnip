package com.mewna.catnip.entity.impl.guild.audit;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.RequiresCatnip;
import com.mewna.catnip.entity.guild.audit.MessagePinInfo;
import lombok.*;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;

/**
 * @author kjp12
 * @since March 18th, 2020
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(fluent = true)
public class MessagePinInfoImpl implements MessagePinInfo, RequiresCatnip {
    private transient Catnip catnip;
    
    private long channelIdAsLong;
    private long messageIdAsLong;
    
    @Override
    public void catnip(@Nonnull final Catnip catnip) {
        this.catnip = catnip;
    }
}
