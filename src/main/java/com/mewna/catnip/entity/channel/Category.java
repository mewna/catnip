package com.mewna.catnip.entity.channel;

import javax.annotation.CheckReturnValue;

/**
 * A category that contains channels in a guild.
 *
 * @author natanbc
 * @since 9/12/18
 */
public interface Category extends GuildChannel {
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
        return true;
    }
}
