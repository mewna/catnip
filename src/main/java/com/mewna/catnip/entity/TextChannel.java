package com.mewna.catnip.entity;

/**
 * @author natanbc
 * @since 9/12/18
 */
public interface TextChannel extends GuildChannel {
    String topic();
    boolean nsfw();
    
    @Override
    default boolean isText() {
        return true;
    }
    
    @Override
    default boolean isVoice() {
        return false;
    }
    
    @Override
    default boolean isCategory() {
        return false;
    }
}
