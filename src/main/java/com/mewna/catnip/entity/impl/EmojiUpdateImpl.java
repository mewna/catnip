package com.mewna.catnip.entity.impl;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.RequiresCatnip;
import com.mewna.catnip.entity.guild.EmojiUpdate;
import com.mewna.catnip.entity.misc.Emoji.CustomEmoji;
import lombok.*;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import java.util.List;

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
public class EmojiUpdateImpl implements EmojiUpdate, RequiresCatnip {
    private transient Catnip catnip;
    
    private String guildId;
    private List<CustomEmoji> emojis;
    
    @Override
    public void catnip(@Nonnull final Catnip catnip) {
        this.catnip = catnip;
    }
}
