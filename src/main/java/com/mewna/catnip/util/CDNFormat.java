package com.mewna.catnip.util;

import com.mewna.catnip.entity.util.ImageOptions;
import com.mewna.catnip.entity.util.ImageType;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author natanbc
 * @since 9/14/18
 */
public final class CDNFormat {
    private CDNFormat() {}
    
    @Nonnull
    @CheckReturnValue
    public static String defaultAvatarUrl(@Nonnull final String discriminator) {
        final int avatarId = Short.parseShort(discriminator) % 5;
        return String.format("https://cdn.discordapp.com/embed/avatars/%s.png", avatarId);
    }
    
    @Nullable
    @CheckReturnValue
    public static String avatarUrl(@Nonnull final String id, @Nullable final String avatar, @Nonnull final ImageOptions options) {
        if(avatar == null) {
            return null;
        }
        if(options.getType() == ImageType.GIF && !avatar.startsWith("a_")) {
            throw new IllegalArgumentException("Cannot build gif avatar URL for non gif avatar!");
        }
        return options.buildUrl(
                String.format("https://cdn.discordapp.com/avatars/%s/%s", id, avatar)
        );
    }
    
    @Nullable
    @CheckReturnValue
    public static String iconUrl(@Nonnull final String id, @Nullable final String icon, @Nonnull final ImageOptions options) {
        if(icon == null) {
            return null;
        }
        if(options.getType() == ImageType.GIF) {
            throw new IllegalArgumentException("Guild icons may not be GIFs");
        }
        return options.buildUrl(
                String.format("https://cdn.discordapp.com/icons/%s/%s", id, icon)
        );
    }
    
    @Nullable
    @CheckReturnValue
    public static String splashUrl(@Nonnull final String id, @Nullable final String splash, @Nonnull final ImageOptions options) {
        if(splash == null) {
            return null;
        }
        if(options.getType() == ImageType.GIF) {
            throw new IllegalArgumentException("Guild icons may not be GIFs");
        }
        return options.buildUrl(
                String.format("https://cdn.discordapp.com/splashes/%s/%s", id, splash)
        );
    }
}
