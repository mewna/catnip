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

package com.mewna.catnip.entity.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.Snowflake;
import com.mewna.catnip.entity.guild.GuildEmbed;
import com.mewna.catnip.entity.channel.*;
import com.mewna.catnip.entity.channel.Channel.ChannelType;
import com.mewna.catnip.entity.guild.*;
import com.mewna.catnip.entity.guild.Guild.ContentFilterLevel;
import com.mewna.catnip.entity.guild.Guild.MFALevel;
import com.mewna.catnip.entity.guild.Guild.NotificationLevel;
import com.mewna.catnip.entity.guild.Guild.VerificationLevel;
import com.mewna.catnip.entity.guild.Invite.InviteChannel;
import com.mewna.catnip.entity.guild.Invite.InviteGuild;
import com.mewna.catnip.entity.guild.Invite.Inviter;
import com.mewna.catnip.entity.guild.PermissionOverride.OverrideType;
import com.mewna.catnip.entity.guild.audit.ActionType;
import com.mewna.catnip.entity.guild.audit.AuditLogChange;
import com.mewna.catnip.entity.guild.audit.AuditLogEntry;
import com.mewna.catnip.entity.guild.audit.OptionalEntryInfo;
import com.mewna.catnip.entity.impl.EmbedImpl.*;
import com.mewna.catnip.entity.impl.InviteImpl.InviteChannelImpl;
import com.mewna.catnip.entity.impl.InviteImpl.InviteGuildImpl;
import com.mewna.catnip.entity.impl.InviteImpl.InviterImpl;
import com.mewna.catnip.entity.impl.MessageImpl.AttachmentImpl;
import com.mewna.catnip.entity.impl.MessageImpl.ReactionImpl;
import com.mewna.catnip.entity.impl.PresenceImpl.*;
import com.mewna.catnip.entity.message.*;
import com.mewna.catnip.entity.message.Embed.*;
import com.mewna.catnip.entity.message.Message.Attachment;
import com.mewna.catnip.entity.message.Message.Reaction;
import com.mewna.catnip.entity.misc.*;
import com.mewna.catnip.entity.misc.Emoji.CustomEmoji;
import com.mewna.catnip.entity.misc.Emoji.UnicodeEmoji;
import com.mewna.catnip.entity.user.Presence;
import com.mewna.catnip.entity.user.Presence.*;
import com.mewna.catnip.entity.user.TypingUser;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.entity.user.VoiceState;
import com.mewna.catnip.entity.util.Permission;
import com.mewna.catnip.entity.voice.VoiceServerUpdate;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author natanbc
 * @since 9/2/18.
 */
@SuppressWarnings({"WeakerAccess", "unused", "OverlyCoupledClass"})
public final class EntityBuilder {
    private static final JsonArray EMPTY_JSON_ARRAY = new JsonArray();
    
    @SuppressWarnings("FieldCanBeLocal")
    private final Catnip catnip;
    
    public EntityBuilder(final Catnip catnip) {
        this.catnip = catnip;
    }
    
    @CheckReturnValue
    private static boolean isInvalid(@Nullable final JsonObject object, @Nonnull final String key) {
        return object == null || !object.containsKey(key);
    }
    
    @Nonnull
    @CheckReturnValue
    public static <T> List<T> immutableListOf(@Nullable final JsonArray array, @Nonnull final Function<JsonObject, T> mapper) {
        if(array == null) {
            return ImmutableList.of();
        }
        final Collection<T> ret = new ArrayList<>(array.size());
        for(final Object object : array) {
            if(!(object instanceof JsonObject)) {
                throw new IllegalArgumentException("Expected all values to be JsonObjects, but found " +
                        (object == null ? "null" : object.getClass()));
            }
            ret.add(mapper.apply((JsonObject) object));
        }
        return ImmutableList.copyOf(ret);
    }
    
    public static <T> Map<String, T> immutableMapOf(@Nullable final JsonArray array,
                                                    @Nonnull final Function<JsonObject, String> keyFunction,
                                                    @Nonnull final Function<JsonObject, T> mapper) {
        if(array == null) {
            return ImmutableMap.of();
        }
        
        final Map<String, T> map = new HashMap<>(array.size());
        
        for(final Object object : array) {
            if(!(object instanceof JsonObject)) {
                throw new IllegalArgumentException("Expected all values to be JsonObjects, but found " +
                        (object == null ? "null" : object.getClass()));
            }
            final JsonObject jsonObject = (JsonObject) object;
            final String key = keyFunction.apply(jsonObject);
            if(key == null || key.isEmpty()) {
                throw new IllegalArgumentException("keyFunction returned null or empty string, which isn't allowed!");
            }
            map.put(key, mapper.apply(jsonObject));
        }
        return ImmutableMap.copyOf(map);
    }
    
    @Nonnull
    @CheckReturnValue
    public static List<String> stringListOf(@Nullable final JsonArray array) {
        if(array == null) {
            return Collections.emptyList();
        }
        final Collection<String> ret = new ArrayList<>(array.size());
        for(final Object object : array) {
            if(!(object instanceof String)) {
                throw new IllegalArgumentException("Expected all values to be strings, but found " +
                        (object == null ? "null" : object.getClass()));
            }
            ret.add((String) object);
        }
        return ImmutableList.copyOf(ret);
    }
    
    @Nullable
    @CheckReturnValue
    private static OffsetDateTime parseTimestamp(@Nullable final CharSequence raw) {
        return raw == null ? null : OffsetDateTime.parse(raw);
    }
    
    @Nonnull
    @CheckReturnValue
    private static JsonObject embedFooterToJson(final Footer footer) {
        return new JsonObject().put("icon_url", footer.iconUrl()).put("text", footer.text());
    }
    
    @Nonnull
    @CheckReturnValue
    private static JsonObject embedImageToJson(final Image image) {
        return new JsonObject().put("url", image.url());
    }
    
    @Nonnull
    @CheckReturnValue
    private static JsonObject embedThumbnailToJson(final Thumbnail thumbnail) {
        return new JsonObject().put("url", thumbnail.url());
    }
    
    @Nonnull
    @CheckReturnValue
    private static JsonObject embedAuthorToJson(final Author author) {
        return new JsonObject().put("name", author.name()).put("url", author.url()).put("icon_url", author.iconUrl());
    }
    
    @Nonnull
    @CheckReturnValue
    private static JsonObject embedFieldToJson(final Field field) {
        return new JsonObject().put("name", field.name()).put("value", field.value()).put("inline", field.inline());
    }
    
    @Nonnull
    @CheckReturnValue
    private static FieldImpl createField(@Nonnull final JsonObject data) {
        return FieldImpl.builder()
                .name(data.getString("name"))
                .value(data.getString("value"))
                .inline(data.getBoolean("inline", false))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    @SuppressWarnings("ConstantConditions")
    public JsonObject embedToJson(final Embed embed) {
        final JsonObject o = new JsonObject();
        
        if(embed.title() != null) {
            o.put("title", embed.title());
        }
        if(embed.description() != null) {
            o.put("description", embed.description());
        }
        if(embed.url() != null) {
            o.put("url", embed.url());
        }
        if(embed.color() != null) {
            o.put("color", embed.color());
        }
        if(embed.footer() != null) {
            o.put("footer", embedFooterToJson(embed.footer()));
        }
        if(embed.image() != null) {
            o.put("image", embedImageToJson(embed.image()));
        }
        if(embed.thumbnail() != null) {
            o.put("thumbnail", embedThumbnailToJson(embed.thumbnail()));
        }
        if(embed.author() != null) {
            o.put("author", embedAuthorToJson(embed.author()));
        }
        if(!embed.fields().isEmpty()) {
            o.put("fields", new JsonArray(embed.fields().stream().map(EntityBuilder::embedFieldToJson).collect(Collectors.toList())));
        }
        
        return o;
    }
    
    @Nonnull
    @CheckReturnValue
    public Embed createEmbed(final JsonObject data) {
        final JsonObject footerRaw = data.getJsonObject("footer");
        final FooterImpl footer = isInvalid(footerRaw, "text") ? null : FooterImpl.builder()
                .text(footerRaw.getString("text"))
                .iconUrl(footerRaw.getString("icon_url"))
                .proxyIconUrl(footerRaw.getString("proxy_icon_url"))
                .build();
        
        final JsonObject imageRaw = data.getJsonObject("image");
        final ImageImpl image = isInvalid(imageRaw, "url") ? null : ImageImpl.builder()
                .url(imageRaw.getString("url"))
                .proxyUrl(imageRaw.getString("proxy_url"))
                .height(imageRaw.getInteger("height", -1))
                .width(imageRaw.getInteger("width", -1))
                .build();
        
        final JsonObject thumbnailRaw = data.getJsonObject("thumbnail");
        final ThumbnailImpl thumbnail = isInvalid(thumbnailRaw, "url") ? null : ThumbnailImpl.builder()
                .url(thumbnailRaw.getString("url"))
                .proxyUrl(thumbnailRaw.getString("proxy_url"))
                .height(thumbnailRaw.getInteger("height", -1))
                .width(thumbnailRaw.getInteger("width", -1))
                .build();
        
        final JsonObject videoRaw = data.getJsonObject("video");
        final VideoImpl video = isInvalid(videoRaw, "url") ? null : VideoImpl.builder()
                .url(videoRaw.getString("url"))
                .height(videoRaw.getInteger("height", -1))
                .width(videoRaw.getInteger("width", -1))
                .build();
        
        final JsonObject providerRaw = data.getJsonObject("provider");
        final ProviderImpl provider = isInvalid(providerRaw, "url") ? null : ProviderImpl.builder()
                .name(providerRaw.getString("name"))
                .url(providerRaw.getString("url"))
                .build();
        
        final JsonObject authorRaw = data.getJsonObject("author");
        final AuthorImpl author = isInvalid(authorRaw, "name") ? null : AuthorImpl.builder()
                .name(authorRaw.getString("name"))
                .url(authorRaw.getString("url"))
                .iconUrl(authorRaw.getString("icon_url"))
                .proxyIconUrl(authorRaw.getString("proxy_icon_url"))
                .build();
        
        return EmbedImpl.builder()
                .title(data.getString("title"))
                .type(EmbedType.byKey(data.getString("type")))
                .description(data.getString("description"))
                .url(data.getString("url"))
                .timestamp(data.getString("timestamp"))
                .color(data.getInteger("color", null))
                .footer(footer)
                .image(image)
                .thumbnail(thumbnail)
                .video(video)
                .provider(provider)
                .author(author)
                .fields(immutableListOf(data.getJsonArray("fields"), EntityBuilder::createField))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public TextChannel createTextChannel(@Nonnull final JsonObject data) {
        return createTextChannel(data.getString("guild_id"), data);
    }
    
    @Nonnull
    @CheckReturnValue
    public TextChannel createTextChannel(@Nullable final String guildId, @Nonnull final JsonObject data) {
        return TextChannelImpl.builder()
                .catnip(catnip)
                .id(data.getString("id"))
                .type(ChannelType.TEXT)
                .name(data.getString("name"))
                .guildId(guildId)
                .position(data.getInteger("position", -1))
                .parentId(data.getString("parent_id"))
                .overrides(immutableListOf(data.getJsonArray("permission_overwrites"), this::createPermissionOverride))
                .topic(data.getString("topic"))
                .nsfw(data.getBoolean("nsfw", false))
                .rateLimitPerUser(data.getInteger("rate_limit_per_user", 0))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public VoiceChannel createVoiceChannel(@Nonnull final JsonObject data) {
        return createVoiceChannel(data.getString("guild_id"), data);
    }
    
    @Nonnull
    @CheckReturnValue
    public VoiceChannel createVoiceChannel(@Nullable final String guildId, @Nonnull final JsonObject data) {
        return VoiceChannelImpl.builder()
                .catnip(catnip)
                .id(data.getString("id"))
                .type(ChannelType.VOICE)
                .name(data.getString("name"))
                .guildId(guildId)
                .position(data.getInteger("position", -1))
                .parentId(data.getString("parent_id"))
                .overrides(immutableListOf(data.getJsonArray("permission_overwrites"), this::createPermissionOverride))
                .bitrate(data.getInteger("bitrate", 0))
                .userLimit(data.getInteger("user_limit", 0))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public Category createCategory(@Nonnull final JsonObject data) {
        return createCategory(data.getString("guild_id"), data);
    }
    
    @Nonnull
    @CheckReturnValue
    public Category createCategory(@Nullable final String guildId, @Nonnull final JsonObject data) {
        return CategoryImpl.builder()
                .catnip(catnip)
                .id(data.getString("id"))
                .type(ChannelType.CATEGORY)
                .name(data.getString("name"))
                .guildId(guildId)
                .position(data.getInteger("position", -1))
                .parentId(data.getString("parent_id"))
                .overrides(immutableListOf(data.getJsonArray("permission_overwrites"), this::createPermissionOverride))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public UserDMChannel createUserDM(@Nonnull final JsonObject data) {
        return UserDMChannelImpl.builder()
                .catnip(catnip)
                .id(data.getString("id"))
                .type(ChannelType.VOICE)
                .userId(data.getJsonArray("recipients").getJsonObject(0).getString("id"))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public GroupDMChannel createGroupDM(@Nonnull final JsonObject data) {
        return GroupDMChannelImpl.builder()
                .catnip(catnip)
                .id(data.getString("id"))
                .type(ChannelType.VOICE)
                .recipients(immutableListOf(data.getJsonArray("recipients"), this::createUser))
                .icon(data.getString("icon"))
                .ownerId(data.getString("owner_id"))
                .applicationId(data.getString("application_id"))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public GuildChannel createGuildChannel(@Nonnull final JsonObject data) {
        return createGuildChannel(data.getString("guild_id"), data);
    }
    
    @Nonnull
    @CheckReturnValue
    public GuildChannel createGuildChannel(@Nonnull final String guildId, @Nonnull final JsonObject data) {
        final ChannelType type = ChannelType.byKey(data.getInteger("type"));
        switch(type) {
            case TEXT:
                return createTextChannel(guildId, data);
            case VOICE:
                return createVoiceChannel(guildId, data);
            case CATEGORY:
                return createCategory(guildId, data);
            default:
                throw new UnsupportedOperationException("Unsupported channel type " + type);
        }
    }
    
    @Nonnull
    @CheckReturnValue
    public DMChannel createDMChannel(@Nonnull final JsonObject data) {
        final ChannelType type = ChannelType.byKey(data.getInteger("type"));
        switch(type) {
            case DM:
                return createUserDM(data);
            case GROUP_DM:
                return createGroupDM(data);
            default:
                throw new UnsupportedOperationException("Unsupported channel type " + type);
        }
    }
    
    @Nonnull
    @CheckReturnValue
    public Channel createChannel(@Nonnull final JsonObject data) {
        final ChannelType type = ChannelType.byKey(data.getInteger("type"));
        if(type.isGuild()) {
            return createGuildChannel(data);
        } else {
            return createDMChannel(data);
        }
    }
    
    @Nonnull
    @CheckReturnValue
    public ChannelPinsUpdate createChannelPinsUpdate(@Nonnull final JsonObject data) {
        return ChannelPinsUpdateImpl.builder()
                .catnip(catnip)
                .channelId(data.getString("channel_id"))
                .lastPinTimestamp(data.getString("last_pin_timestamp"))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public PermissionOverride createPermissionOverride(@Nonnull final JsonObject data) {
        return PermissionOverrideImpl.builder()
                .catnip(catnip)
                .id(data.getString("id"))
                .type(OverrideType.byKey(data.getString("type")))
                .allow(Permission.toSet(data.getLong("allow", 0L)))
                .deny(Permission.toSet(data.getLong("deny", 0L)))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public Role createRole(@Nonnull final String guildId, @Nonnull final JsonObject data) {
        return RoleImpl.builder()
                .catnip(catnip)
                .id(data.getString("id"))
                .guildId(guildId)
                .name(data.getString("name"))
                .color(data.getInteger("color"))
                .hoist(data.getBoolean("hoist"))
                .position(data.getInteger("position"))
                .permissions(Permission.toSet(data.getLong("permissions")))
                .managed(data.getBoolean("managed"))
                .mentionable(data.getBoolean("mentionable"))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public PartialRole createPartialRole(@Nonnull final String guildId, @Nonnull final String roleId) {
        return PartialRoleImpl.builder()
                .catnip(catnip)
                .guildId(guildId)
                .id(roleId)
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public User createUser(@Nonnull final JsonObject data) {
        return UserImpl.builder()
                .catnip(catnip)
                .username(data.getString("username"))
                .id(data.getString("id"))
                .discriminator(data.getString("discriminator"))
                .avatar(data.getString("avatar", null))
                .bot(data.getBoolean("bot", false))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public Presence createPresence(@Nonnull final JsonObject data) {
        return PresenceImpl.builder()
                .catnip(catnip)
                .status(OnlineStatus.fromString(data.getString("status")))
                .activity(createActivity(data.getJsonObject("game", null)))
                .build();
    }
    
    @Nullable
    @CheckReturnValue
    public Activity createActivity(@Nullable final JsonObject data) {
        if(data == null) {
            return null;
        } else {
            return ActivityImpl.builder()
                    .name(data.getString("name"))
                    .type(ActivityType.byId(data.getInteger("type")))
                    .url(data.getString("url"))
                    .timestamps(createTimestamps(data.getJsonObject("timestamps", null)))
                    .applicationId(data.getString("application_id"))
                    .details(data.getString("details"))
                    .state(data.getString("state"))
                    .party(createParty(data.getJsonObject("party", null)))
                    .assets(createAssets(data.getJsonObject("assets", null)))
                    .secrets(createSecrets(data.getJsonObject("secrets", null)))
                    .instance(data.getBoolean("instance", false))
                    .flags(ActivityFlag.fromInt(data.getInteger("flags", 0)))
                    .build();
        }
    }
    
    @Nullable
    @CheckReturnValue
    public ActivityTimestamps createTimestamps(@Nullable final JsonObject data) {
        if(data == null) {
            return null;
        } else {
            return ActivityTimestampsImpl.builder()
                    .start(data.getLong("start", -1L))
                    .end(data.getLong("end", -1L))
                    .build();
        }
    }
    
    @Nullable
    @CheckReturnValue
    public ActivityParty createParty(@Nullable final JsonObject data) {
        if(data == null) {
            return null;
        } else {
            final JsonArray size = data.getJsonArray("size", new JsonArray(Arrays.asList(-1, -1)));
            return ActivityPartyImpl.builder()
                    .id(data.getString("id"))
                    // Initialized to -1 if doesn't exist
                    .currentSize(size.getInteger(0))
                    .maxSize(size.getInteger(1))
                    .build();
        }
    }
    
    @Nullable
    @CheckReturnValue
    public ActivityAssets createAssets(@Nullable final JsonObject data) {
        if(data == null) {
            return null;
        } else {
            return ActivityAssetsImpl.builder()
                    .largeImage(data.getString("large_image"))
                    .largeText(data.getString("large_text"))
                    .smallImage(data.getString("small_image"))
                    .smallText(data.getString("small_text"))
                    .build();
        }
    }
    
    @Nullable
    @CheckReturnValue
    public ActivitySecrets createSecrets(@Nullable final JsonObject data) {
        if(data == null) {
            return null;
        } else {
            return ActivitySecretsImpl.builder()
                    .join(data.getString("join"))
                    .spectate(data.getString("spectate"))
                    .match(data.getString("match"))
                    .build();
        }
    }
    
    @Nonnull
    @CheckReturnValue
    public TypingUser createTypingUser(@Nonnull final JsonObject data) {
        return TypingUserImpl.builder()
                .catnip(catnip)
                .id(data.getString("user_id"))
                .channelId(data.getString("channel_id"))
                .guildId(data.getString("guild_id"))
                .timestamp(data.getLong("timestamp"))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public Member createMember(@Nonnull final String guildId, @Nonnull final String id, @Nonnull final JsonObject data) {
        final JsonObject userData = data.getJsonObject("user");
        if(userData != null) {
            catnip.cacheWorker().bulkCacheUsers(Collections.singletonList(createUser(userData)));
        }
        final String joinedAt;
        if(data.getString("joined_at", null) != null) {
            joinedAt = data.getString("joined_at");
        } else {
            // This will only happen during GUILD_MEMBER_REMOVE afaik, but is this the right solution?
            final Member cachedMember = catnip.cache().member(guildId, id);
            if(cachedMember != null && cachedMember.joinedAt() != null) {
                // Guaranteed not null by preceding if
                //noinspection ConstantConditions
                joinedAt = cachedMember.joinedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            } else {
                joinedAt = null;
            }
        }
        
        
        return MemberImpl.builder()
                .catnip(catnip)
                .id(id)
                .guildId(guildId)
                .nick(data.getString("nick"))
                // This is safe
                .roleIds(ImmutableSet.copyOf(data.getJsonArray("roles", new JsonArray())
                        .stream()
                        .map(e -> (String) e)
                        .collect(Collectors.toSet())))
                .joinedAt(joinedAt)
                // If not present, it's probably(?) safe to assume not
                .deaf(data.getBoolean("deaf", false))
                .mute(data.getBoolean("mute", false))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public Member createMember(@Nonnull final String guildId, @SuppressWarnings("TypeMayBeWeakened") @Nonnull final User user,
                               @Nonnull final JsonObject data) {
        return createMember(guildId, user.id(), data);
    }
    
    @Nonnull
    @CheckReturnValue
    public Member createMember(@Nonnull final String guildId, @Nonnull final JsonObject data) {
        return createMember(guildId, createUser(data.getJsonObject("user")), data);
    }
    
    @Nonnull
    @CheckReturnValue
    public PartialMember createPartialMember(@Nonnull final String guild, @Nonnull final JsonObject data) {
        return PartialMemberImpl.builder()
                .catnip(catnip)
                .guildId(guild)
                .user(createUser(data.getJsonObject("user")))
                .roleIds(ImmutableSet.copyOf(data.getJsonArray("roles").stream().map(e -> (String) e).collect(Collectors.toSet())))
                .nick(data.getString("nick"))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public VoiceState createVoiceState(@Nullable final String guildId, @Nonnull final JsonObject data) {
        return VoiceStateImpl.builder()
                .catnip(catnip)
                .guildId(guildId)
                .channelId(data.getString("channel_id"))
                .userId(data.getString("user_id"))
                .sessionId(data.getString("session_id"))
                .deaf(data.getBoolean("deaf"))
                .mute(data.getBoolean("mute"))
                .selfDeaf(data.getBoolean("self_deaf"))
                .selfMute(data.getBoolean("self_mute"))
                .suppress(data.getBoolean("suppress"))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public VoiceState createVoiceState(@Nonnull final JsonObject data) {
        return createVoiceState(data.getString("guild_id"), data);
    }
    
    @Nonnull
    @CheckReturnValue
    public VoiceServerUpdate createVoiceServerUpdate(@Nonnull final JsonObject data) {
        return VoiceServerUpdateImpl.builder()
                .catnip(catnip)
                .token(data.getString("token"))
                .guildId(data.getString("guild_id"))
                .endpoint(data.getString("endpoint"))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public UnicodeEmoji createUnicodeEmoji(@Nonnull final JsonObject data) {
        return UnicodeEmojiImpl.builder()
                .catnip(catnip)
                .name(data.getString("name"))
                .requiresColons(data.getBoolean("require_colons", true))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public CustomEmoji createCustomEmoji(@Nullable final String guildId, @Nonnull final JsonObject data) {
        final JsonObject userRaw = data.getJsonObject("user");
        
        return CustomEmojiImpl.builder()
                .catnip(catnip)
                .id(data.getString("id"))
                .guildId(guildId)
                .name(data.getString("name"))
                .roles(stringListOf(data.getJsonArray("roles")))
                .user(userRaw == null ? null : createUser(userRaw))
                .requiresColons(data.getBoolean("require_colons", true))
                .managed(data.getBoolean("managed", false))
                .animated(data.getBoolean("animated", false))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public Emoji createEmoji(@Nullable final String guildId, @Nonnull final JsonObject data) {
        // If it has an id, then it has a guild attached, so the @Nonnull warning can be ignored
        //noinspection ConstantConditions
        return data.getValue("id") == null ? createUnicodeEmoji(data) : createCustomEmoji(guildId, data);
    }
    
    @Nonnull
    @CheckReturnValue
    public EmojiUpdate createGuildEmojisUpdate(@Nonnull final JsonObject data) {
        return EmojiUpdateImpl.builder()
                .catnip(catnip)
                .guildId(data.getString("guild_id"))
                .emojis(immutableListOf(data.getJsonArray("emojis"),
                        e -> createCustomEmoji(data.getString("guild_id"), e)))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public Attachment createAttachment(@Nonnull final JsonObject data) {
        return AttachmentImpl.builder()
                .catnip(catnip)
                .id(data.getString("id"))
                .fileName(data.getString("filename"))
                .size(data.getInteger("size"))
                .url(data.getString("url"))
                .proxyUrl(data.getString("proxy_url"))
                .height(data.getInteger("height", -1))
                .width(data.getInteger("width", -1))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public Reaction createReaction(@Nonnull final String guildId, @Nonnull final JsonObject data) {
        return ReactionImpl.builder()
                .count(data.getInteger("count"))
                .self(data.getBoolean("self", false))
                .emoji(createEmoji(guildId, data.getJsonObject("emoji")))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public ReactionUpdate createReactionUpdate(@Nonnull final JsonObject data) {
        return ReactionUpdateImpl.builder()
                .catnip(catnip)
                .userId(data.getString("user_id"))
                .channelId(data.getString("channel_id"))
                .messageId(data.getString("message_id"))
                .guildId(data.getString("guild_id"))
                .emoji(createEmoji(null, data.getJsonObject("emoji")))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public BulkRemovedReactions createBulkRemovedReactions(@Nonnull final JsonObject data) {
        return BulkRemovedReactionsImpl.builder()
                .catnip(catnip)
                .channelId(data.getString("channel_id"))
                .messageId(data.getString("message_id"))
                .guildId(data.getString("guild_id"))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public Message createMessage(@Nonnull final JsonObject data) {
        final User author = createUser(data.getJsonObject("author"));
        
        final JsonObject memberRaw = data.getJsonObject("member");
        // If member exists, guild_id must also exist
        final Member member = memberRaw == null ? null : createMember(data.getString("guild_id"), author, memberRaw);
        
        return MessageImpl.builder()
                .catnip(catnip)
                .id(data.getString("id"))
                .channelId(data.getString("channel_id"))
                .author(author)
                .content(data.getString("content"))
                .timestamp(data.getString("timestamp"))
                .editedTimestamp(data.getString("edited_timestamp"))
                .tts(data.getBoolean("tts", false))
                .mentionsEveryone(data.getBoolean("mention_everyone", false))
                .mentionedUsers(immutableListOf(data.getJsonArray("mentions"), this::createUser))
                .mentionedRoles(stringListOf(data.getJsonArray("mention_roles")))
                .attachments(immutableListOf(data.getJsonArray("attachments"), this::createAttachment))
                .embeds(immutableListOf(data.getJsonArray("embeds"), this::createEmbed))
                .reactions(immutableListOf(data.getJsonArray("reactions"), e -> createReaction(data.getString("guild_id"), e)))
                .nonce(String.valueOf(data.getValue("nonce")))
                .pinned(data.getBoolean("pinned", false))
                .webhookId(data.getString("webhook_id"))
                .type(MessageType.byId(data.getInteger("type", MessageType.DEFAULT.getId())))
                .member(member)
                .guildId(data.getString("guild_id"))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public MessageEmbedUpdate createMessageEmbedUpdate(final JsonObject data) {
        return MessageEmbedUpdateImpl.builder()
                .catnip(catnip)
                .id(data.getString("id"))
                .guildId(data.getString("guild_id"))
                .channelId(data.getString("channel_id"))
                .embeds(immutableListOf(data.getJsonArray("embeds"), this::createEmbed))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public GuildEmbed createGuildEmbed(@Nonnull final JsonObject data) {
        return GuildEmbedImpl.builder()
                .catnip(catnip)
                .channelId(data.getString("channel_id", null))
                .enabled(data.getBoolean("enabled"))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public Guild createGuild(@Nonnull final JsonObject data) {
        return createGuild(data, true);
    }
    
    @Nonnull
    @CheckReturnValue
    public Guild createGuild(@Nonnull final JsonObject data, final boolean cache) {
        // As we don't store these fields on the guild object itself, we have
        // to update them in the cache
        final String id = data.getString("id"); //optimization
        if(cache) {
            if(data.getJsonArray("roles") != null) {
                catnip.cacheWorker().bulkCacheRoles(immutableListOf(data.getJsonArray("roles"),
                        e -> createRole(id, e)));
            }
            if(data.getJsonArray("channels") != null) {
                catnip.cacheWorker().bulkCacheChannels(immutableListOf(data.getJsonArray("channels"),
                        e -> createGuildChannel(id, e)));
            }
            if(data.getJsonArray("members") != null) {
                catnip.cacheWorker().bulkCacheMembers(immutableListOf(data.getJsonArray("members"),
                        e -> createMember(id, e)));
            }
            if(data.getJsonArray("emojis") != null) {
                catnip.cacheWorker().bulkCacheEmoji(immutableListOf(data.getJsonArray("emojis"),
                        e -> createCustomEmoji(id, e)));
            }
            if(data.getJsonArray("presences") != null) {
                catnip.cacheWorker().bulkCachePresences(immutableMapOf(data.getJsonArray("presences"),
                        o -> o.getJsonObject("user").getString("id"), this::createPresence));
            }
            if(data.getJsonArray("voice_states") != null) {
                catnip.cacheWorker().bulkCacheVoiceStates(immutableListOf(
                        data.getJsonArray("voice_states"), e -> createVoiceState(id, e)));
            }
        }
        return GuildImpl.builder()
                .catnip(catnip)
                .id(id)
                .name(data.getString("name"))
                .icon(data.getString("icon"))
                .splash(data.getString("splash"))
                .owned(data.getBoolean("owner", false))
                .ownerId(data.getString("owner_id"))
                .permissions(Permission.toSet(data.getLong("permissions", 0L)))
                .region(data.getString("region"))
                .afkChannelId(data.getString("afk_channel_id", null))
                .afkTimeout(data.getInteger("afk_timeout", 0))
                .embedEnabled(data.getBoolean("embed_enabled", false))
                .embedChannelId(data.getString("embed_channel_id"))
                .verificationLevel(VerificationLevel.byKey(data.getInteger("verification_level", 0)))
                .defaultMessageNotifications(NotificationLevel.byKey(data.getInteger("default_message_notifications", 0)))
                .explicitContentFilter(ContentFilterLevel.byKey(data.getInteger("explicit_content_filter", 0)))
                .features(stringListOf(data.getJsonArray("features")))
                .mfaLevel(MFALevel.byKey(data.getInteger("mfa_level", 0)))
                .applicationId(data.getString("application_id"))
                .widgetEnabled(data.getBoolean("widget_enabled", false))
                .widgetChannelId(data.getString("widget_channel_id"))
                .systemChannelId(data.getString("system_channel_id"))
                .joinedAt(data.getString("joined_at"))
                .large(data.getBoolean("large", false))
                .unavailable(data.getBoolean("unavailable", false))
                .memberCount(data.getInteger("member_count", -1))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public UnavailableGuild createUnavailableGuild(@Nonnull final JsonObject data) {
        return UnavailableGuildImpl.builder()
                .catnip(catnip)
                .id(data.getString("id"))
                .unavailable(data.getBoolean("unavailable"))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public PartialGuild createPartialGuild(@Nonnull final JsonObject data) {
        return PartialGuildImpl.builder()
                .catnip(catnip)
                .id(data.getString("id"))
                .name(data.getString("name"))
                .icon(data.getString("icon"))
                .owned(data.getBoolean("owner", false))
                .permissions(Permission.toSet(data.getLong("permissions", 0L)))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public GatewayGuildBan createGatewayGuildBan(@Nonnull final JsonObject data) {
        return GatewayGuildBanImpl.builder()
                .catnip(catnip)
                .guildId(data.getString("guild_id"))
                .user(createUser(data.getJsonObject("user")))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public GuildBan createGuildBan(@Nonnull final JsonObject data) {
        return GuildBanImpl.builder()
                .catnip(catnip)
                .reason(data.getString("reason"))
                .user(createUser(data.getJsonObject("user")))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public Invite createInvite(@Nonnull final JsonObject data) {
        if(data.containsKey("uses")) {
            return createCreatedInvite(data);
        }
        return InviteImpl.builder()
                .catnip(catnip)
                .code(data.getString("code"))
                .inviter(createInviter(data.getJsonObject("inviter")))
                .guild(createInviteGuild(data.getJsonObject("guild")))
                .channel(createInviteChannel(data.getJsonObject("channel")))
                .approximatePresenceCount(data.getInteger("approximate_presence_count", -1))
                .approximateMemberCount(data.getInteger("approximate_member_count", -1))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public CreatedInvite createCreatedInvite(@Nonnull final JsonObject data) {
        return CreatedInviteImpl.builder()
                .catnip(catnip)
                .code(data.getString("code"))
                .inviter(createInviter(data.getJsonObject("inviter")))
                .guild(createInviteGuild(data.getJsonObject("guild")))
                .channel(createInviteChannel(data.getJsonObject("channel")))
                .approximatePresenceCount(data.getInteger("approximate_presence_count", -1))
                .approximateMemberCount(data.getInteger("approximate_member_count", -1))
                .uses(data.getInteger("uses"))
                .maxUses(data.getInteger("max_uses"))
                .maxAge(data.getInteger("max_age"))
                .temporary(data.getBoolean("temporary", false))
                .createdAt(data.getString("created_at"))
                .revoked(data.getBoolean("revoked", false))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public InviteChannel createInviteChannel(@Nonnull final JsonObject data) {
        return InviteChannelImpl.builder()
                .catnip(catnip)
                .id(data.getString("id"))
                .name(data.getString("name"))
                .type(ChannelType.byKey(data.getInteger("type")))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public InviteGuild createInviteGuild(@Nonnull final JsonObject data) {
        return InviteGuildImpl.builder()
                .catnip(catnip)
                .id(data.getString("id"))
                .name(data.getString("name"))
                .icon(data.getString("icon"))
                .splash(data.getString("splash"))
                .features(stringListOf(data.getJsonArray("features")))
                .verificationLevel(VerificationLevel.byKey(data.getInteger("verification_level", 0)))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public Inviter createInviter(@Nonnull final JsonObject data) {
        return InviterImpl.builder()
                .catnip(catnip)
                .id(data.getString("id"))
                .username(data.getString("username"))
                .discriminator(data.getString("discriminator"))
                .avatar(data.getString("avatar"))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public VoiceRegion createVoiceRegion(@Nonnull final JsonObject data) {
        return VoiceRegionImpl.builder()
                .catnip(catnip)
                .id(data.getString("id"))
                .name(data.getString("name"))
                .vip(data.getBoolean("vip", false))
                .optimal(data.getBoolean("optimal", false))
                .deprecated(data.getBoolean("deprecated", false))
                .custom(data.getBoolean("custom", false))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public Webhook createWebhook(@Nonnull final JsonObject data) {
        return WebhookImpl.builder()
                .catnip(catnip)
                .id(data.getString("id"))
                .guildId(data.getString("guild_id"))
                .channelId(data.getString("channel_id"))
                .user(createUser(data.getJsonObject("user")))
                .name(data.getString("name"))
                .avatar(data.getString("avatar"))
                .token(data.getString("token"))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public WebhooksUpdate createWebhooksUpdate(@Nonnull final JsonObject data) {
        return WebhooksUpdateImpl.builder()
                .catnip(catnip)
                .guildId(data.getString("guild_id"))
                .channelId(data.getString("channel_id"))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public DeletedMessage createDeletedMessage(@Nonnull final JsonObject data) {
        return DeletedMessageImpl.builder()
                .catnip(catnip)
                .id(data.getString("id"))
                .channelId(data.getString("channel_id"))
                .guildId(data.getString("guild_id"))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public BulkDeletedMessages createBulkDeletedMessages(@Nonnull final JsonObject data) {
        return BulkDeletedMessagesImpl.builder()
                .catnip(catnip)
                .ids(ImmutableList.copyOf(data.getJsonArray("ids").stream().map(e -> (String) e).collect(Collectors.toList())))
                .channelId(data.getString("channel_id"))
                .guildId(data.getString("guild_id"))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public Ready createReady(@Nonnull final JsonObject data) {
        return ReadyImpl.builder()
                .catnip(catnip)
                .version(data.getInteger("v"))
                .user(createUser(data.getJsonObject("user")))
                .trace(ImmutableList.copyOf(data.getJsonArray("_trace")
                        .stream()
                        .map(e -> (String) e)
                        .toArray(String[]::new)))
                .guilds(ImmutableSet.copyOf(data.getJsonArray("guilds")
                        .stream()
                        .map(e -> (JsonObject) e)
                        .map(this::createUnavailableGuild)
                        .toArray(UnavailableGuild[]::new)))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public Resumed createResumed(@Nonnull final JsonObject data) {
        return ResumedImpl.builder()
                .catnip(catnip)
                .trace(ImmutableList.copyOf(data.getJsonArray("_trace").stream().map(e -> (String) e).collect(Collectors.toList())))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public AuditLogChange createAuditLogChange(@Nonnull final JsonObject data) {
        return AuditLogChangeImpl.builder()
                .catnip(catnip)
                .key(data.getString("key"))
                .newValue(data.getValue("new_value")) // no npe if null/optional key
                .oldValue(data.getValue("old_value"))
                .build();
    }
    
    @Nullable
    @CheckReturnValue
    public OptionalEntryInfo createOptionalEntryInfo(@Nonnull final JsonObject data, @Nonnull final ActionType type) {
        switch(type) {
            case MEMBER_PRUNE:
                return MemberPruneInfoImpl.builder()
                        .catnip(catnip)
                        .deleteMemberDays(data.getInteger("delete_member_days"))
                        .removedMembersCount(data.getInteger("members_removed"))
                        .build();
            case MESSAGE_DELETE:
                return MessageDeleteInfoImpl.builder()
                        .catnip(catnip)
                        .channelId(data.getString("channel_id"))
                        .deletedMessagesCount(Integer.parseUnsignedInt(data.getString("count")))
                        .build();
            case CHANNEL_OVERWRITE_CREATE:
            case CHANNEL_OVERWRITE_UPDATE:
            case CHANNEL_OVERWRITE_DELETE:
                return OverrideUpdateInfoImpl.builder()
                        .catnip(catnip)
                        .overriddenEntityId(data.getString("id"))
                        .overrideType(OverrideType.byKey(data.getString("type")))
                        .roleName(data.getString("role_name"))
                        .build();
            default:
                return null;
        }
    }
    
    @Nonnull
    @CheckReturnValue
    public AuditLogEntry createAuditLogEntry(@Nonnull final JsonObject data, @Nonnull final Map<String, Webhook> webhooks,
                                             @Nonnull final Map<String, User> users) {
        final ActionType type = ActionType.byKey(data.getInteger("action_type"));
        return AuditLogEntryImpl.builder()
                .catnip(catnip)
                .id(data.getString("id"))
                .user(users.get(data.getString("user_id")))
                .targetId(data.getString("target_id"))
                .webhook(webhooks.get(data.getString("target_id")))
                .type(type)
                .reason(data.getString("reason"))
                .changes(immutableListOf(data.getJsonArray("changes"), this::createAuditLogChange))
                .options(data.containsKey("options") ? createOptionalEntryInfo(data.getJsonObject("options"), type) : null)
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public List<AuditLogEntry> createAuditLog(@Nonnull final JsonObject data) {
        final Map<String, Webhook> webhooks = immutableMapOf(data.getJsonArray("webhooks"), x -> x.getString("id"), this::createWebhook);
        final Map<String, User> users = immutableMapOf(data.getJsonArray("users"), x -> x.getString("id"), this::createUser);
        
        return immutableListOf(data.getJsonArray("audit_log_entries"), e ->
                createAuditLogEntry(e, webhooks, users)
        );
    }
    
    @Nonnull
    @CheckReturnValue
    public ApplicationInfo createApplicationInfo(@Nonnull final JsonObject data) {
        return ApplicationInfoImpl.builder()
                .catnip(catnip)
                .id(data.getString("id"))
                .name(data.getString("name"))
                .icon(data.getString("icon"))
                .description(data.getString("description"))
                .rpcOrigins(ImmutableList.copyOf(data.getJsonArray("rpc_origins", new JsonArray())
                        .stream()
                        .map(Object::toString)
                        .collect(Collectors.toList())))
                .publicBot(data.getBoolean("bot_public"))
                .requiresCodeGrant(data.getBoolean("bot_require_code_grant"))
                .owner(createUser(data.getJsonObject("owner")))
                .build();
    }
}
