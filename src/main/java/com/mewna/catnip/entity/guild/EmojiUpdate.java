package com.mewna.catnip.entity.guild;

import com.mewna.catnip.entity.Entity;
import com.mewna.catnip.entity.misc.Emoji.CustomEmoji;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.List;

/**
 * Fired over the event bus when a guild's emojis are updated.
 *
 * @author amy
 * @since 10/9/18.
 */
public interface EmojiUpdate extends Entity {
    /**
     * @return The id of the guild whose emojis were updated.
     */
    @Nonnull
    String guildId();
    
    /**
     * @return A non-{@code null}, possibly-empty list of the guild's emojis.
     */
    @Nonnull
    @CheckReturnValue
    List<CustomEmoji> emojis();
}
