package com.mewna.catnip.entity.channel;

import javax.annotation.Nonnull;

/**
 * @author Alula
 * @since 7/14/19.
 */
public interface LookingForGameChannel extends GuildChannel {
    @Nonnull
    @Override
    default ChannelType type() {
        return ChannelType.LOOKING_FOR_GAME;
    }

    boolean nsfw();

    // TODO: implement APIs
}
