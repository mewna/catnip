package com.mewna.catnip.entity.guild;

import com.mewna.catnip.entity.Entity;
import com.mewna.catnip.entity.misc.Emoji.CustomEmoji;

import java.util.List;

/**
 * @author amy
 * @since 10/9/18.
 */
public interface EmojiUpdate extends Entity {
    String guildId();
    
    List<CustomEmoji> emoji();
}
