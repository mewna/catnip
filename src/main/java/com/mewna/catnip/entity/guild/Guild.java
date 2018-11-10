package com.mewna.catnip.entity.guild;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mewna.catnip.entity.Snowflake;
import com.mewna.catnip.entity.channel.Channel;
import com.mewna.catnip.entity.channel.GuildChannel;
import com.mewna.catnip.entity.channel.Webhook;
import com.mewna.catnip.entity.impl.GuildImpl;
import com.mewna.catnip.entity.misc.CreatedInvite;
import com.mewna.catnip.entity.misc.Emoji.CustomEmoji;
import com.mewna.catnip.entity.user.VoiceState;
import com.mewna.catnip.entity.util.ImageOptions;
import com.mewna.catnip.entity.util.Permission;
import com.mewna.catnip.util.Utils;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletionStage;

/**
 * @author natanbc
 * @since 9/6/18
 */
@SuppressWarnings("unused")
@JsonDeserialize(as = GuildImpl.class)
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
    
    @CheckReturnValue
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
    default List<Role> roles() {
        return catnip().cache().roles(id());
    }
    
    @Nonnull
    @CheckReturnValue
    default List<CustomEmoji> emojis() {
        return catnip().cache().emojis(id());
    }
    
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
    default List<Member> members() {
        return catnip().cache().members(id());
    }
    
    @Nonnull
    @CheckReturnValue
    default List<Channel> channels() {
        return catnip().cache().channels(id());
    }
    
    @Nonnull
    @CheckReturnValue
    default List<VoiceState> voiceStates() {
        return catnip().cache().voiceStates(id());
    }
    
    // REST methods
    
    @Nonnull
    @JsonIgnore
    @CheckReturnValue
    default CompletionStage<List<Role>> fetchRoles() {
        return catnip().rest().guild().getGuildRoles(id());
    }
    
    @Nonnull
    @JsonIgnore
    @CheckReturnValue
    default CompletionStage<List<GuildChannel>> fetchChannels() {
        return catnip().rest().guild().getGuildChannels(id());
    }
    
    @Nonnull
    @JsonIgnore
    @CheckReturnValue
    default CompletionStage<List<CreatedInvite>> fetchInvites() {
        return catnip().rest().guild().getGuildInvites(id());
    }
    
    @Nonnull
    @JsonIgnore
    @CheckReturnValue
    default CompletionStage<List<Webhook>> fetchWebhooks() {
        return catnip().rest().webhook().getGuildWebhooks(id());
    }
    
    @Nonnull
    @JsonIgnore
    @CheckReturnValue
    default CompletionStage<List<CustomEmoji>> fetchEmojis() {
        return catnip().rest().emoji().listGuildEmojis(id());
    }
    
    @Nonnull
    @JsonIgnore
    @CheckReturnValue
    default CompletionStage<CustomEmoji> fetchEmoji(@Nonnull final String emojiId) {
        return catnip().rest().emoji().getGuildEmoji(id(), emojiId);
    }
    
    @Nonnull
    @JsonIgnore
    default CompletionStage<CustomEmoji> createEmoji(@Nonnull final String name, @Nonnull final byte[] image,
                                                     @Nonnull final Collection<String> roles) {
        return catnip().rest().emoji().createGuildEmoji(id(), name, image, roles);
    }
    
    @Nonnull
    @JsonIgnore
    default CompletionStage<CustomEmoji> createEmoji(@Nonnull final String name, @Nonnull final URI imageData,
                                                     @Nonnull final Collection<String> roles) {
        return catnip().rest().emoji().createGuildEmoji(id(), name, imageData, roles);
    }
    
    @Nonnull
    @JsonIgnore
    default CompletionStage<CustomEmoji> modifyEmoji(@Nonnull final String emojiId, @Nonnull final String name,
                                                     @Nonnull final Collection<String> roles) {
        return catnip().rest().emoji().modifyGuildEmoji(id(), emojiId, name, roles);
    }
    
    @Nonnull
    @JsonIgnore
    default CompletionStage<Void> deleteEmoji(@Nonnull final String emojiId) {
        return catnip().rest().emoji().deleteGuildEmoji(id(), emojiId);
    }
    
    @Nonnull
    @JsonIgnore
    default CompletionStage<Void> leave() {
        return catnip().rest().user().leaveGuild(id());
    }
    
    @Nonnull
    @JsonIgnore
    default CompletionStage<Void> delete() {
        return catnip().rest().guild().deleteGuild(id());
    }
    
    @Nonnull
    @JsonIgnore
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
    
    @SuppressWarnings("unused")
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
        private URI icon;
        private String ownerId;
        private URI splash;
        private String systemChannelId;
        
        public GuildEditFields(@Nullable final Guild guild) {
            this.guild = guild;
        }
        
        public GuildEditFields() {
            this(null);
        }
        
        @Nonnull
        public GuildEditFields icon(@Nullable final URI iconData) {
            if(iconData != null) {
                Utils.validateImageUri(iconData);
            }
            icon = iconData;
            return this;
        }
        
        @Nonnull
        public GuildEditFields icon(@Nullable final byte[] iconData) {
            return icon(iconData == null ? null : Utils.asImageDataUri(iconData));
        }
        
        @Nonnull
        public GuildEditFields splash(@Nullable final URI splashData) {
            if(splashData != null) {
                Utils.validateImageUri(splashData);
            }
            splash = splashData;
            return this;
        }
        
        @Nonnull
        public GuildEditFields splash(@Nullable final byte[] splashData) {
            return splash(splashData == null ? null : Utils.asImageDataUri(splashData));
        }
        
        @Nonnull
        public CompletionStage<Guild> submit() {
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
            if(verificationLevel != null && (guild == null || verificationLevel != guild.verificationLevel())) {
                payload.put("verification_level", verificationLevel.getKey());
            }
            if(defaultMessageNotifications != null && (guild == null || defaultMessageNotifications != guild.defaultMessageNotifications())) {
                payload.put("default_message_notifications", defaultMessageNotifications.getKey());
            }
            if(explicitContentFilter != null && (guild == null || explicitContentFilter != guild.explicitContentFilter())) {
                payload.put("explicit_content_filter", explicitContentFilter.getKey());
            }
            if(afkChannelId != null && (guild == null || !Objects.equals(afkChannelId, guild.afkChannelId()))) {
                payload.put("afk_channel_id", afkChannelId);
            }
            if(afkTimeout != null && (guild == null || !Objects.equals(afkTimeout, guild.afkTimeout()))) {
                payload.put("afk_timeout", afkTimeout);
            }
            if(icon != null) {
                payload.put("icon", icon.toString());
            }
            if(ownerId != null && (guild == null || !Objects.equals(ownerId, guild.ownerId()))) {
                payload.put("owner_id", ownerId);
            }
            if(splash != null) {
                payload.put("splash", splash.toString());
            }
            if(systemChannelId != null && (guild == null || !Objects.equals(systemChannelId, guild.systemChannelId()))) {
                payload.put("system_channel_id", systemChannelId);
            }
            return payload;
        }
    }
}
