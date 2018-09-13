package com.mewna.catnip.entity;

/**
 * @author natanbc
 * @since 9/12/18
 */
public interface VoiceChannel extends GuildChannel {
    int bitrate();
    int userLimit();
    
    @Override
    default boolean isText() {
        return false;
    }
    
    @Override
    default boolean isVoice() {
        return true;
    }
    
    @Override
    default boolean isCategory() {
        return false;
    }
}
