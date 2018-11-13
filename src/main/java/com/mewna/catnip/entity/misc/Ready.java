package com.mewna.catnip.entity.misc;

import com.mewna.catnip.entity.Entity;
import com.mewna.catnip.entity.user.User;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.List;

/**
 * Fired when a shard logs in successfully.
 *
 * @author amy
 * @since 10/4/18.
 */
public interface Ready extends Entity {
    /**
     * @return The websocket gateway version.
     */
    @Nonnegative
    int version();
    
    /**
     * @return The user who logged in.
     */
    @Nonnull
    User user();
    
    /**
     * @return Debugging trace.
     */
    @Nonnull
    List<String> trace();
}
