package com.mewna.catnip.entity.misc;

import com.mewna.catnip.entity.Snowflake;
import com.mewna.catnip.entity.user.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Information about an application.
 *
 * @author amy
 * @since 10/17/18.
 */
public interface ApplicationInfo extends Snowflake {
    /**
     * @return The application's name.
     */
    @Nonnull
    String name();
    
    /**
     * @return The application's icon.
     */
    @Nullable
    String icon();
    
    /**
     * @return The application's description.
     */
    @Nullable
    String description();
    
    /**
     * @return A non-{@code null}, possibly-empty list of the application's RPC
     * origins.
     */
    @Nonnull
    List<String> rpcOrigins();
    
    /**
     * @return Whether or not the application is a public bot.
     */
    boolean publicBot();
    
    /**
     * @return Whether or not the application requires a code grant before the
     * bot can be added.
     */
    boolean requiresCodeGrant();
    
    /**
     * @return The user who owns the application.
     */
    @Nonnull
    User owner();
}
