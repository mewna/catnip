package com.mewna.catnip.entity.voice;

import com.mewna.catnip.entity.Entity;

import javax.annotation.Nonnull;

/**
 * @author amy
 * @since 10/6/18.
 */
public interface VoiceServerUpdate extends Entity {
    @Nonnull
    String token();
    
    @Nonnull
    String guildId();
    
    @Nonnull
    String endpoint();
}
