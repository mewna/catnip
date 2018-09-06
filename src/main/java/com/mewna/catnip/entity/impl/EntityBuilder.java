package com.mewna.catnip.entity.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.*;
import com.mewna.catnip.entity.Embed.EmbedType;
import com.mewna.catnip.entity.Emoji.CustomEmoji;
import com.mewna.catnip.entity.Emoji.UnicodeEmoji;
import com.mewna.catnip.entity.impl.EmbedImpl.*;
import com.mewna.catnip.entity.impl.MessageImpl.Attachment;
import com.mewna.catnip.entity.impl.MessageImpl.Reaction;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author natanbc
 * @since 9/2/18.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public final class EntityBuilder {
    private static final JsonArray EMPTY_JSON_ARRAY = new JsonArray();
    
    @SuppressWarnings("FieldCanBeLocal")
    private final Catnip catnip;
    
    public EntityBuilder(final Catnip catnip) {
        this.catnip = catnip;
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
    public Embed createEmbed(final JsonObject data) {
        final String timestampRaw = data.getString("timestamp");
        
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
        
        final JsonArray fieldsRaw = data.getJsonArray("fields", EMPTY_JSON_ARRAY);
        final Collection<Field> fields = new ArrayList<>(fieldsRaw.size());
        for(final Object object : fieldsRaw) {
            if(!(object instanceof JsonObject)) {
                throw new IllegalArgumentException("Expected all embed fields to be JsonObjects, but found " +
                        (object == null ? "null" : object.getClass())
                );
            }
            final JsonObject fieldObject = (JsonObject) object;
            fields.add(Field.builder()
                    .name(fieldObject.getString("name"))
                    .value(fieldObject.getString("value"))
                    .inline(fieldObject.getBoolean("inline", false))
                    .build()
            );
        }
        
        return EmbedImpl.builder()
                .title(data.getString("title"))
                .type(EmbedType.byKey(data.getString("type")))
                .description(data.getString("description"))
                .url(data.getString("url"))
                .timestamp(timestampRaw == null ? null : OffsetDateTime.parse(timestampRaw))
                .color(data.getInteger("color", null))
                .footer(footer)
                .image(image)
                .thumbnail(thumbnail)
                .video(video)
                .provider(provider)
                .author(author)
                .fields(ImmutableList.copyOf(fields))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public Role createRole(@Nonnull final JsonObject data) {
        return RoleImpl.builder()
                .catnip(catnip)
                .id(data.getString("id"))
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
    public Member createMember(@Nonnull final String id, @Nonnull final JsonObject data) {
        final String joinedAtRaw = data.getString("joined_at");
        return MemberImpl.builder()
                .catnip(catnip)
                .id(id)
                .nick(data.getString("nick"))
                .roles(ImmutableSet.of()) // TODO: fetch roles from cache? or at least give the ids
                .joinedAt(joinedAtRaw == null ? null : OffsetDateTime.parse(joinedAtRaw))
                .deaf(data.getBoolean("deaf"))
                .mute(data.getBoolean("mute"))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public Member createMember(@Nonnull final User user, @Nonnull final JsonObject data) {
        return createMember(user.id(), data);
    }
    
    @Nonnull
    @CheckReturnValue
    public Member createMember(@Nonnull final JsonObject data) {
        return createMember(createUser(data.getJsonObject("user")), data);
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
        final JsonArray rolesRaw = data.getJsonArray("mention_roles", EMPTY_JSON_ARRAY);
        final Collection<String> roles = new ArrayList<>(rolesRaw.size());
        for(final Object object : rolesRaw) {
            if(!(object instanceof String)) {
                throw new IllegalArgumentException("Expected all role ids to be strings, but found " +
                        (object == null ? "null" : object.getClass()));
            }
            roles.add((String)object);
        }
        
        final JsonObject userRaw = data.getJsonObject("user");
        
        return CustomEmojiImpl.builder()
                .catnip(catnip)
                .id(data.getString("id"))
                .name(data.getString("name"))
                .roles(ImmutableList.copyOf(roles))
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
        final List<User> mentionedUsers = data.getJsonArray("mentions").stream().filter(e -> e instanceof JsonObject)
                .map(e -> (JsonObject) e).map(this::createUser).collect(Collectors.toList());
        
        final User author = createUser(data.getJsonObject("author"));
        
        final JsonObject memberRaw = data.getJsonObject("member");
        final Member member = memberRaw == null ? null : createMember(author, memberRaw);
        
        final String timestampRaw = data.getString("timestamp");
        final String editedTimestampRaw = data.getString("edited_timestamp");
        
        final JsonArray embedsRaw = data.getJsonArray("embeds", EMPTY_JSON_ARRAY);
        final Collection<Embed> embeds = new ArrayList<>(embedsRaw.size());
        for(final Object object : embedsRaw) {
            if(!(object instanceof JsonObject)) {
                throw new IllegalArgumentException("Expected all embeds to be JsonObjects, but found " +
                        (object == null ? "null" : object.getClass()));
            }
            embeds.add(createEmbed((JsonObject) object));
        }
        
        final JsonArray mentionedRolesRaw = data.getJsonArray("mention_roles", EMPTY_JSON_ARRAY);
        final Collection<String> mentionedRoles = new ArrayList<>(mentionedRolesRaw.size());
        for(final Object object : mentionedRolesRaw) {
            if(!(object instanceof String)) {
                throw new IllegalArgumentException("Expected all role mentions to be strings, but found " +
                        (object == null ? "null" : object.getClass()));
            }
            mentionedRoles.add((String)object);
        }
        
        final JsonArray attachmentsRaw = data.getJsonArray("attachments", EMPTY_JSON_ARRAY);
        final Collection<Message.Attachment> attachments = new ArrayList<>(attachmentsRaw.size());
        for(final Object object : attachmentsRaw) {
            if(!(object instanceof JsonObject)) {
                throw new IllegalArgumentException("Expected all attachments to be JsonObjects, but found " +
                        (object == null ? "null" : object.getClass()));
            }
            attachments.add(createAttachment((JsonObject) object));
        }
    
        final JsonArray reactionsRaw = data.getJsonArray("reactions", EMPTY_JSON_ARRAY);
        final Collection<Message.Reaction> reactions = new ArrayList<>(reactionsRaw.size());
        for(final Object object : reactionsRaw) {
            if(!(object instanceof JsonObject)) {
                throw new IllegalArgumentException("Expected all reactions to be JsonObjects, but found " +
                        (object == null ? "null" : object.getClass()));
            }
            reactions.add(createReaction((JsonObject) object));
        }
        
        return MessageImpl.builder()
                .catnip(catnip)
                .id(data.getString("id"))
                .channelId(data.getString("channel_id"))
                .author(author)
                .content(data.getString("content"))
                .timestamp(timestampRaw == null ? null : OffsetDateTime.parse(timestampRaw))
                .editedTimestamp(editedTimestampRaw == null ? null : OffsetDateTime.parse(editedTimestampRaw))
                .tts(data.getBoolean("tts"))
                .mentionsEveryone(data.getBoolean("mention_everyone", false))
                .mentionedUsers(mentionedUsers)
                .mentionedRoles(ImmutableList.copyOf(mentionedRoles))
                .attachments(ImmutableList.copyOf(attachments))
                .embeds(ImmutableList.copyOf(embeds))
                .reactions(ImmutableList.copyOf(reactions))
                .nonce(data.getString("nonce"))
                .pinned(data.getBoolean("pinned"))
                .webhookId(data.getString("webhook_id"))
                .type(MessageType.byId(data.getInteger("type")))
                
                //not actually documented
                .member(member)
                .guildId(data.getString("guild_id"))
                .build();
    }
    
    @CheckReturnValue
    private static boolean isInvalid(@Nullable final JsonObject object, @Nonnull final String key) {
        return object == null || !object.containsKey(key);
    }
}
