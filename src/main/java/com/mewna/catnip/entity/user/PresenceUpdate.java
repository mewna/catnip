package com.mewna.catnip.entity.user;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

/**
 * @author natanbc
 * @since 12/14/18
 */
public interface PresenceUpdate extends Presence {
    /**
     * @return ID of the user.
     */
    @Nonnull
    String id();
    
    /**
     * @return ID of the guild.
     */
    @Nonnull
    String guildId();
    
    /**
     * @return Roles the user has.
     */
    @Nonnull
    Set<String> roles();
    
    /**
     * @return Nickname of the user.
     */
    @Nullable
    String nick();
    
    /**
     * @return Status reported for the user's mobile device.
     */
    @Nullable
    String mobileStatus();
}
