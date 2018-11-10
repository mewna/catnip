package com.mewna.catnip.entity.misc;

import com.mewna.catnip.entity.Entity;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * A voice region on Discord.
 *
 * @author natanbc
 * @since 9/14/18
 */
public interface VoiceRegion extends Entity {
    /**
     * @return The id of the voice region.
     */
    @Nonnull
    @CheckReturnValue
    String id();
    
    /**
     * @return The name of the voice region.
     */
    @Nonnull
    @CheckReturnValue
    String name();
    
    /**
     * @return Whether the voice region is VIP-only.
     */
    @CheckReturnValue
    boolean vip();
    
    /**
     * @return Whether the voice region is optimal.
     */
    @CheckReturnValue
    boolean optimal();
    
    /**
     * @return Whether the voice region is deprecated.
     */
    @CheckReturnValue
    boolean deprecated();
    
    /**
     * @return Whether the voice region is custom.
     */
    @CheckReturnValue
    boolean custom();
}
