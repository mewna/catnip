package com.mewna.catnip.entity.impl;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.GuildEmbed;
import lombok.*;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;

/**
 * @author SamOphis
 * @since 10/18/2018
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(fluent = true, chain = true)
public class GuildEmbedImpl implements GuildEmbed {
    private transient Catnip catnip;
    
    private String channelId;
    private boolean enabled;
    
    @Override
    public void catnip(@Nonnull final Catnip catnip) {
        this.catnip = catnip;
    }
}
