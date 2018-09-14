package com.mewna.catnip.entity.impl;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.Channel.ChannelType;
import com.mewna.catnip.entity.Guild.VerificationLevel;
import com.mewna.catnip.entity.Invite;
import com.mewna.catnip.entity.util.ImageOptions;
import com.mewna.catnip.entity.util.ImageType;
import com.mewna.catnip.util.CDNFormat;
import lombok.*;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

/**
 * @author natanbc
 * @since 9/14/18.
 */
@Getter
@Setter
@Builder
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public class InviteImpl implements Invite, RequiresCatnip {
    private transient Catnip catnip;
    
    private String code;
    private Inviter inviter;
    private InviteGuild guild;
    private InviteChannel channel;
    private int approximatePresenceCount;
    private int approximateMemberCount;
    
    @Override
    public void catnip(@Nonnull final Catnip catnip) {
        this.catnip = catnip;
    }
    
    @Override
    public int hashCode() {
        return code.hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return obj instanceof Invite && ((Invite)obj).code().equals(code);
    }
    
    @Override
    public String toString() {
        return String.format("Invite (%s)", code);
    }
    
    @Getter
    @Setter
    @Builder
    @Accessors(fluent = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InviterImpl implements Inviter, RequiresCatnip {
        private transient Catnip catnip;
        
        private String id;
        private String username;
        private String discriminator;
        private String avatar;
    
        @Override
        public void catnip(@Nonnull final Catnip catnip) {
            this.catnip = catnip;
        }
    
        @Override
        public boolean animatedAvatar() {
            return avatar != null && avatar.startsWith("a_");
        }
    
        @Nonnull
        @Override
        public String defaultAvatarUrl() {
            return CDNFormat.defaultAvatarUrl(discriminator);
        }
    
        @Nullable
        @Override
        public String avatarUrl(@Nonnull final ImageOptions options) {
            return CDNFormat.avatarUrl(id, avatar, options);
        }
    
        @Nullable
        @Override
        public String avatarUrl() {
            return avatarUrl(defaultOptions());
        }
    
        @Nonnull
        @Override
        public String effectiveAvatarUrl(@Nonnull final ImageOptions options) {
            return avatar == null ? defaultAvatarUrl() : Objects.requireNonNull(
                    avatarUrl(options),
                    "Avatar url is null but avatar hash is present (??)"
            );
        }
    
        @Nonnull
        @Override
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
            return obj instanceof Inviter && ((Inviter)obj).id().equals(id);
        }
    
        @Override
        public String toString() {
            return String.format("Inviter (%s)", username);
        }
    }
    
    @Getter
    @Setter
    @Builder
    @Accessors(fluent = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InviteGuildImpl implements InviteGuild, RequiresCatnip {
        private transient Catnip catnip;
        
        private String id;
        private String name;
        private String icon;
        private String splash;
        private List<String> features;
        private VerificationLevel verificationLevel;
    
        @Override
        public void catnip(@Nonnull final Catnip catnip) {
            this.catnip = catnip;
        }
        
        @Nullable
        @Override
        public String iconUrl(@Nonnull final ImageOptions options) {
            return CDNFormat.iconUrl(id, icon, options);
        }
    
        @Nullable
        @Override
        public String splashUrl(@Nonnull final ImageOptions options) {
            return CDNFormat.splashUrl(id, splash, options);
        }
    
        @Override
        public int hashCode() {
            return id.hashCode();
        }
    
        @Override
        public boolean equals(final Object obj) {
            return obj instanceof InviteGuild && ((InviteGuild)obj).id().equals(id);
        }
    
        @Override
        public String toString() {
            return String.format("InviteGuild (%s)", name);
        }
    }
    
    @Getter
    @Setter
    @Builder
    @Accessors(fluent = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InviteChannelImpl implements InviteChannel, RequiresCatnip {
        private transient Catnip catnip;
        
        private String id;
        private String name;
        private ChannelType type;
        
        @Override
        public void catnip(@Nonnull final Catnip catnip) {
            this.catnip = catnip;
        }
        
        @Override
        public int hashCode() {
            return id.hashCode();
        }
        
        @Override
        public boolean equals(final Object obj) {
            return obj instanceof InviteChannel && ((InviteChannel)obj).id().equals(id);
        }
        
        @Override
        public String toString() {
            return String.format("InviteChannel (%s)", name);
        }
    }
}
