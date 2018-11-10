package com.mewna.catnip.entity.voice;

import com.mewna.catnip.entity.Entity;

import javax.annotation.Nonnull;

/**
 * Fired when connecting to voice, or when voice servers fail over.
 *
 * @author amy
 * @since 10/6/18.
 */
public interface VoiceServerUpdate extends Entity {
    /**
     * @return The token used for voice connections.
     */
    @Nonnull
    String token();
    
    /**
     * @return The id of the guild whose voice server this is.
     */
    @Nonnull
    String guildId();
    
    /**
     * @return The endpoint to open voice connections to.
     */
    @Nonnull
    String endpoint();
}
