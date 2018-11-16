package com.mewna.catnip.entity.guild;

import com.mewna.catnip.entity.RequiresCatnip;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;

/**
 * A guild's embed.
 *
 * @author SamOphis
 * @since 10/18/2018
 */
@SuppressWarnings("unused")
public interface GuildEmbed extends RequiresCatnip {
    /**
     * @return Whether the embed is enabled.
     */
    @CheckReturnValue
    boolean enabled();
    
    /**
     * @return The id the embed is enabled for.
     */
    @Nullable
    @CheckReturnValue
    String channelId();
}
