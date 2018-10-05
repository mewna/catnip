package com.mewna.catnip.entity.impl;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.entity.util.ImageOptions;
import com.mewna.catnip.entity.util.ImageType;
import com.mewna.catnip.util.CDNFormat;
import lombok.*;
import lombok.experimental.Accessors;

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
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings({"WeakerAccess", "unused"})
public class UserImpl implements User, RequiresCatnip {
    private transient Catnip catnip;
    
    private String username;
    private String id;
    private String discriminator;
    private String avatar;
    private boolean bot;
    
    @Override
    public void catnip(@Nonnull final Catnip catnip) {
        this.catnip = catnip;
    }
    
    @Override
    @CheckReturnValue
    public boolean animatedAvatar() {
        return avatar != null && avatar.startsWith("a_");
    }
    
    @Nonnull
    @CheckReturnValue
    public String defaultAvatarUrl() {
        return CDNFormat.defaultAvatarUrl(discriminator);
    }
    
    @Nullable
    @CheckReturnValue
    public String avatarUrl(@Nonnull final ImageOptions options) {
        return CDNFormat.avatarUrl(id, avatar, options);
    }
    
    @Nullable
    @CheckReturnValue
    public String avatarUrl() {
        return avatarUrl(defaultOptions());
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
        return effectiveAvatarUrl(defaultOptions());
    }
    
    private ImageOptions defaultOptions() {
        return new ImageOptions().type(animatedAvatar() ? ImageType.GIF : null);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return obj instanceof User && ((User)obj).id().equals(id);
    }
    
    @Override
    public String toString() {
        return String.format("User (%s#%s)", username, discriminator);
    }
}
