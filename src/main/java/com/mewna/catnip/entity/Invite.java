package com.mewna.catnip.entity;

import com.mewna.catnip.entity.Channel.ChannelType;
import com.mewna.catnip.entity.Guild.VerificationLevel;
import com.mewna.catnip.entity.util.ImageOptions;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author natanbc
 * @since 9/14/18
 */
public interface Invite extends Entity {
    @Nonnull
    @CheckReturnValue
    String code();
    
    @Nonnull
    @CheckReturnValue
    Inviter inviter();
    
    @Nonnull
    @CheckReturnValue
    InviteGuild guild();
    
    @Nonnull
    @CheckReturnValue
    InviteChannel channel();
    
    int approximatePresenceCount();
    int approximateMemberCount();
    
    @Nonnull
    default CompletableFuture<Invite> delete() {
        return catnip().rest().invite().deleteInvite(code());
    }
    
    interface Inviter extends Snowflake {
        @Nonnull
        @CheckReturnValue
        String id();
        
        @Nonnull
        @CheckReturnValue
        String username();
    
        @Nonnull
        @CheckReturnValue
        String discriminator();
        
        @Nonnull
        @CheckReturnValue
        String avatar();
        
        @CheckReturnValue
        boolean animatedAvatar();
    
        @Nonnull
        @CheckReturnValue
        String defaultAvatarUrl();
        
        @Nullable
        @CheckReturnValue
        String avatarUrl(@Nonnull ImageOptions options);
        @Nullable
        @CheckReturnValue
        String avatarUrl();
    
        @Nonnull
        @CheckReturnValue
        String effectiveAvatarUrl(@Nonnull ImageOptions options);
        @Nonnull
        @CheckReturnValue
        String effectiveAvatarUrl();
    }
    
    interface InviteGuild extends Snowflake {
        @Nonnull
        @CheckReturnValue
        String id();
    
        @Nonnull
        @CheckReturnValue
        String name();
    
        @Nullable
        @CheckReturnValue
        String icon();
    
        @Nullable
        @CheckReturnValue
        String splash();
        
        @Nonnull
        @CheckReturnValue
        List<String> features();
        
        @Nonnull
        @CheckReturnValue
        VerificationLevel verificationLevel();
    
        @Nullable
        @CheckReturnValue
        String iconUrl(@Nonnull ImageOptions options);
        @Nullable
        @CheckReturnValue
        default String iconUrl() {
            return iconUrl(new ImageOptions());
        }
    
        @Nullable
        @CheckReturnValue
        String splashUrl(@Nonnull ImageOptions options);
        @Nullable
        @CheckReturnValue
        default String splashUrl() {
            return splashUrl(new ImageOptions());
        }
    }
    
    interface InviteChannel extends Snowflake {
        @Nonnull
        @CheckReturnValue
        String id();
    
        @Nonnull
        @CheckReturnValue
        String name();
        
        @Nonnull
        @CheckReturnValue
        ChannelType type();
    }
}
