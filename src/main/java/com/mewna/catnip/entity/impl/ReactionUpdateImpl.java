package com.mewna.catnip.entity.impl;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.RequiresCatnip;
import com.mewna.catnip.entity.message.ReactionUpdate;
import com.mewna.catnip.entity.misc.Emoji;
import lombok.*;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;

/**
 * @author amy
 * @since 10/6/18.
 */
@Getter
@Setter
@Builder
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public class ReactionUpdateImpl implements ReactionUpdate, RequiresCatnip {
    private transient Catnip catnip;
    
    private String userId;
    private String channelId;
    private String messageId;
    private String guildId;
    private Emoji emoji;
    
    public void catnip(@Nonnull final Catnip catnip) {
        this.catnip = catnip;
    }
}
