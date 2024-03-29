/*
 * Copyright (c) 2018 amy, All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.mewna.catnip.util;

import com.mewna.catnip.entity.guild.Role;
import com.mewna.catnip.entity.sticker.Sticker;
import com.mewna.catnip.entity.sticker.StickerFormatType;
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
    private CDNFormat() {
    }
    
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
        if(options.type() == ImageType.GIF && !avatar.startsWith("a_")) {
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
        if(options.type() == ImageType.GIF && !icon.startsWith("a_")) {
            throw new IllegalArgumentException("Cannot build gif icon URL for non gif icon!");
        }
        return options.buildUrl(
                String.format("https://cdn.discordapp.com/icons/%s/%s", id, icon)
        );
    }
    
    @Nullable
    @CheckReturnValue
    public static String applicationIconUrl(@Nonnull final String id, @Nullable final String icon, @Nonnull final ImageOptions options) {
        if(icon == null) {
            return null;
        }
        if(options.type() == ImageType.GIF && !icon.startsWith("a_")) {
            throw new IllegalArgumentException("Cannot build gif icon URL for non gif application icon!");
        }
        return options.buildUrl(String.format("team-icons/%s/%s", id, icon));
    }
    
    @Nullable
    @CheckReturnValue
    public static String teamIconUrl(@Nonnull final String id, @Nullable final String icon, @Nonnull final ImageOptions options) {
        if(icon == null) {
            return null;
        }
        if(options.type() == ImageType.GIF) {
            throw new IllegalArgumentException("Team icons may not be GIFs");
        }
        return options.buildUrl(
                String.format("https://cdn.discordapp.com/team-icons/%s/%s", id, icon)
        );
    }
    
    @Nullable
    @CheckReturnValue
    public static String splashUrl(@Nonnull final String id, @Nullable final String splash, @Nonnull final ImageOptions options) {
        if(splash == null) {
            return null;
        }
        if(options.type() == ImageType.GIF) {
            throw new IllegalArgumentException("Guild icons may not be GIFs");
        }
        return options.buildUrl(
                String.format("https://cdn.discordapp.com/splashes/%s/%s", id, splash)
        );
    }
    
    @Nonnull
    @CheckReturnValue
    public static String stickerUrl(@Nonnull final Sticker sticker) {
        // This method is special because stickers are special
        if(sticker.formatType() == StickerFormatType.LOTTIE) {
            // TODO: How to handle lottie?
            throw new UnsupportedOperationException("CDN URLs for lottie stickers are currently unsupported.");
        }
        return String.format("https://cdn.discordapp.com/stickers/%s/%s.%s", sticker.id(), sticker.asset(),
                sticker.formatType().name().toLowerCase());
    }
    
    @Nonnull
    @CheckReturnValue
    public static String roleIconUrl(@Nonnull final String roleId, @Nonnull final String iconHash,
                                     @Nonnull final ImageOptions options) {
        return options.buildUrl(String.format("https://cdn.discordapp.com/role-icons/%s/%s", roleId, iconHash));
    }
    
    @Nonnull
    @CheckReturnValue
    public static String guildAvatarUrl(@Nonnull final String guildId, @Nonnull final String userId,
                                        @Nonnull final String hash, @Nonnull final ImageOptions options) {
        return options.buildUrl(String.format("https://cdn.discordapp.com/guilds/%s/%s/%s", guildId, userId, hash));
    }
}
