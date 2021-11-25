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

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.channel.*;
import com.mewna.catnip.entity.channel.Channel.ChannelType;
import com.mewna.catnip.entity.channel.ThreadChannel.ThreadMember;
import com.mewna.catnip.entity.channel.ThreadChannel.ThreadMetadata;
import com.mewna.catnip.entity.guild.*;
import com.mewna.catnip.entity.guild.Guild.*;
import com.mewna.catnip.entity.guild.Invite.InviteChannel;
import com.mewna.catnip.entity.guild.Invite.InviteGuild;
import com.mewna.catnip.entity.guild.Invite.Inviter;
import com.mewna.catnip.entity.guild.PermissionOverride.OverrideType;
import com.mewna.catnip.entity.guild.ScheduledEvent.EntityMetadata;
import com.mewna.catnip.entity.guild.ScheduledEvent.EventEntityType;
import com.mewna.catnip.entity.guild.ScheduledEvent.EventStatus;
import com.mewna.catnip.entity.guild.ScheduledEvent.PrivacyLevel;
import com.mewna.catnip.entity.guild.audit.*;
import com.mewna.catnip.entity.impl.channel.*;
import com.mewna.catnip.entity.impl.channel.ThreadChannelImpl.ThreadMemberImpl;
import com.mewna.catnip.entity.impl.channel.ThreadChannelImpl.ThreadMetadataImpl;
import com.mewna.catnip.entity.impl.guild.*;
import com.mewna.catnip.entity.impl.guild.InviteImpl.InviteChannelImpl;
import com.mewna.catnip.entity.impl.guild.InviteImpl.InviteGuildImpl;
import com.mewna.catnip.entity.impl.guild.InviteImpl.InviterImpl;
import com.mewna.catnip.entity.impl.guild.ScheduledEventImpl.EntityMetadataImpl;
import com.mewna.catnip.entity.impl.guild.audit.*;
import com.mewna.catnip.entity.impl.interaction.CustomIdInteractionDataImpl;
import com.mewna.catnip.entity.impl.interaction.InteractionMemberImpl;
import com.mewna.catnip.entity.impl.interaction.command.*;
import com.mewna.catnip.entity.impl.interaction.component.ButtonInteractionImpl;
import com.mewna.catnip.entity.impl.interaction.component.SelectInteractionImpl;
import com.mewna.catnip.entity.impl.interaction.component.SelectInteractionImpl.SelectInteractionDataImpl;
import com.mewna.catnip.entity.impl.message.*;
import com.mewna.catnip.entity.impl.message.EmbedImpl.*;
import com.mewna.catnip.entity.impl.misc.*;
import com.mewna.catnip.entity.impl.sticker.StickerImpl;
import com.mewna.catnip.entity.impl.user.*;
import com.mewna.catnip.entity.impl.user.PresenceImpl.*;
import com.mewna.catnip.entity.impl.voice.VoiceRegionImpl;
import com.mewna.catnip.entity.impl.voice.VoiceServerUpdateImpl;
import com.mewna.catnip.entity.interaction.CustomIdInteractionData;
import com.mewna.catnip.entity.interaction.Interaction;
import com.mewna.catnip.entity.interaction.InteractionMember;
import com.mewna.catnip.entity.interaction.InteractionType;
import com.mewna.catnip.entity.interaction.command.*;
import com.mewna.catnip.entity.interaction.component.SelectInteractionData;
import com.mewna.catnip.entity.message.*;
import com.mewna.catnip.entity.message.Embed.*;
import com.mewna.catnip.entity.message.Message.Attachment;
import com.mewna.catnip.entity.message.Message.MessageActivity;
import com.mewna.catnip.entity.message.Message.MessageApplication;
import com.mewna.catnip.entity.message.Message.Reaction;
import com.mewna.catnip.entity.message.component.MessageComponent.MessageComponentType;
import com.mewna.catnip.entity.misc.*;
import com.mewna.catnip.entity.misc.Emoji.ActivityEmoji;
import com.mewna.catnip.entity.misc.Emoji.CustomEmoji;
import com.mewna.catnip.entity.misc.Emoji.UnicodeEmoji;
import com.mewna.catnip.entity.sticker.Sticker;
import com.mewna.catnip.entity.sticker.StickerFormatType;
import com.mewna.catnip.entity.user.*;
import com.mewna.catnip.entity.user.Presence.*;
import com.mewna.catnip.entity.util.Permission;
import com.mewna.catnip.entity.voice.VoiceRegion;
import com.mewna.catnip.entity.voice.VoiceServerUpdate;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.mewna.catnip.util.JsonUtil.*;

/**
 * @author natanbc
 * @since 9/2/18.
 */
@SuppressWarnings({"WeakerAccess", "OverlyCoupledClass", "DuplicatedCode", "ClassCanBeRecord"})
public final class EntityBuilder {
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
    private static JsonObject embedFooterToJson(final Footer footer) {
        return JsonObject.builder()
                .value("icon_url", footer.iconUrl())
                .value("text", footer.text())
                .done();
    }
    
    @Nonnull
    @CheckReturnValue
    private static JsonObject embedImageToJson(final Image image) {
        return JsonObject.builder()
                .value("url", image.url())
                .done();
    }
    
    @Nonnull
    @CheckReturnValue
    private static JsonObject embedThumbnailToJson(final Thumbnail thumbnail) {
        return JsonObject.builder()
                .value("url", thumbnail.url())
                .done();
    }
    
    @Nonnull
    @CheckReturnValue
    private static JsonObject embedAuthorToJson(final Author author) {
        return JsonObject.builder()
                .value("name", author.name())
                .value("url", author.url())
                .value("icon_url", author.iconUrl())
                .done();
    }
    
    @Nonnull
    @CheckReturnValue
    private static JsonObject embedFieldToJson(final Field field) {
        return JsonObject.builder()
                .value("name", field.name())
                .value("value", field.value())
                .value("inline", field.inline())
                .done();
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
    private <T, R extends T> R delegate(@Nonnull final Class<T> type, @Nonnull final T data) {
        return catnip.options().entityDelegator().delegate(type, data);
    }
    
    @Nonnull
    @CheckReturnValue
    @SuppressWarnings("ConstantConditions")
    public JsonObject embedToJson(final Embed embed) {
        final JsonObject o = new JsonObject();
        final OffsetDateTime timestamp = embed.timestamp(); // to avoid parsing timestamp twice
        if(timestamp != null) {
            o.put("timestamp", timestamp.format(DateTimeFormatter.ISO_INSTANT));
        }
        
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
            final JsonArray array = new JsonArray();
            for(final Field field : embed.fields()) {
                array.add(embedFieldToJson(field));
            }
            o.put("fields", array);
        }
        
        return o;
    }
    
    @Nonnull
    @CheckReturnValue
    public JsonObject referenceToJson(@Nonnull final MessageReference reference) {
        final JsonObject o = new JsonObject();
        o.put("message_id", reference.messageId());
        o.put("channel_id", reference.channelId());
        o.put("guild_id", reference.guildId());
        return o;
    }
    
    @Nonnull
    @CheckReturnValue
    public Embed createEmbed(final JsonObject data) {
        final JsonObject footerRaw = data.getObject("footer");
        final FooterImpl footer = isInvalid(footerRaw, "text") ? null : FooterImpl.builder()
                .text(footerRaw.getString("text"))
                .iconUrl(footerRaw.getString("icon_url"))
                .proxyIconUrl(footerRaw.getString("proxy_icon_url"))
                .build();
        
        final JsonObject imageRaw = data.getObject("image");
        final ImageImpl image = isInvalid(imageRaw, "url") ? null : ImageImpl.builder()
                .url(imageRaw.getString("url"))
                .proxyUrl(imageRaw.getString("proxy_url"))
                .height(imageRaw.getInt("height", -1))
                .width(imageRaw.getInt("width", -1))
                .build();
        
        final JsonObject thumbnailRaw = data.getObject("thumbnail");
        final ThumbnailImpl thumbnail = isInvalid(thumbnailRaw, "url") ? null : ThumbnailImpl.builder()
                .url(thumbnailRaw.getString("url"))
                .proxyUrl(thumbnailRaw.getString("proxy_url"))
                .height(thumbnailRaw.getInt("height", -1))
                .width(thumbnailRaw.getInt("width", -1))
                .build();
        
        final JsonObject videoRaw = data.getObject("video");
        final VideoImpl video = isInvalid(videoRaw, "url") ? null : VideoImpl.builder()
                .url(videoRaw.getString("url"))
                .height(videoRaw.getInt("height", -1))
                .width(videoRaw.getInt("width", -1))
                .build();
        
        final JsonObject providerRaw = data.getObject("provider");
        final ProviderImpl provider = isInvalid(providerRaw, "url") ? null : ProviderImpl.builder()
                .name(providerRaw.getString("name"))
                .url(providerRaw.getString("url"))
                .build();
        
        final JsonObject authorRaw = data.getObject("author");
        final AuthorImpl author = isInvalid(authorRaw, "name") ? null : AuthorImpl.builder()
                .name(authorRaw.getString("name"))
                .url(authorRaw.getString("url"))
                .iconUrl(authorRaw.getString("icon_url"))
                .proxyIconUrl(authorRaw.getString("proxy_icon_url"))
                .build();
        
        return delegate(Embed.class, EmbedImpl.builder()
                .title(data.getString("title"))
                .type(EmbedType.byKey(data.getString("type")))
                .description(data.getString("description"))
                .url(data.getString("url"))
                .timestamp(data.getString("timestamp"))
                .color(data.getInt("color"))
                .footer(footer)
                .image(image)
                .thumbnail(thumbnail)
                .video(video)
                .provider(provider)
                .author(author)
                .fields(toList(data.getArray("fields"), EntityBuilder::createField))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public TextChannel createTextChannel(@Nonnull final String guildId, @Nonnull final JsonObject data) {
        final String parentId = data.getString("parent_id");
        return delegate(TextChannel.class, TextChannelImpl.builder()
                .catnip(catnip)
                .idAsLong(Long.parseUnsignedLong(data.getString("id")))
                .name(data.getString("name"))
                .guildIdAsLong(Long.parseUnsignedLong(guildId))
                .position(data.getInt("position", -1))
                .parentIdAsLong(parentId == null ? 0 : Long.parseUnsignedLong(parentId))
                .overrides(toList(data.getArray("permission_overwrites"), this::createPermissionOverride))
                .topic(data.getString("topic"))
                .nsfw(data.getBoolean("nsfw", false))
                .rateLimitPerUser(data.getInt("rate_limit_per_user", 0))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public NewsChannel createNewsChannel(@Nonnull final String guildId, @Nonnull final JsonObject data) {
        final String parentId = data.getString("parent_id");
        return delegate(NewsChannel.class, NewsChannelImpl.builder()
                .catnip(catnip)
                .idAsLong(Long.parseUnsignedLong(data.getString("id")))
                .name(data.getString("name"))
                .guildIdAsLong(Long.parseUnsignedLong(guildId))
                .position(data.getInt("position", -1))
                .parentIdAsLong(parentId == null ? 0 : Long.parseUnsignedLong(parentId))
                .overrides(toList(data.getArray("permission_overwrites"), this::createPermissionOverride))
                .topic(data.getString("topic"))
                .nsfw(data.getBoolean("nsfw", false))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public StoreChannel createStoreChannel(@Nonnull final String guildId, @Nonnull final JsonObject data) {
        final String parentId = data.getString("parent_id");
        return delegate(StoreChannel.class, StoreChannelImpl.builder()
                .catnip(catnip)
                .idAsLong(Long.parseUnsignedLong(data.getString("id")))
                .name(data.getString("name"))
                .guildIdAsLong(Long.parseUnsignedLong(guildId))
                .position(data.getInt("position", -1))
                .parentIdAsLong(parentId == null ? 0 : Long.parseUnsignedLong(parentId))
                .overrides(toList(data.getArray("permission_overwrites"), this::createPermissionOverride))
                .nsfw(data.getBoolean("nsfw", false))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public VoiceChannel createVoiceChannel(@Nonnull final String guildId, @Nonnull final JsonObject data) {
        final String parentId = data.getString("parent_id");
        return delegate(VoiceChannel.class, VoiceChannelImpl.builder()
                .catnip(catnip)
                .idAsLong(Long.parseUnsignedLong(data.getString("id")))
                .name(data.getString("name"))
                .guildIdAsLong(Long.parseUnsignedLong(guildId))
                .position(data.getInt("position", -1))
                .parentIdAsLong(parentId == null ? 0 : Long.parseUnsignedLong(parentId))
                .overrides(toList(data.getArray("permission_overwrites"), this::createPermissionOverride))
                .bitrate(data.getInt("bitrate", 0))
                .userLimit(data.getInt("user_limit", 0))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public Category createCategory(@Nonnull final String guildId, @Nonnull final JsonObject data) {
        return delegate(Category.class, CategoryImpl.builder()
                .catnip(catnip)
                .idAsLong(Long.parseUnsignedLong(data.getString("id")))
                .name(data.getString("name"))
                .guildIdAsLong(Long.parseUnsignedLong(guildId))
                .position(data.getInt("position", -1))
                .overrides(toList(data.getArray("permission_overwrites"), this::createPermissionOverride))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public UserDMChannel createUserDM(@Nonnull final JsonObject data) {
        return delegate(UserDMChannel.class, UserDMChannelImpl.builder()
                .catnip(catnip)
                .idAsLong(Long.parseUnsignedLong(data.getString("id")))
                .userIdAsLong(Long.parseUnsignedLong(data.getArray("recipients").getObject(0).getString("id")))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public GroupDMChannel createGroupDM(@Nonnull final JsonObject data) {
        return delegate(GroupDMChannel.class, GroupDMChannelImpl.builder()
                .catnip(catnip)
                .idAsLong(Long.parseUnsignedLong(data.getString("id")))
                .recipients(toList(data.getArray("recipients"), this::createUser))
                .icon(data.getString("icon"))
                .ownerIdAsLong(Long.parseUnsignedLong(data.getString("owner_id")))
                .applicationIdAsLong(Long.parseUnsignedLong(data.getString("application_id")))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public GuildChannel createGuildChannel(@Nonnull final JsonObject data) {
        return createGuildChannel(data.getString("guild_id"), data);
    }
    
    @Nonnull
    @CheckReturnValue
    public GuildChannel createGuildChannel(@Nonnull final String guildId, @Nonnull final JsonObject data) {
        final ChannelType type = ChannelType.byKey(data.getInt("type"));
        switch(type) {
            case TEXT -> {
                return createTextChannel(guildId, data);
            }
            case VOICE -> {
                return createVoiceChannel(guildId, data);
            }
            case CATEGORY -> {
                return createCategory(guildId, data);
            }
            case NEWS -> {
                return createNewsChannel(guildId, data);
            }
            case STORE -> {
                return createStoreChannel(guildId, data);
            }
            case NEWS_THREAD, PUBLIC_THREAD, PRIVATE_THREAD -> {
                return createThreadChannel(guildId, data);
            }
            default -> throw new UnsupportedOperationException("Unsupported channel type " + type);
        }
    }
    
    @Nonnull
    @CheckReturnValue
    public DMChannel createDMChannel(@Nonnull final JsonObject data) {
        final ChannelType type = ChannelType.byKey(data.getInt("type"));
        return switch(type) {
            case DM -> createUserDM(data);
            case GROUP_DM -> createGroupDM(data);
            default -> throw new UnsupportedOperationException("Unsupported channel type " + type);
        };
    }
    
    @Nonnull
    @CheckReturnValue
    public Channel createChannel(@Nonnull final JsonObject data) {
        final ChannelType type = ChannelType.byKey(data.getInt("type"));
        if(type.guild()) {
            return createGuildChannel(data);
        } else {
            return createDMChannel(data);
        }
    }
    
    @Nonnull
    @CheckReturnValue
    public ChannelPinsUpdate createChannelPinsUpdate(@Nonnull final JsonObject data) {
        return delegate(ChannelPinsUpdate.class, ChannelPinsUpdateImpl.builder()
                .catnip(catnip)
                .channelIdAsLong(Long.parseUnsignedLong(data.getString("channel_id")))
                .lastPinTimestamp(data.getString("last_pin_timestamp"))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public PermissionOverride createPermissionOverride(@Nonnull final JsonObject data) {
        return delegate(PermissionOverride.class, PermissionOverrideImpl.builder()
                .catnip(catnip)
                .idAsLong(Long.parseUnsignedLong(data.getString("id")))
                .type(OverrideType.byKey(data.getInt("type")))
                .allowRaw(Long.parseUnsignedLong(data.getString("allow", "0")))
                .denyRaw(Long.parseUnsignedLong(data.getString("deny", "0")))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public Role createRole(@Nonnull final String guildId, @Nonnull final JsonObject data) {
        return delegate(Role.class, RoleImpl.builder()
                .catnip(catnip)
                .idAsLong(Long.parseUnsignedLong(data.getString("id")))
                .guildIdAsLong(Long.parseUnsignedLong(guildId))
                .name(data.getString("name"))
                .color(data.getInt("color"))
                .hoist(data.getBoolean("hoist"))
                .position(data.getInt("position"))
                .permissionsRaw(Long.parseUnsignedLong(data.getString("permissions", "0")))
                .managed(data.getBoolean("managed"))
                .mentionable(data.getBoolean("mentionable"))
                .tags(createRoleTags(data))
                .icon(data.getString("icon"))
                .unicodeEmoji(data.getString("unicode_emoji"))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public RoleTags createRoleTags(@Nonnull final JsonObject data) {
        return delegate(RoleTags.class, RoleTagsImpl.builder()
                .botId(data.getString("bot_id"))
                .premiumSubscriber(data.has("premium_subscriber"))
                .integrationId(data.getString("integration_id"))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public PartialRole createPartialRole(@Nonnull final String guildId, @Nonnull final String roleId) {
        return delegate(PartialRole.class, PartialRoleImpl.builder()
                .catnip(catnip)
                .idAsLong(Long.parseUnsignedLong(roleId))
                .guildIdAsLong(Long.parseUnsignedLong(guildId))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public User createUser(@Nonnull final JsonObject data) {
        return delegate(User.class, UserImpl.builder()
                .catnip(catnip)
                .username(data.getString("username"))
                .idAsLong(Long.parseUnsignedLong(data.getString("id")))
                .discriminator(data.getString("discriminator"))
                .avatar(data.getString("avatar", null))
                .bot(data.getBoolean("bot", false))
                .publicFlags(UserFlag.toSet(data.getInt("public_flags", 0)))
                .accentColor(data.getInt("accent_color", 0))
                .banner(data.getString("banner", null))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public Presence createPresence(@Nonnull final JsonObject data) {
        final JsonObject clientStatus = data.getObject("client_status");
        final String mobileStatusString = clientStatus == null ? null : clientStatus.getString("mobile");
        final String webStatusString = clientStatus == null ? null : clientStatus.getString("web");
        final String desktopStatusString = clientStatus == null ? null : clientStatus.getString("desktop");
        
        return delegate(Presence.class, PresenceImpl.builder()
                .catnip(catnip)
                .status(OnlineStatus.fromString(data.getString("status")))
                .activities(toList(data.getArray("activities", new JsonArray()), this::createActivity))
                .mobileStatus(mobileStatusString != null ? OnlineStatus.fromString(mobileStatusString) : null)
                .webStatus(webStatusString != null ? OnlineStatus.fromString(webStatusString) : null)
                .desktopStatus(desktopStatusString != null ? OnlineStatus.fromString(desktopStatusString) : null)
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public PresenceUpdate createPresenceUpdate(@Nonnull final JsonObject data) {
        final JsonObject clientStatus = data.getObject("client_status");
        final String mobileStatusString = clientStatus == null ? null : clientStatus.getString("mobile");
        final String webStatusString = clientStatus == null ? null : clientStatus.getString("web");
        final String desktopStatusString = clientStatus == null ? null : clientStatus.getString("desktop");
        
        return delegate(PresenceUpdate.class, PresenceUpdateImpl.builder()
                .catnip(catnip)
                .status(OnlineStatus.fromString(data.getString("status")))
                .activities(toList(data.getArray("activities", new JsonArray()), this::createActivity))
                .idAsLong(Long.parseUnsignedLong(data.getObject("user").getString("id")))
                .guildIdAsLong(Long.parseUnsignedLong(data.getString("guild_id")))
                .mobileStatus(mobileStatusString != null ? OnlineStatus.fromString(mobileStatusString) : null)
                .webStatus(webStatusString != null ? OnlineStatus.fromString(webStatusString) : null)
                .desktopStatus(desktopStatusString != null ? OnlineStatus.fromString(desktopStatusString) : null)
                .build());
    }
    
    @Nullable
    @CheckReturnValue
    public Activity createActivity(@Nullable final JsonObject data) {
        if(data == null) {
            return null;
        } else {
            final String applicationId = data.getString("application_id");
            return delegate(Activity.class, ActivityImpl.builder()
                    .name(data.getString("name"))
                    .type(ActivityType.byId(data.getInt("type")))
                    .url(data.getString("url"))
                    .timestamps(createTimestamps(data.getObject("timestamps", null)))
                    .applicationIdAsLong(applicationId == null ? 0 : Long.parseUnsignedLong(applicationId))
                    .details(data.getString("details"))
                    .state(data.getString("state"))
                    .party(createParty(data.getObject("party", null)))
                    .assets(createAssets(data.getObject("assets", null)))
                    .secrets(createSecrets(data.getObject("secrets", null)))
                    .instance(data.getBoolean("instance", false))
                    .flags(ActivityFlag.fromInt(data.getInt("flags", 0)))
                    .emoji(createActivityEmoji(data.getObject("emoji", null)))
                    .build());
        }
    }
    
    @Nullable
    @CheckReturnValue
    public ActivityEmoji createActivityEmoji(@Nullable final JsonObject data) {
        if(data == null) {
            return null;
        } else {
            final String id = data.getString("id");
            return delegate(ActivityEmoji.class, ActivityEmojiImpl.builder()
                    .idAsLong(id != null ? Long.parseUnsignedLong(id) : 0)
                    .animated(data.getBoolean("animated", false))
                    .name(data.getString("name"))
                    .build());
        }
    }
    
    @Nullable
    @CheckReturnValue
    public ActivityTimestamps createTimestamps(@Nullable final JsonObject data) {
        if(data == null) {
            return null;
        } else {
            // Defend against stringly-typed timestamps.
            // I asked Jake, he says that integers >53 bits are automatically
            // serialized to strings. Since this field is user-provided, it
            // means that an irresponsible end-user could send large-enough
            // integers to hit this cap and thereby end up causing :fire: for
            // us.
            // Therefore, this defends against that exact issue.
            long start;
            try {
                start = data.getNumber("start", -1L).longValue();
            } catch(final ClassCastException ignored) {
                start = Long.parseLong(data.getString("start", "-1"));
            }
            long end;
            try {
                end = data.getNumber("end", -1L).longValue();
            } catch(final ClassCastException ignored) {
                end = Long.parseLong(data.getString("end", "-1"));
            }
            
            return delegate(ActivityTimestamps.class, ActivityTimestampsImpl.builder()
                    .start(start)
                    .end(end)
                    .build());
        }
    }
    
    @Nullable
    @CheckReturnValue
    public ActivityParty createParty(@Nullable final JsonObject data) {
        if(data == null) {
            return null;
        } else {
            final JsonArray size = data.getArray("size", new JsonArray(Arrays.asList(-1, -1)));
            return delegate(ActivityParty.class, ActivityPartyImpl.builder()
                    .id(data.getString("id"))
                    // Initialized to -1 if doesn't exist
                    .currentSize(size.getInt(0))
                    .maxSize(size.getInt(1))
                    .build());
        }
    }
    
    @Nullable
    @CheckReturnValue
    public ActivityAssets createAssets(@Nullable final JsonObject data) {
        if(data == null) {
            return null;
        } else {
            return delegate(ActivityAssets.class, ActivityAssetsImpl.builder()
                    .largeImage(data.getString("large_image"))
                    .largeText(data.getString("large_text"))
                    .smallImage(data.getString("small_image"))
                    .smallText(data.getString("small_text"))
                    .build());
        }
    }
    
    @Nullable
    @CheckReturnValue
    public ActivitySecrets createSecrets(@Nullable final JsonObject data) {
        if(data == null) {
            return null;
        } else {
            return delegate(ActivitySecrets.class, ActivitySecretsImpl.builder()
                    .join(data.getString("join"))
                    .spectate(data.getString("spectate"))
                    .match(data.getString("match"))
                    .build());
        }
    }
    
    @Nonnull
    @CheckReturnValue
    public TypingUser createTypingUser(@Nonnull final JsonObject data) {
        final String guildId = data.getString("guild_id");
        return delegate(TypingUser.class, TypingUserImpl.builder()
                .catnip(catnip)
                .idAsLong(Long.parseUnsignedLong(data.getString("user_id")))
                .channelIdAsLong(Long.parseUnsignedLong(data.getString("channel_id")))
                .guildIdAsLong(guildId == null ? 0 : Long.parseUnsignedLong(guildId))
                .timestamp(data.getNumber("timestamp").longValue())
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public Member createMember(@Nonnull final String guildId, @Nonnull final String id, @Nonnull final JsonObject data) {
        final JsonObject userData = data.getObject("user");
        final long guild = Long.parseUnsignedLong(guildId);
        if(userData != null) {
            final int shards = catnip.shardManager().shardCount();
            final int shardMod = shards == 0 ? 1 : shards;
            catnip.cacheWorker().bulkCacheUsers(
                    (int) ((guild >> 22) % shardMod),
                    Collections.singletonList(createUser(userData)));
        }
        final String joinedAt;
        if(data.getString("joined_at", null) != null) {
            joinedAt = data.getString("joined_at");
        } else {
            joinedAt = null;
        }
        
        return delegate(Member.class, MemberImpl.builder()
                .catnip(catnip)
                .idAsLong(Long.parseUnsignedLong(id))
                .guildIdAsLong(guild)
                .nick(data.getString("nick"))
                .roleIds(toStringSet(data.getArray("roles")))
                .joinedAt(joinedAt)
                .premiumSince(data.getString("premium_since", null))
                .avatarHash(data.getString("avatar"))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public Member createMember(@Nonnull final String guildId, @Nonnull final User user,
                               @Nonnull final JsonObject data) {
        return createMember(guildId, user.id(), data);
    }
    
    @Nonnull
    @CheckReturnValue
    public Member createMember(@Nonnull final String guildId, @Nonnull final JsonObject data) {
        return createMember(guildId, createUser(data.getObject("user")), data);
    }
    
    @Nonnull
    @CheckReturnValue
    public PartialMember createPartialMember(@Nonnull final String guild, @Nonnull final JsonObject data) {
        return delegate(PartialMember.class, PartialMemberImpl.builder()
                .catnip(catnip)
                .guildIdAsLong(Long.parseUnsignedLong(guild))
                .user(createUser(data.getObject("user")))
                .roleIds(toStringSet(data.getArray("roles")))
                .nick(data.getString("nick"))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public VoiceState createVoiceState(@Nullable final String guildId, @Nonnull final JsonObject data) {
        final String channelId = data.getString("channel_id");
        return delegate(VoiceState.class, VoiceStateImpl.builder()
                .catnip(catnip)
                .guildIdAsLong(guildId == null ? 0 : Long.parseUnsignedLong(guildId))
                .channelIdAsLong(channelId == null ? 0 : Long.parseUnsignedLong(channelId))
                .userIdAsLong(Long.parseUnsignedLong(data.getString("user_id")))
                .sessionId(data.getString("session_id"))
                .deaf(data.getBoolean("deaf"))
                .mute(data.getBoolean("mute"))
                .selfDeaf(data.getBoolean("self_deaf"))
                .selfMute(data.getBoolean("self_mute"))
                .suppress(data.getBoolean("suppress"))
                .selfStream(data.getBoolean("self_stream", false))
                .requestToSpeakTimestampRaw(data.getString("request_to_speak_timestamp", null))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public VoiceState createVoiceState(@Nonnull final JsonObject data) {
        return createVoiceState(data.getString("guild_id"), data);
    }
    
    @Nonnull
    @CheckReturnValue
    public VoiceServerUpdate createVoiceServerUpdate(@Nonnull final JsonObject data) {
        return delegate(VoiceServerUpdate.class, VoiceServerUpdateImpl.builder()
                .catnip(catnip)
                .token(data.getString("token"))
                .guildIdAsLong(Long.parseUnsignedLong(data.getString("guild_id")))
                .endpoint(data.getString("endpoint"))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public UnicodeEmoji createUnicodeEmoji(@Nonnull final JsonObject data) {
        return delegate(UnicodeEmoji.class, UnicodeEmojiImpl.builder()
                .catnip(catnip)
                .name(data.getString("name"))
                .requiresColons(data.getBoolean("require_colons", true))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public CustomEmoji createCustomEmoji(@Nullable final String guildId, @Nonnull final JsonObject data) {
        final JsonObject userRaw = data.getObject("user");
        
        return delegate(CustomEmoji.class, CustomEmojiImpl.builder()
                .catnip(catnip)
                .idAsLong(Long.parseUnsignedLong(data.getString("id")))
                .guildIdAsLong(guildId == null ? 0 : Long.parseUnsignedLong(guildId))
                .name(data.getString("name"))
                .roles(toStringList(data.getArray("roles")))
                .user(userRaw == null ? null : createUser(userRaw))
                .requiresColons(data.getBoolean("require_colons", true))
                .managed(data.getBoolean("managed", false))
                .animated(data.getBoolean("animated", false))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public Emoji createEmoji(@Nullable final String guildId, @Nonnull final JsonObject data) {
        return data.get("id") == null ? createUnicodeEmoji(data) : createCustomEmoji(guildId, data);
    }
    
    @Nonnull
    @CheckReturnValue
    public EmojiUpdate createGuildEmojisUpdate(@Nonnull final JsonObject data) {
        final String guildId = data.getString("guild_id");
        return delegate(EmojiUpdate.class, EmojiUpdateImpl.builder()
                .catnip(catnip)
                .guildIdAsLong(Long.parseUnsignedLong(guildId))
                .emojis(toList(data.getArray("emojis"),
                        e -> createCustomEmoji(guildId, e)))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public Attachment createAttachment(@Nonnull final JsonObject data) {
        return delegate(Attachment.class, AttachmentImpl.builder()
                .catnip(catnip)
                .idAsLong(Long.parseUnsignedLong(data.getString("id")))
                .fileName(data.getString("filename"))
                .size(data.getInt("size"))
                .url(data.getString("url"))
                .proxyUrl(data.getString("proxy_url"))
                .height(data.getInt("height", -1))
                .width(data.getInt("width", -1))
                .ephemeral(data.getBoolean("ephemeral", false))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public Reaction createReaction(@Nonnull final String guildId, @Nonnull final JsonObject data) {
        return delegate(Reaction.class, ReactionImpl.builder()
                .count(data.getInt("count"))
                .self(data.getBoolean("self", false))
                .emoji(createEmoji(guildId, data.getObject("emoji")))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public ReactionUpdate createReactionUpdate(@Nonnull final JsonObject data) {
        return delegate(ReactionUpdate.class, ReactionUpdateImpl.builder()
                .catnip(catnip)
                .userId(data.getString("user_id"))
                .channelId(data.getString("channel_id"))
                .messageId(data.getString("message_id"))
                .guildId(data.getString("guild_id"))
                .emoji(createEmoji(null, data.getObject("emoji")))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public BulkRemovedReactions createBulkRemovedReactions(@Nonnull final JsonObject data) {
        return delegate(BulkRemovedReactions.class, BulkRemovedReactionsImpl.builder()
                .catnip(catnip)
                .channelId(data.getString("channel_id"))
                .messageId(data.getString("message_id"))
                .guildId(data.getString("guild_id"))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public Sticker createSticker(@Nonnull final JsonObject data) {
        return delegate(Sticker.class, StickerImpl.builder()
                .catnip(catnip)
                .idAsLong(Long.parseUnsignedLong(data.getString("id")))
                .packIdAsLong(Long.parseUnsignedLong(data.getString("pack_id")))
                .name(data.getString("name"))
                .description(data.getString("description"))
                .tags(data.has("tags") ? List.of(data.getString("tags").split(",")) : List.of())
                .asset(data.getString("asset"))
                .previewAsset(data.getString("preview_asset"))
                .formatType(StickerFormatType.byId(data.getInt("format_type")))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public Message createMessage(@Nonnull final JsonObject data) {
        final User author = createUser(data.getObject("author"));
        
        final JsonObject memberRaw = data.getObject("member");
        // If member exists, guild_id must also exist
        final Member member = memberRaw == null ? null : createMember(data.getString("guild_id"), author, memberRaw);
        
        final String guildId = data.getString("guild_id");
        final String webhookId = data.getString("webhook_id");
        final JsonObject rawReferencedMessage = data.getObject("referenced_message", null);
        
        final List<Member> mentionedMembers = new ArrayList<>();
        if(guildId != null) {
            mentionedMembers.addAll(toList(data.getArray("mentions"), o -> createPartialMemberMention(guildId, o)));
        }
        
        final var activity = createMessageActivity(data.getObject("activity"));
        final var application = createMessageApplication(data.getObject("application"));
        final var reference = createMessageReference(data.getObject("message_reference"));
        final var referencedMessage = rawReferencedMessage == null ? null : createMessage(rawReferencedMessage);
        final var channelMentions = toList(data.getArray("mention_channels", new JsonArray()), this::createChannelMention);
        
        return delegate(Message.class, MessageImpl.builder()
                .catnip(catnip)
                .idAsLong(Long.parseUnsignedLong(data.getString("id")))
                .channelIdAsLong(Long.parseUnsignedLong(data.getString("channel_id")))
                .author(author)
                .content(data.getString("content"))
                .timestamp(data.getString("timestamp"))
                .editedTimestamp(data.getString("edited_timestamp"))
                .tts(data.getBoolean("tts", false))
                .mentionsEveryone(data.getBoolean("mention_everyone", false))
                .mentionedUsers(toList(data.getArray("mentions"), this::createUser))
                .mentionedMembers(mentionedMembers)
                .mentionedRoleIds(toStringList(data.getArray("mention_roles")))
                .mentionedChannels(toList(data.getArray("mention_channels"), this::createChannelMention))
                .attachments(toList(data.getArray("attachments"), this::createAttachment))
                .embeds(toList(data.getArray("embeds"), this::createEmbed))
                .reactions(toList(data.getArray("reactions"), e -> createReaction(data.getString("guild_id"), e)))
                .nonce(String.valueOf(data.get("nonce")))
                .pinned(data.getBoolean("pinned", false))
                .type(MessageType.byId(data.getInt("type", MessageType.DEFAULT.id())))
                .member(member)
                .guildIdAsLong(guildId == null ? 0 : Long.parseUnsignedLong(guildId))
                .webhookIdAsLong(webhookId == null ? 0 : Long.parseUnsignedLong(webhookId))
                .activity(activity)
                .application(application)
                .flagsRaw(data.getInt("flags", 0))
                .messageReference(reference)
                .mentionedChannels(channelMentions)
                .referencedMessage(referencedMessage)
                .stickers(toList(data.getArray("stickers", new JsonArray()), this::createSticker))
                .build());
    }
    
    @Nullable
    @CheckReturnValue
    public ChannelMention createChannelMention(@Nullable final JsonObject data) {
        if(data == null) {
            return null;
        } else {
            return delegate(ChannelMention.class, ChannelMentionImpl.builder()
                    .catnip(catnip)
                    .idAsLong(Long.parseUnsignedLong(data.getString("id")))
                    .guildIdAsLong(Long.parseUnsignedLong(data.getString("guild_id")))
                    .name(data.getString("name"))
                    .type(ChannelType.byKey(data.getInt("type")))
                    .build());
        }
    }
    
    @Nullable
    @CheckReturnValue
    public MessageActivity createMessageActivity(@Nullable final JsonObject data) {
        if(data == null) {
            return null;
        } else {
            return delegate(MessageActivity.class, MessageActivityImpl.builder()
                    .type(MessageActivityType.byId(data.getInt("type")))
                    .partyId(data.getString("party_id"))
                    .build());
        }
    }
    
    @Nullable
    @CheckReturnValue
    public MessageApplication createMessageApplication(@Nullable final JsonObject data) {
        if(data == null) {
            return null;
        } else {
            return delegate(MessageApplication.class, MessageApplicationImpl.builder()
                    .id(data.getString("id"))
                    .coverImage(data.getString("cover_image", null))
                    .description(data.getString("description"))
                    .icon(data.getString("icon", null))
                    .name(data.getString("name"))
                    .build());
        }
    }
    
    @Nullable
    @CheckReturnValue
    private MessageReference createMessageReference(@Nullable final JsonObject data) {
        if(data == null) {
            return null;
        } else {
            return delegate(MessageReference.class, MessageReferenceImpl.builder()
                    .catnip(catnip)
                    .guildId(data.getString("guild_id"))
                    .channelId(data.getString("channel_id"))
                    .messageId(data.getString("message_id"))
                    .build());
        }
    }
    
    @Nullable
    @CheckReturnValue
    private Member createPartialMemberMention(final String guildId, final JsonObject data) {
        if(data.containsKey("member")) {
            return createMember(guildId, data.getString("id"), data.getObject("member"));
        } else {
            return null;
        }
    }
    
    @Nonnull
    @CheckReturnValue
    public MessageEmbedUpdate createMessageEmbedUpdate(final JsonObject data) {
        final String guildId = data.getString("guild_id");
        return delegate(MessageEmbedUpdate.class, MessageEmbedUpdateImpl.builder()
                .catnip(catnip)
                .idAsLong(Long.parseUnsignedLong(data.getString("id")))
                .guildIdAsLong(guildId == null ? 0 : Long.parseUnsignedLong(guildId))
                .channelIdAsLong(Long.parseUnsignedLong(data.getString("channel_id")))
                .embeds(toList(data.getArray("embeds"), this::createEmbed))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public GuildEmbed createGuildEmbed(@Nonnull final JsonObject data) {
        final String channelId = data.getString("channel_id");
        return delegate(GuildEmbed.class, GuildEmbedImpl.builder()
                .catnip(catnip)
                .channelIdAsLong(channelId == null ? 0 : Long.parseUnsignedLong(channelId))
                .enabled(data.getBoolean("enabled"))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public Guild createAndCacheGuild(@Nonnegative final int shardId, @Nonnull final JsonObject data) {
        // As we don't store these fields on the guild object itself, we have
        // to update them in the cache
        final String id = data.getString("id"); //optimization
        if(data.getArray("roles") != null) {
            catnip.cacheWorker().bulkCacheRoles(shardId, toList(data.getArray("roles"),
                    e -> createRole(id, e)));
        }
        if(data.getArray("channels") != null) {
            catnip.cacheWorker().bulkCacheChannels(shardId, toList(data.getArray("channels"),
                    e -> createGuildChannel(id, e)));
        }
        if(data.getArray("members") != null) {
            catnip.cacheWorker().bulkCacheMembers(shardId, toList(data.getArray("members"),
                    e -> createMember(id, e)));
        }
        if(data.getArray("emojis") != null) {
            catnip.cacheWorker().bulkCacheEmoji(shardId, toList(data.getArray("emojis"),
                    e -> createCustomEmoji(id, e)));
        }
        if(data.getArray("presences") != null) {
            catnip.cacheWorker().bulkCachePresences(shardId, toMap(data.getArray("presences"),
                    o -> o.getObject("user").getString("id"), this::createPresence));
        }
        if(data.getArray("voice_states") != null) {
            catnip.cacheWorker().bulkCacheVoiceStates(shardId, toList(data.getArray("voice_states"), e -> createVoiceState(id, e)));
        }
        if(data.getArray("threads") != null) {
            catnip.cacheWorker().bulkCacheChannels(shardId, toList(data.getArray("threads"), e -> createThreadChannel(id, e)));
        }
        return createGuild(data);
    }
    
    @Nonnull
    @CheckReturnValue
    public Guild createGuild(@Nonnull final JsonObject data) {
        final String afkChannelId = data.getString("afk_channel_id");
        final String applicationId = data.getString("application_id");
        final String widgetChannelId = data.getString("widget_channel_id");
        final String systemChannelId = data.getString("system_channel_id");
        final int maxPresences = data.getInt("max_presences", 5000);
        final int premiumSubscriptionCount = data.getInt("premium_subscription_count", 0);
        return delegate(Guild.class, GuildImpl.builder()
                .catnip(catnip)
                .idAsLong(Long.parseUnsignedLong(data.getString("id")))
                .name(data.getString("name"))
                .icon(data.getString("icon"))
                .splash(data.getString("splash"))
                .owned(data.getBoolean("owner", false))
                .ownerIdAsLong(Long.parseUnsignedLong(data.getString("owner_id")))
                .permissions(Permission.toSet(Long.parseUnsignedLong(data.getString("permissions", "0"))))
                .region(data.getString("region"))
                .afkChannelIdAsLong(afkChannelId == null ? 0 : Long.parseUnsignedLong(afkChannelId))
                .afkTimeout(data.getInt("afk_timeout", 0))
                .verificationLevel(VerificationLevel.byKey(data.getInt("verification_level", 0)))
                .defaultMessageNotifications(NotificationLevel.byKey(data.getInt("default_message_notifications", 0)))
                .explicitContentFilter(ContentFilterLevel.byKey(data.getInt("explicit_content_filter", 0)))
                .features(stringListToTypedList(data.getArray("features"),
                        feature -> GuildFeature.unknownValueOf(catnip, feature)))
                .mfaLevel(MFALevel.byKey(data.getInt("mfa_level", 0)))
                .applicationIdAsLong(applicationId == null ? 0 : Long.parseUnsignedLong(applicationId))
                .widgetEnabled(data.getBoolean("widget_enabled", false))
                .widgetChannelIdAsLong(widgetChannelId == null ? 0 : Long.parseUnsignedLong(widgetChannelId))
                .systemChannelIdAsLong(systemChannelId == null ? 0 : Long.parseUnsignedLong(systemChannelId))
                .joinedAt(data.getString("joined_at"))
                .large(data.getBoolean("large", false))
                .unavailable(data.getBoolean("unavailable", false))
                .maxPresences(maxPresences)
                .maxMembers(data.getInt("max_members", 0))
                .approximateMemberCount(data.getInt("approximate_member_count", 0))
                .approximatePresenceCount(data.getInt("approximate_presence_count", 0))
                .vanityUrlCode(data.getString("vanity_url_code"))
                .description(data.getString("description"))
                .banner(data.getString("banner"))
                .premiumTier(PremiumTier.byKey(data.getInt("premium_tier", 0)))
                .premiumSubscriptionCount(premiumSubscriptionCount)
                .preferredLocale(data.getString("preferred_locale"))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public UnavailableGuild createUnavailableGuild(@Nonnull final JsonObject data) {
        return delegate(UnavailableGuild.class, UnavailableGuildImpl.builder()
                .catnip(catnip)
                .idAsLong(Long.parseUnsignedLong(data.getString("id")))
                .unavailable(data.getBoolean("unavailable"))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public PartialGuild createPartialGuild(@Nonnull final JsonObject data) {
        return delegate(PartialGuild.class, PartialGuildImpl.builder()
                .catnip(catnip)
                .idAsLong(Long.parseUnsignedLong(data.getString("id")))
                .name(data.getString("name"))
                .icon(data.getString("icon"))
                .owned(data.getBoolean("owner", false))
                .permissions(Permission.toSet(Long.parseUnsignedLong(data.getString("permissions", "0"))))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public GatewayGuildBan createGatewayGuildBan(@Nonnull final JsonObject data) {
        return delegate(GatewayGuildBan.class, GatewayGuildBanImpl.builder()
                .catnip(catnip)
                .guildIdAsLong(Long.parseUnsignedLong(data.getString("guild_id")))
                .user(createUser(data.getObject("user")))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public GuildBan createGuildBan(@Nonnull final JsonObject data) {
        return delegate(GuildBan.class, GuildBanImpl.builder()
                .catnip(catnip)
                .reason(data.getString("reason"))
                .user(createUser(data.getObject("user")))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public Invite createInvite(@Nonnull final JsonObject data) {
        if(data.containsKey("uses")) {
            return createCreatedInvite(data);
        }
        return delegate(Invite.class, InviteImpl.builder()
                .catnip(catnip)
                .code(data.getString("code"))
                .inviter(createInviter(data.getObject("inviter")))
                .guild(createInviteGuild(data.getObject("guild")))
                .channel(createInviteChannel(data.getObject("channel")))
                .approximatePresenceCount(data.getInt("approximate_presence_count", -1))
                .approximateMemberCount(data.getInt("approximate_member_count", -1))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public DeletedInvite createDeletedInvite(@Nonnull final JsonObject data) {
        long guildId = 0;
        if(data.has("guild_id")) {
            guildId = Long.parseUnsignedLong(data.getString("guild_id"));
        }
        return delegate(DeletedInvite.class, DeletedInviteImpl.builder()
                .catnip(catnip)
                .code(data.getString("code"))
                .channelIdAsLong(Long.parseUnsignedLong(data.getString("channel_id")))
                .guildIdAsLong(guildId)
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public CreatedInvite createCreatedInvite(@Nonnull final JsonObject data) {
        return delegate(CreatedInvite.class, CreatedInviteImpl.builder()
                .catnip(catnip)
                .code(data.getString("code"))
                .inviter(createInviter(data.getObject("inviter")))
                .guild(createInviteGuild(data.getObject("guild")))
                .channel(createInviteChannel(data.getObject("channel")))
                .approximatePresenceCount(data.getInt("approximate_presence_count", -1))
                .approximateMemberCount(data.getInt("approximate_member_count", -1))
                .uses(data.getInt("uses"))
                .maxUses(data.getInt("max_uses"))
                .maxAge(data.getInt("max_age"))
                .temporary(data.getBoolean("temporary", false))
                .createdAtString(data.getString("created_at"))
                .revoked(data.getBoolean("revoked", false))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public InviteChannel createInviteChannel(@Nonnull final JsonObject data) {
        return delegate(InviteChannel.class, InviteChannelImpl.builder()
                .catnip(catnip)
                .idAsLong(Long.parseUnsignedLong(data.getString("id")))
                .name(data.getString("name"))
                .type(ChannelType.byKey(data.getInt("type")))
                .build());
    }
    
    @Nullable
    @CheckReturnValue
    public InviteGuild createInviteGuild(@Nullable final JsonObject data) {
        if(data == null) {
            return null;
        }
        return delegate(InviteGuild.class, InviteGuildImpl.builder()
                .catnip(catnip)
                .idAsLong(Long.parseUnsignedLong(data.getString("id")))
                .name(data.getString("name"))
                .icon(data.getString("icon"))
                .splash(data.getString("splash"))
                .features(stringListToTypedList(data.getArray("features"),
                        feature -> GuildFeature.unknownValueOf(catnip, feature)))
                .verificationLevel(VerificationLevel.byKey(data.getInt("verification_level", 0)))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public Inviter createInviter(@Nonnull final JsonObject data) {
        return delegate(Inviter.class, InviterImpl.builder()
                .catnip(catnip)
                .idAsLong(Long.parseUnsignedLong(data.getString("id")))
                .username(data.getString("username"))
                .discriminator(data.getString("discriminator"))
                .avatar(data.getString("avatar"))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public VoiceRegion createVoiceRegion(@Nonnull final JsonObject data) {
        return delegate(VoiceRegion.class, VoiceRegionImpl.builder()
                .catnip(catnip)
                .id(data.getString("id"))
                .name(data.getString("name"))
                .vip(data.getBoolean("vip", false))
                .optimal(data.getBoolean("optimal", false))
                .deprecated(data.getBoolean("deprecated", false))
                .custom(data.getBoolean("custom", false))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public Webhook createWebhook(@Nonnull final JsonObject data) {
        return delegate(Webhook.class, WebhookImpl.builder()
                .catnip(catnip)
                .idAsLong(Long.parseUnsignedLong(data.getString("id")))
                .guildIdAsLong(Long.parseUnsignedLong(data.getString("guild_id")))
                .channelIdAsLong(Long.parseUnsignedLong(data.getString("channel_id")))
                .user(!data.containsKey("user") ? null : createUser(data.getObject("user")))
                .name(data.getString("name"))
                .avatar(data.getString("avatar"))
                .token(data.getString("token"))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public WebhooksUpdate createWebhooksUpdate(@Nonnull final JsonObject data) {
        final String channelId = data.getString("channel_id");
        return delegate(WebhooksUpdate.class, WebhooksUpdateImpl.builder()
                .catnip(catnip)
                .guildIdAsLong(Long.parseUnsignedLong(data.getString("guild_id")))
                .channelIdAsLong(channelId == null ? 0 : Long.parseUnsignedLong(channelId))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public DeletedMessage createDeletedMessage(@Nonnull final JsonObject data) {
        final String guildId = data.getString("guild_id");
        return delegate(DeletedMessage.class, DeletedMessageImpl.builder()
                .catnip(catnip)
                .idAsLong(Long.parseUnsignedLong(data.getString("id")))
                .channelIdAsLong(Long.parseUnsignedLong(data.getString("channel_id")))
                .guildIdAsLong(guildId == null ? 0 : Long.parseUnsignedLong(guildId))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public BulkDeletedMessages createBulkDeletedMessages(@Nonnull final JsonObject data) {
        final String guildId = data.getString("guild_id");
        return delegate(BulkDeletedMessages.class, BulkDeletedMessagesImpl.builder()
                .catnip(catnip)
                .ids(toStringList(data.getArray("ids")))
                .channelIdAsLong(Long.parseUnsignedLong(data.getString("channel_id")))
                .guildIdAsLong(guildId == null ? 0 : Long.parseUnsignedLong(guildId))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public Ready createReady(@Nonnull final JsonObject data) {
        // We always send the shard key in IDENTIFY, so this should always be present
        final JsonArray shard = data.getArray("shard");
        return delegate(Ready.class, ReadyImpl.builder()
                .catnip(catnip)
                .version(data.getInt("v"))
                .user(createUser(data.getObject("user")))
                .guilds(toSet(data.getArray("guilds"), this::createUnavailableGuild))
                .shardId(shard.getInt(0))
                .shardCount(shard.getInt(1))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public Resumed createResumed(@SuppressWarnings({"unused", "RedundantSuppression"}) @Nonnull final JsonObject data) {
        return delegate(Resumed.class, ResumedImpl.builder()
                .catnip(catnip)
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public AuditLogChange createAuditLogChange(@Nonnull final JsonObject data) {
        return delegate(AuditLogChange.class, AuditLogChangeImpl.builder()
                .catnip(catnip)
                .key(data.getString("key"))
                .newValue(data.get("new_value")) // no npe if null/optional key
                .oldValue(data.get("old_value"))
                .build());
    }
    
    @Nullable
    @CheckReturnValue
    public OptionalEntryInfo createOptionalEntryInfo(@Nonnull final JsonObject data, @Nonnull final ActionType type) {
        // The data returned for MESSAGE_BULK_DELETE *doesn't* actually follow https://discord.com/developers/docs/resources/audit-log#audit-log-entry-object-optional-audit-entry-info
        // Instead, we're going to just return 'Count' as that is the only option that's present. If you're looking for 'channel_id', use 'target_id' (named targetId() and targetIdAsLong()) in AuditLogEntry.
        return switch(type) {
            case MEMBER_PRUNE -> delegate(MemberPruneInfo.class, MemberPruneInfoImpl.builder()
                    .catnip(catnip)
                    .deleteMemberDays(data.getInt("delete_member_days"))
                    .removedMembersCount(data.getInt("members_removed"))
                    .build());
            case MEMBER_MOVE -> delegate(MemberMoveInfo.class, MemberMoveInfoImpl.builder()
                    .catnip(catnip)
                    .channelIdAsLong(Long.parseUnsignedLong(data.getString("channel_id")))
                    .membersMovedCount(Integer.parseUnsignedInt(data.getString("count")))
                    .build());
            case MEMBER_DISCONNECT -> delegate(MemberDisconnectInfo.class, MemberDisconnectInfoImpl.builder()
                    .catnip(catnip)
                    .membersDisconnectedCount(Integer.parseUnsignedInt(data.getString("count")))
                    .build());
            case MESSAGE_DELETE -> delegate(MessageDeleteInfo.class, MessageDeleteInfoImpl.builder()
                    .catnip(catnip)
                    .channelIdAsLong(Long.parseUnsignedLong(data.getString("channel_id")))
                    .deletedMessagesCount(Integer.parseUnsignedInt(data.getString("count")))
                    .build());
            case MESSAGE_BULK_DELETE -> delegate(MessageBulkDeleteInfo.class, MessageBulkDeleteInfoImpl.builder()
                    .catnip(catnip)
                    .deletedMessagesCount(Integer.parseUnsignedInt(data.getString("count")))
                    .build());
            case MESSAGE_PIN, MESSAGE_UNPIN -> delegate(MessagePinInfo.class, MessagePinInfoImpl.builder()
                    .catnip(catnip)
                    .channelIdAsLong(Long.parseUnsignedLong(data.getString("channel_id")))
                    .messageIdAsLong(Long.parseUnsignedLong(data.getString("message_id")))
                    .build());
            case CHANNEL_OVERWRITE_CREATE, CHANNEL_OVERWRITE_UPDATE, CHANNEL_OVERWRITE_DELETE -> delegate(OverrideUpdateInfo.class, OverrideUpdateInfoImpl.builder()
                    .catnip(catnip)
                    .overriddenEntityIdAsLong(Long.parseUnsignedLong(data.getString("id")))
                    .overrideType(OverrideType.byKey(data.getInt("type")))
                    .roleName(data.getString("role_name"))
                    .build());
            default -> null;
        };
    }
    
    @Nonnull
    @CheckReturnValue
    public AuditLogEntry createAuditLogEntry(@Nonnull final JsonObject data, @Nonnull final Map<String, Webhook> webhooks,
                                             @Nonnull final Map<String, User> users) {
        final ActionType type = ActionType.byKey(data.getInt("action_type"));
        final String targetId = data.getString("target_id");
        return delegate(AuditLogEntry.class, AuditLogEntryImpl.builder()
                .catnip(catnip)
                .idAsLong(Long.parseUnsignedLong(data.getString("id")))
                .user(users.get(data.getString("user_id")))
                .targetIdAsLong(targetId == null ? 0 : Long.parseUnsignedLong(targetId))
                .webhook(webhooks.get(data.getString("target_id")))
                .type(type)
                .reason(data.getString("reason"))
                .changes(toList(data.getArray("changes"), this::createAuditLogChange))
                .options(data.containsKey("options") ? createOptionalEntryInfo(data.getObject("options"), type) : null)
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public List<AuditLogEntry> createAuditLog(@Nonnull final JsonObject data) {
        final Map<String, Webhook> webhooks = toMap(data.getArray("webhooks"), x -> x.getString("id"), this::createWebhook);
        final Map<String, User> users = toMap(data.getArray("users"), x -> x.getString("id"), this::createUser);
        
        return toList(data.getArray("audit_log_entries"), e ->
                createAuditLogEntry(e, webhooks, users)
        );
    }
    
    @Nonnull
    @CheckReturnValue
    public ApplicationInfo createApplicationInfo(@Nonnull final JsonObject data) {
        final JsonObject team = data.getObject("team");
        return delegate(ApplicationInfo.class, ApplicationInfoImpl.builder()
                .catnip(catnip)
                .idAsLong(Long.parseUnsignedLong(data.getString("id")))
                .name(data.getString("name"))
                .icon(data.getString("icon"))
                .description(data.getString("description"))
                .rpcOrigins(toStringList(data.getArray("rpc_origins")))
                .publicBot(data.getBoolean("bot_public"))
                .requiresCodeGrant(data.getBoolean("bot_require_code_grant"))
                .owner(createApplicationOwner(data.getObject("owner")))
                .team(team == null ? null : createTeam(team))
                .summary(data.getString("summary"))
                .verifyKey(data.getString("verify_key"))
                .guildIdAsLong(Long.parseUnsignedLong(data.getString("guild_id", "0")))
                .primarySkuId(data.getString("primary_sku_id"))
                .slug(data.getString("slug"))
                .coverImage(data.getString("cover_image"))
                .flags(ApplicationFlag.toSet(data.getLong("flags")))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public ApplicationOwner createApplicationOwner(@Nonnull final JsonObject data) {
        return delegate(ApplicationOwner.class, ApplicationOwnerImpl.builder()
                .catnip(catnip)
                .username(data.getString("username"))
                .idAsLong(Long.parseUnsignedLong(data.getString("id")))
                .discriminator(data.getString("discriminator"))
                .avatar(data.getString("avatar", null))
                .bot(data.getBoolean("bot", false))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public Team createTeam(@Nonnull final JsonObject data) {
        return delegate(Team.class, TeamImpl.builder()
                .catnip(catnip)
                .idAsLong(Long.parseUnsignedLong(data.getString("id")))
                .ownerIdAsLong(Long.parseUnsignedLong(data.getString("owner_user_id")))
                .name(data.getString("name"))
                .icon(data.getString("icon"))
                .members(toList(data.getArray("members"), this::createTeamMember))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public TeamMember createTeamMember(@Nonnull final JsonObject data) {
        return delegate(TeamMember.class, TeamMemberImpl.builder()
                .catnip(catnip)
                .teamIdAsLong(Long.parseUnsignedLong(data.getString("team_id")))
                .membershipState(data.getInt("membership_state"))
                .permissions(toStringList(data.getArray("permissions")))
                .user(createUser(data.getObject("user")))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public GatewayInfo createGatewayInfo(@Nonnull final JsonObject data) {
        final JsonObject sessionStartLimit = data.getObject("session_start_limit");
        if(data.containsKey("shards")) {
            // Valid data
            return delegate(GatewayInfo.class, GatewayInfoImpl.builder()
                    .catnip(catnip)
                    .valid(true)
                    .url(data.getString("url"))
                    .shards(data.getInt("shards"))
                    .totalSessions(sessionStartLimit.getInt("total"))
                    .remainingSessions(sessionStartLimit.getInt("remaining"))
                    .resetAfter(sessionStartLimit.getNumber("reset_after").longValue())
                    .build());
        } else {
            // Invalid data - probably borked token
            return delegate(GatewayInfo.class, GatewayInfoImpl.builder()
                    .catnip(catnip)
                    .valid(false)
                    .url("")
                    .shards(-1)
                    .totalSessions(-1)
                    .remainingSessions(-1)
                    .resetAfter(-1)
                    .build());
        }
    }
    
    @Nonnull
    @CheckReturnValue
    public ApplicationCommand createApplicationCommand(@Nonnull final JsonObject data) {
        return delegate(ApplicationCommand.class, ApplicationCommandImpl.builder()
                .catnip(catnip)
                .idAsLong(Long.parseUnsignedLong(data.getString("id")))
                .applicationIdAsLong(Long.parseUnsignedLong(data.getString("application_id")))
                .guildIdAsLong(Long.parseUnsignedLong(data.getString("guild_id", "0")))
                .description(data.getString("description"))
                .name(data.getString("name"))
                .options(toList(data.getArray("options"), this::createApplicationCommandOption))
                .versionAsLong(Long.parseUnsignedLong(data.getString("version")))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public ApplicationCommandOption createApplicationCommandOption(@Nonnull final JsonObject data) {
        return delegate(ApplicationCommandOption.class, ApplicationCommandOptionImpl.builder()
                .name(data.getString("name"))
                .type(ApplicationCommandOptionType.byKey(data.getInt("type")))
                .description(data.getString("description"))
                .defaultOption(data.getBoolean("default", false))
                .required(data.getBoolean("required", false))
                .options(toList(data.getArray("options"), this::createApplicationCommandOption))
                .choices(toList(data.getArray("choices"), this::createApplicationCommandOptionChoice))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public ApplicationCommandOptionChoice<?> createApplicationCommandOptionChoice(@Nonnull final JsonObject data) {
        final Object innerValue = data.get("value");
        if(innerValue instanceof String) {
            return delegate(ApplicationCommandOptionStringChoice.class, ApplicationCommandOptionStringChoiceImpl.builder()
                    .name(data.getString("name"))
                    .value(data.getString("value"))
                    .build());
        } else if(innerValue instanceof Integer) {
            return delegate(ApplicationCommandOptionIntegerChoice.class, ApplicationCommandOptionIntegerChoiceImpl.builder()
                    .catnip(catnip)
                    .name(data.getString("name"))
                    .value(data.getInt("value"))
                    .build());
        } else {
            throw new IllegalArgumentException("Unknown value type: " + innerValue.getClass().getName());
        }
    }
    
    @Nonnull
    @CheckReturnValue
    public Interaction<?> createInteraction(@Nonnull final JsonObject data) {
        final var type = InteractionType.byKey(data.getInt("type"));
        switch(type) {
            case APPLICATION_COMMAND -> {
                return delegate(Interaction.class, ApplicationCommandInteractionImpl.builder()
                        .catnip(catnip)
                        // TODO: Nullables
                        .channelIdAsLong(Long.parseUnsignedLong(data.getString("channel_id", "0")))
                        .guildIdAsLong(Long.parseUnsignedLong(data.getString("guild_id", "0")))
                        .idAsLong(Long.parseUnsignedLong(data.getString("id", "0")))
                        .token(data.getString("token"))
                        .type(type)
                        .version(data.getInt("version"))
                        .member(data.has("member") ? createInteractionMember(data.getString("guild_id"), data.getObject("member")) : null)
                        .data(data.has("data") ? createApplicationCommandInteractionData(data.getObject("data")) : null)
                        .build());
            }
            case MESSAGE_COMPONENT -> {
                CustomIdInteractionData customIdInteractionData = null;
                MessageComponentType componentType = null;
                if(data.has("data")) {
                    componentType = MessageComponentType.byKey(data.getObject("data").getInt("component_type"));
                    switch(componentType) {
                        case BUTTON -> customIdInteractionData = new CustomIdInteractionDataImpl(
                                MessageComponentType.byKey(data.getObject("data").getInt("component_type")),
                                data.getObject("data").getString("custom_id"));
                        case SELECT -> customIdInteractionData = new SelectInteractionDataImpl(
                                MessageComponentType.byKey(data.getObject("data").getInt("component_type")),
                                data.getObject("data").getString("custom_id"),
                                toStringList(data.getObject("data").getArray("values")));
                        default -> throw new IllegalArgumentException("Unknown ComponentType for reaction: " + data.getObject("data").getInt("component_type"));
                    }
                }
                if(componentType == null) {
                    throw new IllegalStateException("Component interaction may not have null component type");
                }
                
                switch(componentType) {
                    case BUTTON -> {
                        return delegate(Interaction.class, ButtonInteractionImpl.builder()
                                .catnip(catnip)
                                // TODO: Nullables
                                .channelIdAsLong(Long.parseUnsignedLong(data.getString("channel_id", "0")))
                                .guildIdAsLong(Long.parseUnsignedLong(data.getString("guild_id", "0")))
                                .idAsLong(Long.parseUnsignedLong(data.getString("id", "0")))
                                .token(data.getString("token"))
                                .type(type)
                                .version(data.getInt("version"))
                                .member(data.has("member") ? createInteractionMember(data.getString("guild_id"), data.getObject("member")) : null)
                                .data(customIdInteractionData)
                                .build());
                    }
                    case SELECT -> {
                        return delegate(Interaction.class, SelectInteractionImpl.builder()
                                .catnip(catnip)
                                // TODO: Nullables
                                .channelIdAsLong(Long.parseUnsignedLong(data.getString("channel_id", "0")))
                                .guildIdAsLong(Long.parseUnsignedLong(data.getString("guild_id", "0")))
                                .idAsLong(Long.parseUnsignedLong(data.getString("id", "0")))
                                .token(data.getString("token"))
                                .type(type)
                                .version(data.getInt("version"))
                                .member(data.has("member") ? createInteractionMember(data.getString("guild_id"), data.getObject("member")) : null)
                                .data((SelectInteractionData) customIdInteractionData)
                                .build());
                    }
                    default -> throw new IllegalArgumentException("Unknown ComponentType for reaction: " + data.getObject("data").getInt("component_type"));
                }
            }
            default -> throw new IllegalArgumentException("Unknown InteractionType for creation: " + type);
        }
    }
    
    @Nonnull
    @CheckReturnValue
    public ApplicationCommandInteractionData createApplicationCommandInteractionData(@Nonnull final JsonObject data) {
        return delegate(ApplicationCommandInteractionData.class, ApplicationCommandInteractionDataImpl.builder()
                .catnip(catnip)
                .idAsLong(Long.parseUnsignedLong(data.getString("id", "0")))
                .name(data.getString("name"))
                .options(toList(data.getArray("options"), this::createApplicationCommandInteractionDataOption))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public ApplicationCommandInteractionDataOption createApplicationCommandInteractionDataOption(@Nonnull final JsonObject data) {
        return delegate(ApplicationCommandInteractionDataOption.class, ApplicationCommandInteractionDataOptionImpl.builder()
                .catnip(catnip)
                .name(data.getString("name"))
                .options(toList(data.getArray("options"), this::createApplicationCommandInteractionDataOption))
                .type(ApplicationCommandOptionType.byKey(data.getInt("type")))
                .value(createApplicationCommandOptionChoice(data))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public InteractionMember createInteractionMember(@Nonnull final String guildId, @Nonnull final JsonObject data) {
        return delegate(InteractionMember.class, InteractionMemberImpl.builder()
                .delegate(createMember(guildId, data))
                .permissions(Permission.toSet(Long.parseUnsignedLong(data.getString("permissions", "0"))))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public Template createTemplate(@Nonnull final JsonObject data) {
        return delegate(Template.class, TemplateImpl.builder()
                .code(data.getString("code"))
                .name(data.getString("name"))
                .description(data.getString("description"))
                .usageCount(data.getInt("usage_count"))
                .creatorIdAsLong(Long.parseUnsignedLong(data.getString("creator_id")))
                .creator(createUser(data.getObject("creator")))
                .createdAtString(data.getString("created_at"))
                .updatedAtString(data.getString("updated_at"))
                .sourceGuildIdAsLong(Long.parseUnsignedLong(data.getString("source_guild_id")))
                .serializedSourceGuild(createGuild(data.getObject("serialized_source_guild")))
                .dirty(data.getBoolean("is_dirty"))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public ThreadChannel createThreadChannel(@Nonnull final String guildId, @Nonnull final JsonObject data) {
        final var parentId = data.getString("parent_id");
        return delegate(ThreadChannel.class, ThreadChannelImpl.builder()
                .catnip(catnip)
                .type(ChannelType.byKey(data.getInt("type")))
                .idAsLong(Long.parseUnsignedLong(data.getString("id")))
                .ownerIdAsLong(Long.parseUnsignedLong(data.getString("owner_id")))
                .name(data.getString("name"))
                .guildIdAsLong(Long.parseUnsignedLong(guildId))
                .position(data.getInt("position", -1))
                .parentIdAsLong(parentId == null ? 0 : Long.parseUnsignedLong(parentId))
                .overrides(toList(data.getArray("permission_overwrites"), this::createPermissionOverride))
                .topic(data.getString("topic"))
                .nsfw(data.getBoolean("nsfw", false))
                .rateLimitPerUser(data.getInt("rate_limit_per_user", 0))
                .messageCount(data.getInt("message_count"))
                .memberCount(data.getInt("member_count"))
                .member(data.has("member") ? createThreadMember(data.getObject("member")) : null)
                .metadata(createThreadMetadata(data.getObject("thread_metadata")))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public ThreadMember createThreadMember(@Nonnull final JsonObject data) {
        return delegate(ThreadMember.class, ThreadMemberImpl.builder()
                .catnip(catnip)
                .idAsLong(Long.parseUnsignedLong(data.getString("id")))
                .userIdAsLong(Long.parseUnsignedLong(data.getString("user_id")))
                .joinedAt(data.getString("joined_at"))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public ThreadMetadata createThreadMetadata(@Nonnull final JsonObject data) {
        return delegate(ThreadMetadata.class, ThreadMetadataImpl.builder()
                .archived(data.getBoolean("archived"))
                .archiveTimestamp(data.getString("archive_timestamp"))
                .autoArchiveDuration(data.getInt("auto_archive_duration"))
                .locked(data.getBoolean("locked"))
                .archiverIdAsLong(Long.parseUnsignedLong(data.getString("archiver_id", "0")))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public DeletedThread createDeletedThread(@Nonnull final JsonObject data) {
        return delegate(DeletedThread.class, DeletedThreadImpl.builder()
                .catnip(catnip)
                .idAsLong(Long.parseUnsignedLong(data.getString("id")))
                .guildIdAsLong(Long.parseUnsignedLong(data.getString("guild_id")))
                .parentIdAsLong(Long.parseUnsignedLong(data.getString("parent_id")))
                .type(ChannelType.byKey(data.getInt("id")))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public ThreadListSync createThreadListSync(@Nonnull final JsonObject data) {
        final var guildId = data.getString("guild_id");
        return delegate(ThreadListSync.class, ThreadListSyncImpl.builder()
                .guildIdAsLong(Long.parseUnsignedLong(guildId))
                .channelIds(toStringList(data.getArray("channel_ids", new JsonArray())))
                .threads(toList(data.getArray("threads"), e -> createThreadChannel(guildId, e)))
                .members(toList(data.getArray("members"), this::createThreadMember))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public ThreadMembersUpdate createThreadMembersUpdate(@Nonnull final JsonObject data) {
        return delegate(ThreadMembersUpdate.class, ThreadMembersUpdateImpl.builder()
                .idAsLong(Long.parseUnsignedLong(data.getString("id")))
                .guildIdAsLong(Long.parseUnsignedLong(data.getString("guild_id")))
                .memberCount(data.getInt("member_count"))
                .addedMembers(toList(data.getArray("added_members", new JsonArray()), this::createThreadMember))
                .removedMembers(toStringList(data.getArray("removed_members", new JsonArray())))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public ScheduledEvent createScheduledEvent(@Nonnull final JsonObject data) {
        return delegate(ScheduledEvent.class, ScheduledEventImpl.builder()
                .idAsLong(Long.parseUnsignedLong(data.getString("id")))
                .guildIdAsLong(Long.parseUnsignedLong(data.getString("guild_id")))
                .channelIdAsLong(Long.parseUnsignedLong(data.getString("channel_id", "0")))
                .creatorIdAsLong(Long.parseUnsignedLong(data.getString("creator_id", "0")))
                .name(data.getString("name"))
                .description(data.getString("description"))
                .scheduledStartTimeRaw(data.getString("scheduled_start_time"))
                .scheduledEndTimeRaw(data.getString("scheduled_end_time"))
                .privacyLevel(PrivacyLevel.byKey(data.getInt("privacy_level")))
                .status(EventStatus.byKey(data.getInt("status")))
                .entityType(EventEntityType.byKey(data.getInt("entity_type")))
                .entityId(data.getString("entity_id"))
                .entityMetadata(createScheduledEventEntityMetadata(data.getObject("entity_metadata")))
                .creator(data.has("creator") ? createUser(data.getObject("creator")) : null)
                .userCount(data.getInt("user_count", 0))
                .build());
    }
    
    @Nonnull
    @CheckReturnValue
    public EntityMetadata createScheduledEventEntityMetadata(@Nonnull final JsonObject data) {
        return delegate(EntityMetadata.class, EntityMetadataImpl.builder()
                .speakerIds(data.has("speaker_ids") ? toStringList(data.getArray("speaker_ids")) : List.of())
                .location(data.getString("location"))
                .build());
    }
}
