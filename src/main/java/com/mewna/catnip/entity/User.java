package com.mewna.catnip.entity;

import lombok.*;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * @author amy
 * @since 9/1/18.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String username;
    private String id;
    private String discriminator;
    private String avatar;
    private boolean bot;
    
    @Nonnull
    @CheckReturnValue
    public String defaultAvatarUrl() {
        final int avatarId = Short.parseShort(discriminator) % 5;
        return String.format("https://cdn.discordapp.com/embed/avatars/%s.png", avatarId);
    }
    
    @Nullable
    @CheckReturnValue
    public String avatarUrl(@Nonnull final ImageOptions options) {
        return avatar == null ? null : options.buildUrl(
                String.format("https://cdn.discordapp.com/avatars/%s/%s.png", id, avatar)
        );
    }
    
    @Nullable
    @CheckReturnValue
    public String avatarUrl() {
        return avatarUrl(new ImageOptions());
    }
    
    @Nonnull
    @CheckReturnValue
    public String effectiveAvatarUrl(@Nonnull final ImageOptions options) {
        return avatar == null ? defaultAvatarUrl() : Objects.requireNonNull(
                avatarUrl(options),
                "Avatar url is null but avatar hash is present (??)"
        );
    }
    
    @Nonnull
    @CheckReturnValue
    public String effectiveAvatarUrl() {
        return effectiveAvatarUrl(new ImageOptions());
    }
}
