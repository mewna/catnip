package com.mewna.catnip.entity.channel;

import javax.annotation.CheckReturnValue;

/**
 * @author natanbc
 * @since 9/12/18
 */
public interface VoiceChannel extends GuildChannel {
    @CheckReturnValue
    int bitrate();
    
    @CheckReturnValue
    int userLimit();
    
    @Override
    @CheckReturnValue
    default boolean isText() {
        return false;
    }
    
    @Override
    @CheckReturnValue
    default boolean isVoice() {
        return true;
    }
    
    @Override
    @CheckReturnValue
    default boolean isCategory() {
        return false;
    }
}
