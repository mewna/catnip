package com.mewna.catnip.entity.guild;

import com.mewna.catnip.entity.Entity;
import com.mewna.catnip.entity.user.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A single guild ban.
 *
 * @author amy
 * @since 10/6/18.
 */
public interface GuildBan extends Entity {
    /**
     * @return The user who was banned.
     */
    @Nonnull
    User user();
    
    /**
     * @return The reason for the ban, if provided.
     */
    @Nullable
    String reason();
}
