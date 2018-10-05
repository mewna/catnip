package com.mewna.catnip.entity.misc;

import com.mewna.catnip.entity.Entity;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * @author natanbc
 * @since 9/14/18
 */
public interface VoiceRegion extends Entity {
    @Nonnull
    @CheckReturnValue
    String id();
    
    @Nonnull
    @CheckReturnValue
    String name();
    
    @CheckReturnValue
    boolean vip();
    
    @CheckReturnValue
    boolean optimal();
    
    @CheckReturnValue
    boolean deprecated();
    
    @CheckReturnValue
    boolean custom();
}
