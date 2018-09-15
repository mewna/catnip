package com.mewna.catnip.entity;

import com.mewna.catnip.entity.Emoji.CustomEmoji;
import com.mewna.catnip.entity.impl.Permission;
import com.mewna.catnip.entity.util.ImageOptions;
import lombok.Getter;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * @author natanbc
 * @since 9/6/18
 */
public interface Guild extends Snowflake {
    @Nonnull
    @CheckReturnValue
    String name();
    
    @Nullable
    @CheckReturnValue
    String icon();
    
    @Nullable
    @CheckReturnValue
    String iconUrl(@Nonnull final ImageOptions options);
    
    @Nullable
    @CheckReturnValue
    default String iconUrl() {
        return iconUrl(new ImageOptions());
    }
    
    @Nullable
    @CheckReturnValue
    String splash();
    
    @Nullable
    @CheckReturnValue
    String splashUrl(@Nonnull ImageOptions options);
    
    @Nullable
    @CheckReturnValue
    default String splashUrl() {
        return splashUrl(new ImageOptions());
    }
    
    boolean owned();
    
    @Nonnull
    @CheckReturnValue
    String ownerId();
    
    @Nonnull
    @CheckReturnValue
    Set<Permission> permissions();
    
    @Nonnull
    @CheckReturnValue
    String region();
    
    @Nullable
    @CheckReturnValue
    String afkChannelId();
    
    @CheckReturnValue
    int afkTimeout();
    
    @CheckReturnValue
    boolean embedEnabled();
    
    @Nullable
    @CheckReturnValue
    String embedChannelId();
    
    @Nonnull
    @CheckReturnValue
    VerificationLevel verificationLevel();
    
    @Nonnull
    @CheckReturnValue
    NotificationLevel defaultMessageNotifications();
    
    @Nonnull
    @CheckReturnValue
    ContentFilterLevel explicitContentFilter();
    
    @Nonnull
    @CheckReturnValue
    List<Role> roles();
    
    @Nonnull
    @CheckReturnValue
    List<CustomEmoji> emojis();
    
    @Nonnull
    @CheckReturnValue
    List<String> features();
    
    @Nonnull
    @CheckReturnValue
    MFALevel mfaLevel();
    
    @Nullable
    @CheckReturnValue
    String applicationId();
    
    @CheckReturnValue
    boolean widgetEnabled();
    
    @Nullable
    @CheckReturnValue
    String widgetChannelId();
    
    @Nullable
    @CheckReturnValue
    String systemChannelId();
    
    //The following fields are only sent in GUILD_CREATE
    
    @CheckReturnValue
    OffsetDateTime joinedAt();
    
    @CheckReturnValue
    boolean large();
    
    @CheckReturnValue
    boolean unavailable();
    
    @Nonnegative
    @CheckReturnValue
    int memberCount();
    
    @Nonnull
    @CheckReturnValue
    List<Member> members();
    
    @Nonnull
    @CheckReturnValue
    List<Channel> channels();
    
    /* TODO:
        - voice_states
        - presences
     */
    
    @Nonnull
    @CheckReturnValue
    default CompletableFuture<List<Role>> fetchRoles() {
        return catnip().rest().guild().getGuildRoles(id());
    }
    
    @Nonnull
    @CheckReturnValue
    default CompletableFuture<List<GuildChannel>> fetchChannels() {
        return catnip().rest().guild().getGuildChannels(id());
    }
    
    @Nonnull
    @CheckReturnValue
    default CompletableFuture<List<CreatedInvite>> fetchInvites() {
        return catnip().rest().guild().getGuildInvites(id());
    }
    
    @Nonnull
    @CheckReturnValue
    default CompletableFuture<List<Webhook>> fetchWebhooks() {
        return catnip().rest().webhook().getGuildWebhooks(id());
    }
    
    @Nonnull
    @CheckReturnValue
    default CompletableFuture<List<CustomEmoji>> fetchEmojis() {
        return catnip().rest().emoji().listGuildEmojis(id());
    }
    
    @Nonnull
    @CheckReturnValue
    default CompletableFuture<CustomEmoji> fetchEmoji(@Nonnull final String emojiId) {
        return catnip().rest().emoji().getGuildEmoji(id(), emojiId);
    }
    
    @Nonnull
    default CompletableFuture<CustomEmoji> createEmoji(@Nonnull final String name, @Nonnull final byte[] image,
                                                       @Nonnull final Collection<String> roles) {
        return catnip().rest().emoji().createGuildEmoji(id(), name, image, roles);
    }
    
    @Nonnull
    default CompletableFuture<CustomEmoji> createEmoji(@Nonnull final String name, @Nonnull final String base64Image,
                                                       @Nonnull final Collection<String> roles) {
        return catnip().rest().emoji().createGuildEmoji(id(), name, base64Image, roles);
    }
    
    @Nonnull
    default CompletableFuture<CustomEmoji> modifyEmoji(@Nonnull final String emojiId, @Nonnull final String name,
                                                       @Nonnull final Collection<String> roles) {
        return catnip().rest().emoji().modifyGuildEmoji(id(), emojiId, name, roles);
    }
    
    @Nonnull
    default CompletableFuture<Void> deleteEmoji(@Nonnull final String emojiId) {
        return catnip().rest().emoji().deleteGuildEmoji(id(), emojiId);
    }
    
    @Nonnull
    default CompletableFuture<Void> leave() {
        return catnip().rest().user().leaveGuild(id());
    }
    
    @Nonnull
    default CompletableFuture<Void> delete() {
        return catnip().rest().guild().deleteGuild(id());
    }
    
    enum NotificationLevel {
        ALL_MESSAGES(0),
        ONLY_MENTIONS(1);
        
        @Getter
        private final int key;
        
        NotificationLevel(final int key) {
            this.key = key;
        }
        
        @Nonnull
        public static NotificationLevel byKey(final int key) {
            for(final NotificationLevel level : values()) {
                if(level.key == key) {
                    return level;
                }
            }
            throw new IllegalArgumentException("No verification level for key " + key);
        }
    }
    
    enum ContentFilterLevel {
        DISABLED(0),
        MEMBERS_WITHOUT_ROLES(1),
        ALL_MEMBERS(2);
        
        @Getter
        private final int key;
        
        ContentFilterLevel(final int key) {
            this.key = key;
        }
        
        @Nonnull
        public static ContentFilterLevel byKey(final int key) {
            for(final ContentFilterLevel level : values()) {
                if(level.key == key) {
                    return level;
                }
            }
            throw new IllegalArgumentException("No content filter level for key " + key);
        }
    }
    
    enum MFALevel {
        NONE(0),
        ELEVATED(1);
        
        @Getter
        private final int key;
    
        MFALevel(final int key) {
            this.key = key;
        }
        
        @Nonnull
        public static MFALevel byKey(final int key) {
            for(final MFALevel level : values()) {
                if(level.key == key) {
                    return level;
                }
            }
            throw new IllegalArgumentException("No MFA level for key " + key);
        }
    }
    
    enum VerificationLevel {
        NONE(0),
        LOW(1),
        MEDIUM(2),
        HIGH(3),
        VERY_HIGH(4);
        
        @Getter
        private final int key;
    
        VerificationLevel(final int key) {
            this.key = key;
        }
        
        @Nonnull
        public static VerificationLevel byKey(final int key) {
            for(final VerificationLevel level : values()) {
                if(level.key == key) {
                    return level;
                }
            }
            throw new IllegalArgumentException("No verification level for key " + key);
        }
    }
}
