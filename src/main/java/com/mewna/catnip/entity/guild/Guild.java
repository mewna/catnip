package com.mewna.catnip.entity.guild;

import com.mewna.catnip.entity.misc.CreatedInvite;
import com.mewna.catnip.entity.misc.Emoji.CustomEmoji;
import com.mewna.catnip.entity.Snowflake;
import com.mewna.catnip.entity.channel.Webhook;
import com.mewna.catnip.entity.channel.Channel;
import com.mewna.catnip.entity.channel.GuildChannel;
import com.mewna.catnip.entity.util.Permission;
import com.mewna.catnip.entity.util.ImageOptions;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
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
    
    @Nonnull
    @CheckReturnValue
    default GuildEditFields edit() {
        return new GuildEditFields(this);
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
    
    @Getter
    @Setter
    @Accessors(fluent = true)
    class GuildEditFields {
        private final Guild guild;
        private String name;
        private String region;
        private VerificationLevel verificationLevel;
        private NotificationLevel defaultMessageNotifications;
        private ContentFilterLevel explicitContentFilter;
        private String afkChannelId;
        private Integer afkTimeout;
        private String icon;
        private String ownerId;
        private String splash;
        private String systemChannelId;
        
        public GuildEditFields(@Nullable final Guild guild) {
            this.guild = guild;
        }
    
        public GuildEditFields() {
            this(null);
        }
        
        @Nonnull
        public CompletableFuture<Guild> submit() {
            if(guild == null) {
                throw new IllegalStateException("Cannot submit edit without a guild object! Please use RestGuild directly instead");
            }
            return guild.catnip().rest().guild().modifyGuild(guild.id(), this);
        }
    
        @Nonnull
        @CheckReturnValue
        public JsonObject payload() {
            final JsonObject payload = new JsonObject();
            if(name != null && (guild == null || !Objects.equals(name, guild.name()))) {
                payload.put("name", name);
            }
            if(region != null && (guild == null || !Objects.equals(region, guild.region()))) {
                payload.put("region", region);
            }
            if(verificationLevel != null && (guild == null || !Objects.equals(verificationLevel, guild.verificationLevel()))) {
                payload.put("verification_level", verificationLevel.getKey());
            }
            if(defaultMessageNotifications != null && (guild == null || !Objects.equals(defaultMessageNotifications, guild.defaultMessageNotifications()))) {
                payload.put("default_message_notifications", defaultMessageNotifications.getKey());
            }
            if(explicitContentFilter != null && (guild == null || !Objects.equals(explicitContentFilter, guild.explicitContentFilter()))) {
                payload.put("explicit_content_filter", explicitContentFilter.getKey());
            }
            if(afkChannelId != null && (guild == null || !Objects.equals(afkChannelId, guild.afkChannelId()))) {
                payload.put("afk_channel_id", afkChannelId);
            }
            if(afkTimeout != null && (guild == null || !Objects.equals(afkTimeout, guild.afkTimeout()))) {
                payload.put("afk_timeout", afkTimeout);
            }
            if(icon != null && (guild == null || !Objects.equals(icon, guild.icon()))) {
                payload.put("icon", icon);
            }
            if(ownerId != null && (guild == null || !Objects.equals(ownerId, guild.ownerId()))) {
                payload.put("owner_id", ownerId);
            }
            if(splash != null && (guild == null || !Objects.equals(splash, guild.splash()))) {
                payload.put("splash", splash);
            }
            if(systemChannelId != null && (guild == null || !Objects.equals(systemChannelId, guild.systemChannelId()))) {
                payload.put("system_channel_id", systemChannelId);
            }
            return payload;
        }
    }
}
