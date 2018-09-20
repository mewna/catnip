package com.mewna.catnip.entity.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mewna.catnip.Catnip;
import com.mewna.catnip.cache.EntityCacheWorker;
import com.mewna.catnip.entity.*;
import com.mewna.catnip.entity.Channel.ChannelType;
import com.mewna.catnip.entity.Embed.EmbedType;
import com.mewna.catnip.entity.Emoji.CustomEmoji;
import com.mewna.catnip.entity.Emoji.UnicodeEmoji;
import com.mewna.catnip.entity.Guild.ContentFilterLevel;
import com.mewna.catnip.entity.Guild.MFALevel;
import com.mewna.catnip.entity.Guild.NotificationLevel;
import com.mewna.catnip.entity.Guild.VerificationLevel;
import com.mewna.catnip.entity.Invite.InviteChannel;
import com.mewna.catnip.entity.Invite.InviteGuild;
import com.mewna.catnip.entity.Invite.Inviter;
import com.mewna.catnip.entity.PermissionOverride.OverrideType;
import com.mewna.catnip.entity.impl.EmbedImpl.*;
import com.mewna.catnip.entity.impl.InviteImpl.InviteChannelImpl;
import com.mewna.catnip.entity.impl.InviteImpl.InviteGuildImpl;
import com.mewna.catnip.entity.impl.InviteImpl.InviterImpl;
import com.mewna.catnip.entity.impl.MessageImpl.Attachment;
import com.mewna.catnip.entity.impl.MessageImpl.Reaction;
import com.mewna.catnip.entity.util.Permission;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
    private static <T> List<T> immutableListOf(@Nullable final JsonArray array, @Nonnull final Function<JsonObject, T> mapper) {
        if(array == null) {
            return Collections.emptyList();
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
    
    @Nonnull
    @CheckReturnValue
    private static List<String> stringListOf(@Nullable final JsonArray array) {
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
    private OffsetDateTime parseTimestamp(@Nullable final CharSequence raw) {
        return raw == null ? null : OffsetDateTime.parse(raw);
    }
    
    @Nonnull
    @CheckReturnValue
    private JsonObject embedFooterToJson(final Embed.Footer footer) {
        return new JsonObject().put("icon_url", footer.iconUrl()).put("text", footer.text());
    }
    
    @Nonnull
    @CheckReturnValue
    private JsonObject embedImageToJson(final Embed.Image image) {
        return new JsonObject().put("url", image.url());
    }
    
    @Nonnull
    @CheckReturnValue
    private JsonObject embedThumbnailToJson(final Embed.Thumbnail thumbnail) {
        return new JsonObject().put("url", thumbnail.url());
    }
    
    @Nonnull
    @CheckReturnValue
    private JsonObject embedAuthorToJson(final Embed.Author author) {
        return new JsonObject().put("name", author.name()).put("url", author.url()).put("icon_url", author.iconUrl());
    }
    
    @Nonnull
    @CheckReturnValue
    private JsonObject embedFieldToJson(final Embed.Field field) {
        return new JsonObject().put("name", field.name()).put("value", field.value()).put("inline", field.inline());
    }
    
    @Nonnull
    @CheckReturnValue
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
            o.put("fields", new JsonArray(embed.fields().stream().map(this::embedFieldToJson).collect(Collectors.toList())));
        }
        
        return o;
    }
    
    @Nonnull
    @CheckReturnValue
    private Field createField(@Nonnull final JsonObject data) {
        return Field.builder()
                .name(data.getString("name"))
                .value(data.getString("value"))
                .inline(data.getBoolean("inline", false))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public Embed createEmbed(final JsonObject data) {
        final JsonObject footerRaw = data.getJsonObject("footer");
        final Footer footer = isInvalid(footerRaw, "text") ? null : Footer.builder()
                .text(footerRaw.getString("text"))
                .iconUrl(footerRaw.getString("icon_url"))
                .proxyIconUrl(footerRaw.getString("proxy_icon_url"))
                .build();
        
        final JsonObject imageRaw = data.getJsonObject("image");
        final Image image = isInvalid(imageRaw, "url") ? null : Image.builder()
                .url(imageRaw.getString("url"))
                .proxyUrl(imageRaw.getString("proxy_url"))
                .height(imageRaw.getInteger("height", -1))
                .width(imageRaw.getInteger("width", -1))
                .build();
        
        final JsonObject thumbnailRaw = data.getJsonObject("thumbnail");
        final Thumbnail thumbnail = isInvalid(thumbnailRaw, "url") ? null : Thumbnail.builder()
                .url(thumbnailRaw.getString("url"))
                .proxyUrl(thumbnailRaw.getString("proxy_url"))
                .height(thumbnailRaw.getInteger("height", -1))
                .width(thumbnailRaw.getInteger("width", -1))
                .build();
        
        final JsonObject videoRaw = data.getJsonObject("video");
        final Video video = isInvalid(videoRaw, "url") ? null : Video.builder()
                .url(videoRaw.getString("url"))
                .height(videoRaw.getInteger("height", -1))
                .width(videoRaw.getInteger("width", -1))
                .build();
        
        final JsonObject providerRaw = data.getJsonObject("provider");
        final Provider provider = isInvalid(providerRaw, "url") ? null : Provider.builder()
                .name(providerRaw.getString("name"))
                .url(providerRaw.getString("url"))
                .build();
        
        final JsonObject authorRaw = data.getJsonObject("author");
        final Author author = isInvalid(authorRaw, "name") ? null : Author.builder()
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
                .timestamp(parseTimestamp(data.getString("timestamp")))
                .color(data.getInteger("color", null))
                .footer(footer)
                .image(image)
                .thumbnail(thumbnail)
                .video(video)
                .provider(provider)
                .author(author)
                .fields(immutableListOf(data.getJsonArray("fields"), this::createField))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public TextChannel createTextChannel(@Nonnull final JsonObject data) {
        return TextChannelImpl.builder()
                .catnip(catnip)
                .id(data.getString("id"))
                .type(ChannelType.TEXT)
                .name(data.getString("name"))
                .guildId(data.getString("guild_id"))
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
        return VoiceChannelImpl.builder()
                .catnip(catnip)
                .id(data.getString("id"))
                .type(ChannelType.VOICE)
                .name(data.getString("name"))
                .guildId(data.getString("guild_id"))
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
        return CategoryImpl.builder()
                .catnip(catnip)
                .id(data.getString("id"))
                .type(ChannelType.CATEGORY)
                .name(data.getString("name"))
                .guildId(data.getString("guild_id"))
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
                .recipient(createUser(data.getJsonArray("recipients").getJsonObject(0)))
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
        final ChannelType type = ChannelType.byKey(data.getInteger("type"));
        switch(type) {
            case TEXT:
                return createTextChannel(data);
            case VOICE:
                return createVoiceChannel(data);
            case CATEGORY:
                return createCategory(data);
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
    public Member createMember(@Nonnull final String guildId, @Nonnull final String id, @Nonnull final JsonObject data) {
        return MemberImpl.builder()
                .catnip(catnip)
                .id(id)
                .nick(data.getString("nick"))
                .roles(ImmutableSet.of()) // TODO: fetch roles from cache? or at least give the ids
                .joinedAt(parseTimestamp(data.getString("joined_at")))
                .deaf(data.getBoolean("deaf"))
                .mute(data.getBoolean("mute"))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public Member createMember(@Nonnull final String guildId, @Nonnull final User user, @Nonnull final JsonObject data) {
        return createMember(guildId, user.id(), data);
    }
    
    @Nonnull
    @CheckReturnValue
    public Member createMember(@Nonnull final String guildId, @Nonnull final JsonObject data) {
        return createMember(guildId, createUser(data.getJsonObject("user")), data);
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
    public CustomEmoji createCustomEmoji(@Nonnull final JsonObject data) {
        final JsonObject userRaw = data.getJsonObject("user");
        
        return CustomEmojiImpl.builder()
                .catnip(catnip)
                .id(data.getString("id"))
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
    public Emoji createEmoji(@Nonnull final JsonObject data) {
        return data.getValue("id") == null ? createUnicodeEmoji(data) : createCustomEmoji(data);
    }
    
    @Nonnull
    @CheckReturnValue
    public Message.Attachment createAttachment(@Nonnull final JsonObject data) {
        return Attachment.builder()
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
    public Message.Reaction createReaction(@Nonnull final JsonObject data) {
        return Reaction.builder()
                .count(data.getInteger("count"))
                .self(data.getBoolean("self", false))
                .emoji(createEmoji(data.getJsonObject("emoji")))
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
                .timestamp(parseTimestamp(data.getString("timestamp")))
                .editedTimestamp(parseTimestamp(data.getString("edited_timestamp")))
                .tts(data.getBoolean("tts"))
                .mentionsEveryone(data.getBoolean("mention_everyone", false))
                .mentionedUsers(immutableListOf(data.getJsonArray("mentions"), this::createUser))
                .mentionedRoles(stringListOf(data.getJsonArray("mention_roles")))
                .attachments(immutableListOf(data.getJsonArray("attachments"), this::createAttachment))
                .embeds(immutableListOf(data.getJsonArray("embeds"), this::createEmbed))
                .reactions(immutableListOf(data.getJsonArray("reactions"), this::createReaction))
                .nonce(data.getString("nonce"))
                .pinned(data.getBoolean("pinned"))
                .webhookId(data.getString("webhook_id"))
                .type(MessageType.byId(data.getInteger("type")))
                
                // Not actually documented (part of lazy guild changes)
                .member(member)
                .guildId(data.getString("guild_id"))
                .build();
    }
    
    /**
     * Unlike {@link #createGuild(JsonObject)}, this method caches parts of the
     * guild as it goes, such as channels and members.
     *
     * @param data Guild object to cache.
     *
     * @return The created guild.
     */
    @Nonnull
    @CheckReturnValue
    public Guild createCachedGuild(@Nonnull final JsonObject data) {
        ((EntityCacheWorker) catnip.cache()).bulkCacheRoles(immutableListOf(data.getJsonArray("roles"), e -> createRole(data.getString("id"), e)));
        // TODO: This should take a guild ID param
        ((EntityCacheWorker) catnip.cache()).bulkCacheChannels(immutableListOf(data.getJsonArray("roles"), this::createGuildChannel));
        ((EntityCacheWorker) catnip.cache()).bulkCacheMembers(immutableListOf(data.getJsonArray("roles"), e -> createMember(data.getString("id"), e)));
        return createGuild(data);
    }
    
    @Nonnull
    @CheckReturnValue
    public Guild createGuild(@Nonnull final JsonObject data) {
        return GuildImpl.builder()
                .catnip(catnip)
                .id(data.getString("id"))
                .name(data.getString("name"))
                .icon(data.getString("icon"))
                .splash(data.getString("splash"))
                .owned(data.getBoolean("owner", false))
                .ownerId(data.getString("owner_id"))
                .permissions(Permission.toSet(data.getLong("permissions", 0L)))
                .region(data.getString("region"))
                .afkChannelId(data.getString("afk_channel_id"))
                .afkTimeout(data.getInteger("afk_timeout"))
                .embedEnabled(data.getBoolean("embed_enabled", false))
                .embedChannelId(data.getString("embed_channel_id"))
                .verificationLevel(VerificationLevel.byKey(data.getInteger("verification_level", 0)))
                .defaultMessageNotifications(NotificationLevel.byKey(data.getInteger("default_message_notifications", 0)))
                .explicitContentFilter(ContentFilterLevel.byKey(data.getInteger("explicit_content_filter", 0)))
                //.roles(immutableListOf(data.getJsonArray("roles"), e -> createRole(data.getString("id"), e)))
                .emojis(immutableListOf(data.getJsonArray("emojis"), this::createCustomEmoji))
                .features(stringListOf(data.getJsonArray("features")))
                .mfaLevel(MFALevel.byKey(data.getInteger("mfa_level", 0)))
                .applicationId(data.getString("application_id"))
                .widgetEnabled(data.getBoolean("widget_enabled", false))
                .widgetChannelId(data.getString("widget_channel_id"))
                .systemChannelId(data.getString("system_channel_id"))
                .joinedAt(parseTimestamp(data.getString("joined_at")))
                .large(data.getBoolean("large", false))
                .unavailable(data.getBoolean("unavailable", false))
                .memberCount(data.getInteger("member_count", -1))
                //.members(immutableListOf(data.getJsonArray("members"), e -> createMember(data.getString("id"), e)))
                //.channels(immutableListOf(data.getJsonArray("channels"), this::createChannel))
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
                .createdAt(parseTimestamp(data.getString("created_at")))
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
}
