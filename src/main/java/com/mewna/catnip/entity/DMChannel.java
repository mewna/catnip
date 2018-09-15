package com.mewna.catnip.entity;

import javax.annotation.CheckReturnValue;

/**
 * @author natanbc
 * @since 9/12/18
 */
public interface DMChannel extends MessageChannel {
    @Override
    @CheckReturnValue
    default boolean isDM() {
        return true;
    }
    
    @Override
    @CheckReturnValue
    default boolean isGuild() {
        return false;
    }
    
    @Override
    @CheckReturnValue
    default boolean isText() {
        return false;
    }
    
    @Override
    @CheckReturnValue
    default boolean isVoice() {
        return false;
    }
    
    @Override
    @CheckReturnValue
    default boolean isCategory() {
        return false;
    }
}
