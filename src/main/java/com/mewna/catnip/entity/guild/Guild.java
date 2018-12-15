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

package com.mewna.catnip.entity.guild;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mewna.catnip.cache.view.CacheView;
import com.mewna.catnip.cache.view.NamedCacheView;
import com.mewna.catnip.entity.Snowflake;
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
 * Represents a Discord guild. A guild is colloquially referred to as a server,
 * both in the client UI and by users.
 *
 * @author natanbc
 * @since 9/6/18
 */
@SuppressWarnings("unused")
@JsonDeserialize(as = GuildImpl.class)
public interface Guild extends Snowflake {
    /**
     * @return The guild's name.
     */
    @Nonnull
    @CheckReturnValue
    String name();
    
    /**
     * @return The hash of the guild's icon.
     */
    @Nullable
    @CheckReturnValue
    String icon();
    
    /**
     * Return the guild's icon's CDN URL with the specified options.
     *
     * @param options The options to configure the URL returned.
     *
     * @return The CDN URL for the guild's icon.
     */
    @Nullable
    @CheckReturnValue
    String iconUrl(@Nonnull final ImageOptions options);
    
    /**
     * @return The CDN URL for the guild's icon.
     */
    @Nullable
    @CheckReturnValue
    default String iconUrl() {
        return iconUrl(new ImageOptions());
    }
    
    /**
     * @return The splash image for the guild. May be {@code null}.
     */
    @Nullable
    @CheckReturnValue
    String splash();
    
    /**
     * The CDN URL of the guild's splash image.
     *
     * @param options The options to configure the URL returned.
     *
     * @return The CDN URL.
     */
    @Nullable
    @CheckReturnValue
    String splashUrl(@Nonnull ImageOptions options);
    
    /**
     * @return The CDN URL of the guild's splash image.
     */
    @Nullable
    @CheckReturnValue
    default String splashUrl() {
        return splashUrl(new ImageOptions());
    }
    
    /**
     * @return Whether the guild is owned by the current user.
     */
    @CheckReturnValue
    boolean owned();
    
    /**
     * @return The id of the user who owns the guild.
     */
    @Nonnull
    @CheckReturnValue
    String ownerId();
    
    /**
     * @return Total permissions for the user in the guild. Does NOT include
     * channel overrides.
     */
    @Nonnull
    @CheckReturnValue
    Set<Permission> permissions();
    
    /**
     * @return The region that the guild's voice servers are located in.
     */
    @Nonnull
    @CheckReturnValue
    String region();
    
    /**
     * @return The id of the afk voice channel for the guild.
     */
    @Nullable
    @CheckReturnValue
    String afkChannelId();
    
    /**
     * @return The amount of time a user must be afk for before they're moved
     * to the afk channel.
     */
    @CheckReturnValue
    int afkTimeout();
    
    /**
     * @return Whether the guild embed is enabled.
     */
    @CheckReturnValue
    boolean embedEnabled();
    
    /**
     * @return The channel the guild embed is for, if enabled.
     */
    @Nullable
    @CheckReturnValue
    String embedChannelId();
    
    /**
     * @return The verification level set for the guild.
     */
    @Nonnull
    @CheckReturnValue
    VerificationLevel verificationLevel();
    
    /**
     * @return The notification level set for the guild.
     */
    @Nonnull
    @CheckReturnValue
    NotificationLevel defaultMessageNotifications();
    
    /**
     * @return The explicit content filter level set for the guild.
     */
    @Nonnull
    @CheckReturnValue
    ContentFilterLevel explicitContentFilter();
    
    /**
     * @return The list of features enabled for the guild.
     */
    @Nonnull
    @CheckReturnValue
    List<String> features();
    
    /**
     * @return The MFA level set for guild administrators.
     */
    @Nonnull
    @CheckReturnValue
    MFALevel mfaLevel();
    
    /**
     * @return The id of the application that created this guild.
     */
    @Nullable
    @CheckReturnValue
    String applicationId();
    
    /**
     * @return Whether or not the guild's widget is enabled.
     */
    @CheckReturnValue
    boolean widgetEnabled();
    
    /**
     * @return The channel the guild's widget is set for, if enabled.
     */
    @Nullable
    @CheckReturnValue
    String widgetChannelId();
    
    /**
     * @return The id of the channel used for system messages (ex. the built-in
     * member join messages).
     */
    @Nullable
    @CheckReturnValue
    String systemChannelId();
    
    //The following fields are only sent in GUILD_CREATE
    
    /**
     * @return The date/time that the current user joined the guild at.
     */
    @CheckReturnValue
    OffsetDateTime joinedAt();
    
    /**
     * @return Whether or not this guild is considered "large."
     */
    @CheckReturnValue
    boolean large();
    
    /**
     * @return Whether or not this guild is currently unavailable.
     */
    @CheckReturnValue
    default boolean unavailable() {
        return catnip().isUnavailable(id());
    }
    
    /**
     * @return The number of users in this guild.
     */
    @Nonnegative
    @CheckReturnValue
    default long memberCount() {
        return members().size();
    }
    
    /**
     * @return All roles in this guild.
     */
    @Nonnull
    @CheckReturnValue
    default NamedCacheView<Role> roles() {
        return catnip().cache().roles(id());
    }
    
    /**
     * @return All custom emoji in this guild.
     */
    @Nonnull
    @CheckReturnValue
    default NamedCacheView<CustomEmoji> emojis() {
        return catnip().cache().emojis(id());
    }
    
    /**
     * @return All members in this guild.
     */
    @Nonnull
    @CheckReturnValue
    default NamedCacheView<Member> members() {
        return catnip().cache().members(id());
    }
    
    /**
     * @return All channels in this guild.
     */
    @Nonnull
    @CheckReturnValue
    default NamedCacheView<GuildChannel> channels() {
        return catnip().cache().channels(id());
    }
    
    /**
     * @return All voice states for this guild.
     */
    @Nonnull
    @CheckReturnValue
    default CacheView<VoiceState> voiceStates() {
        return catnip().cache().voiceStates(id());
    }
    
    // REST methods
    
    /**
     * Fetch all roles for this guild from the API.
     *
     * @return A CompletionStage that completes when the roles are fetched.
     */
    @Nonnull
    @JsonIgnore
    @CheckReturnValue
    default CompletionStage<List<Role>> fetchRoles() {
        return catnip().rest().guild().getGuildRoles(id());
    }
    
    /**
     * Fetch all channels for this guild from the API.
     *
     * @return A CompletionStage that completes when the channels are fetched.
     */
    @Nonnull
    @JsonIgnore
    @CheckReturnValue
    default CompletionStage<List<GuildChannel>> fetchChannels() {
        return catnip().rest().guild().getGuildChannels(id());
    }
    
    /**
     * Fetch all invites for this guild from the API.
     *
     * @return A CompletionStage that completes when the invites are fetched.
     */
    @Nonnull
    @JsonIgnore
    @CheckReturnValue
    default CompletionStage<List<CreatedInvite>> fetchInvites() {
        return catnip().rest().guild().getGuildInvites(id());
    }
    
    /**
     * Fetch all webhooks for this guild.
     *
     * @return A CompletionStage that completes when the webhooks are fetched.
     */
    @Nonnull
    @JsonIgnore
    @CheckReturnValue
    default CompletionStage<List<Webhook>> fetchWebhooks() {
        return catnip().rest().webhook().getGuildWebhooks(id());
    }
    
    /**
     * Fetch all emojis for this guild.
     *
     * @return A CompletionStage that completes when the emojis are fetched.
     */
    @Nonnull
    @JsonIgnore
    @CheckReturnValue
    default CompletionStage<List<CustomEmoji>> fetchEmojis() {
        return catnip().rest().emoji().listGuildEmojis(id());
    }
    
    /**
     * Fetch a single emoji from this guild.
     *
     * @param emojiId The id of the emoji to fetch.
     *
     * @return A CompletionStage that completes when the emoji is fetched.
     */
    @Nonnull
    @JsonIgnore
    @CheckReturnValue
    default CompletionStage<CustomEmoji> fetchEmoji(@Nonnull final String emojiId) {
        return catnip().rest().emoji().getGuildEmoji(id(), emojiId);
    }
    
    /**
     * Create a new emoji on the guild.
     *
     * @param name  The new emoji's name.
     * @param image The image for the new emoji.
     * @param roles The roles that can use the new emoji.
     *
     * @return A CompletionStage that completes when the emoji is created.
     */
    @Nonnull
    @JsonIgnore
    default CompletionStage<CustomEmoji> createEmoji(@Nonnull final String name, @Nonnull final byte[] image,
                                                     @Nonnull final Collection<String> roles) {
        return catnip().rest().emoji().createGuildEmoji(id(), name, image, roles);
    }
    
    /**
     * Create a new emoji on the guild.
     *
     * @param name      The new emoji's name.
     * @param imageData The image for the new emoji.
     * @param roles     The roles that can use the new emoji.
     *
     * @return A CompletionStage that completes when the emoji is created.
     */
    @Nonnull
    @JsonIgnore
    default CompletionStage<CustomEmoji> createEmoji(@Nonnull final String name, @Nonnull final URI imageData,
                                                     @Nonnull final Collection<String> roles) {
        return catnip().rest().emoji().createGuildEmoji(id(), name, imageData, roles);
    }
    
    /**
     * Modify the given emoji.
     *
     * @param emojiId The id of the emoji to modify.
     * @param name    The name of the emoji. To not change it, pass the old name.
     * @param roles   The roles that can use the emoji. To not change it, pass
     *                the old roles.
     *
     * @return A CompletionStage that completes when the emoji is modified.
     */
    @Nonnull
    @JsonIgnore
    default CompletionStage<CustomEmoji> modifyEmoji(@Nonnull final String emojiId, @Nonnull final String name,
                                                     @Nonnull final Collection<String> roles) {
        return catnip().rest().emoji().modifyGuildEmoji(id(), emojiId, name, roles);
    }
    
    /**
     * Delete the given emoji from the guild.
     *
     * @param emojiId The id of the emoji to delete.
     *
     * @return A CompletionStage that completes when the emoji is deleted.
     */
    @Nonnull
    @JsonIgnore
    default CompletionStage<Void> deleteEmoji(@Nonnull final String emojiId) {
        return catnip().rest().emoji().deleteGuildEmoji(id(), emojiId);
    }
    
    /**
     * Leave this guild.
     *
     * @return A CompletionStage that completes when the guild is left.
     */
    @Nonnull
    @JsonIgnore
    default CompletionStage<Void> leave() {
        return catnip().rest().user().leaveGuild(id());
    }
    
    /**
     * Delete this guild.
     *
     * @return A CompletionStage that completes when the guild is deleted.
     */
    @Nonnull
    @JsonIgnore
    default CompletionStage<Void> delete() {
        return catnip().rest().guild().deleteGuild(id());
    }
    
    /**
     * Closes the voice connection for this guild. This method is equivalent to
     * {@code guild.catnip().{@link com.mewna.catnip.Catnip#closeVoiceConnection(String) closeVoiceConnection}(guild.id())}
     *
     * @see com.mewna.catnip.Catnip#closeVoiceConnection(String)
     */
    @JsonIgnore
    default void closeVoiceConnection() {
        catnip().closeVoiceConnection(id());
    }
    
    /**
     * Edit this guild.
     *
     * @return A guild editor that can complete the editing.
     */
    @Nonnull
    @JsonIgnore
    @CheckReturnValue
    default GuildEditFields edit() {
        return new GuildEditFields(this);
    }
    
    /**
     * The notification level for a guild.
     */
    enum NotificationLevel {
        /**
         * Users get notifications for all messages.
         */
        ALL_MESSAGES(0),
        /**
         * Users only get notifications for mentions.
         */
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
    
    /**
     * The content filter level for a guild.
     */
    enum ContentFilterLevel {
        /**
         * No messages are filtered.
         */
        DISABLED(0),
        /**
         * Only messages from members with no roles are filtered.
         */
        MEMBERS_WITHOUT_ROLES(1),
        /**
         * Messages from all members are filtered.
         */
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
    
    /**
     * The 2FA level required for this guild.
     */
    enum MFALevel {
        /**
         * 2FA is not required.
         */
        NONE(0),
        /**
         * 2FA is required for admin actions.
         */
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
    
    /**
     * The verification level for a guild.
     */
    enum VerificationLevel {
        /**
         * No restrictions.
         */
        NONE(0),
        /**
         * Members must have a verified email on their account.
         */
        LOW(1),
        /**
         * Members must also be registered on Discord for more than 5 minutes.
         */
        MEDIUM(2),
        /**
         * Members must also have been a member of this guild for more than 10
         * minutes.
         */
        HIGH(3),
        /**
         * Members must also have a verified phone number on their account.
         */
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
