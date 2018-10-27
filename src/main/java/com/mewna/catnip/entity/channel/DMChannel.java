package com.mewna.catnip.entity.channel;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.annotation.CheckReturnValue;

/**
 * @author natanbc
 * @since 9/12/18
 */
public interface DMChannel extends MessageChannel {
    @Override
    @JsonIgnore
    @CheckReturnValue
    default boolean isDM() {
        return true;
    }
    
    @Override
    @JsonIgnore
    @CheckReturnValue
    default boolean isGuild() {
        return false;
    }
    
    @Override
    @JsonIgnore
    @CheckReturnValue
    default boolean isText() {
        return false;
    }
    
    @Override
    @JsonIgnore
    @CheckReturnValue
    default boolean isVoice() {
        return false;
    }
    
    @Override
    @JsonIgnore
    @CheckReturnValue
    default boolean isCategory() {
        return false;
    }
}
