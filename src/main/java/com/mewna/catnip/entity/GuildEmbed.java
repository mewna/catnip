package com.mewna.catnip.entity;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;

/**
 * @author SamOphis
 * @since 10/18/2018
 */

@SuppressWarnings("unused")
public interface GuildEmbed extends RequiresCatnip {
    @CheckReturnValue
    boolean enabled();
    
    @Nullable
    @CheckReturnValue
    String channelId();
}
