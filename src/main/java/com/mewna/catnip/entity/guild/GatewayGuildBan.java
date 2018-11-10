package com.mewna.catnip.entity.guild;

import com.mewna.catnip.entity.Entity;
import com.mewna.catnip.entity.user.User;

import javax.annotation.Nonnull;

/**
 * Fired over the event bus when a user is banned in a guild.
 *
 * @author amy
 * @since 10/6/18.
 */
public interface GatewayGuildBan extends Entity {
    /**
     * @return The id of the guild where the user was banned.
     */
    @Nonnull
    String guildId();
    
    /**
     * @return The user who was banned.
     */
    @Nonnull
    User user();
}
