package com.mewna.catnip.entity;

import com.mewna.catnip.entity.channel.TextChannel;
import com.mewna.catnip.entity.guild.Member;
import com.mewna.catnip.entity.user.User;

/**
 * An entity that can be mentioned. {@link User}s, {@link TextChannel}s, and
 * {@link Member}s are all examples of mentionable entities.
 *
 * @author Nik Ammerlaan
 * @since 2/3/19.
 */
@FunctionalInterface
public interface Mentionable {
    /**
     * @return A mention for this entity that can be sent in a message.
     */
    String asMention();
}
