package com.mewna.catnip.entity;

import com.mewna.catnip.entity.util.ImageOptions;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

/**
 * @author amy
 * @since 9/4/18
 */
public interface User extends Snowflake {
    /**
     * Whether the user's avatar is animated.
     *
     * @return True if the avatar is animated, false otherwise.
     */
    @CheckReturnValue
    boolean isAvatarAnimated();
    
    /**
     * The URL for the default avatar for this user.
     *
     * @return String containing the URL to the default avatar. Never null.
     */
    @Nonnull
    @CheckReturnValue
    String defaultAvatarUrl();
    
    /**
     * The URL for the user's set avatar. Can be null if the user has not set an avatar.
     *
     * @see User#defaultAvatarUrl() Getting the user's default fallback avatar
     * @see User#effectiveAvatarUrl() Getting the user's effective avatar
     *
     * @param options {@link ImageOptions Image Options}.
     *
     * @return String containing the URL to their avatar, options considered. Can be null.
     */
    @Nullable
    @CheckReturnValue
    String avatarUrl(@Nonnull final ImageOptions options);
    
    /**
     * The URL for the user's set avatar. Can be null if the user has not set an avatar.
     *
     * @see User#defaultAvatarUrl() Getting the user's default fallback avatar
     * @see User#effectiveAvatarUrl() Getting the user's effective avatar
     *
     * @return String containing the URL to their avatar. Can be null.
     */
    @Nullable
    @CheckReturnValue
    String avatarUrl();
    
    /**
     * The URL for the user's effective avatar, as displayed in the Discord client.
     * <br>Convenience method for getting the user's default avatar
     * when {@link User#avatarUrl()} is null.
     *
     * @param options {@link ImageOptions Image Options}.
     *
     * @return String containing a URL to their effective avatar, options considered. Never null.
     */
    @Nonnull
    @CheckReturnValue
    String effectiveAvatarUrl(@Nonnull final ImageOptions options);
    
    /**
     * The URL for the user's effective avatar, as displayed in the Discord client.
     * <br>Convenience method for getting the user's default avatar
     * when {@link User#avatarUrl()} is null.
     *
     * @return String containing a URL to their effective avatar. Never null.
     */
    @Nonnull
    @CheckReturnValue
    String effectiveAvatarUrl();
    
    /**
     * The username of the user.
     *
     * @return User's name. Never null.
     */
    @Nonnull
    @CheckReturnValue
    String username();
    
    /**
     * The unique snowflake ID of the user.
     *
     * @return User's ID. never null.
     */
    @Nonnull
    @CheckReturnValue
    String id();
    
    /**
     * Discriminator of the user, used to tell Amy#0001 from Amy#0002.
     *
     * @return 4 digit discriminator as a string. Never null.
     */
    @Nonnull
    @CheckReturnValue
    String discriminator();
    
    /**
     * User's avatar hash.
     * <br><b>This does not return their avatar URL nor image directly.</b>
     *
     * @see User#avatarUrl() Getting the user's avatar
     *
     * @return User's hashed avatar string. Can be null.
     */
    @Nullable
    @CheckReturnValue
    String avatar();
    
    /**
     * Whether the user is a bot, or webhook/fake user.
     *
     * @return True if the user is a bot, false if the user is a human.
     */
    @CheckReturnValue
    boolean bot();
    
    /**
     * Creates a DM channel with this user.
     *
     * @return Future with the result of the DM creation.
     */
    default CompletableFuture<DMChannel> createDM() {
        return catnip().rest().channel().createDM(id());
    }
}
