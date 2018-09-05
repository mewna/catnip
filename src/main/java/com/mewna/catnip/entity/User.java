package com.mewna.catnip.entity;

import com.mewna.catnip.entity.util.ImageOptions;

/**
 * @author amy
 * @since 9/4/18
 */
public interface User {
    
    /**
     * Whether the user's avatar is animated.
     *
     * @return True if the avatar is animated, false otherwise.
     */
    boolean isAvatarAnimated();
    
    /**
     * The URL for the default avatar for this user.
     *
     * @return String containing the URL to the default avatar. Never null.
     */
    String defaultAvatarUrl();
    
    /**
     * The URL for the user's set avatar. Can be null if the user has not set an avatar.
     * <br>See {@link User#defaultAvatarUrl()} and {@link User#effectiveAvatarUrl()}.
     *
     * @param options {@link ImageOptions Image Options}.
     *
     * @return String containing the URL to their avatar, options considered. Can be null.
     */
    String avatarUrl(final ImageOptions options);
    
    /**
     * The URL for the user's set avatar. Can be null if the user has not set an avatar.
     * <br>See {@link User#defaultAvatarUrl()} and {@link User#effectiveAvatarUrl()}.
     *
     * @return String containing the URL to their avatar. Can be null.
     */
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
    String effectiveAvatarUrl(final ImageOptions options);
    
    /**
     * The URL for the user's effective avatar, as displayed in the Discord client.
     * <br>Convenience method for getting the user's default avatar
     * when {@link User#avatarUrl()} is null.
     *
     * @return String containing a URL to their effective avatar. Never null.
     */
    String effectiveAvatarUrl();
    
    /**
     * The username of the user.
     *
     * @return User's name. Never null.
     */
    String username();
    
    /**
     * The unique snowflake ID of the user.
     *
     * @return User's ID. never null.
     */
    String id();
    
    /**
     * Discriminator of the user, used to tell Amy#0001 from Amy#0002.
     *
     * @return 4 digit discriminator as a string. Never null.
     */
    String discriminator();
    
    /**
     * User's avatar hash.
     * <br><b>This does not return their avatar URL nor image directly.</b>
     * See {@link User#avatarUrl()} for a more useful method.
     *
     * @return User's hashed avatar string. Can be null.
     */
    String avatar();
    
    /**
     * Whether the user is a bot, or webhook/fake user.
     *
     * @return True if the user is a bot, false if the user is a human.
     */
    boolean bot();
}
