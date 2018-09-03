package com.mewna.catnip.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mewna.catnip.entity.Embed.Author;
import com.mewna.catnip.entity.Embed.EmbedType;
import com.mewna.catnip.entity.Embed.Field;
import com.mewna.catnip.entity.Embed.Footer;
import com.mewna.catnip.entity.Embed.Image;
import com.mewna.catnip.entity.Embed.Provider;
import com.mewna.catnip.entity.Embed.Thumbnail;
import com.mewna.catnip.entity.Embed.Video;
import com.mewna.catnip.entity.Message.MessageType;
import com.mewna.catnip.internal.CatnipImpl;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author natanbc
 * @since 9/2/18.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public final class EntityBuilder {
    private final CatnipImpl catnip;
    
    public EntityBuilder(final CatnipImpl catnip) {
        this.catnip = catnip;
    }
    
    @Nonnull
    @CheckReturnValue
    public Embed createEmbed(final JsonObject data) {
        final String timestampRaw = data.getString("timestamp");

        final JsonObject footerRaw = data.getJsonObject("footer");
        final Footer footer = footerRaw == null ? null : Footer.builder()
                .text(footerRaw.getString("text"))
                .iconUrl(footerRaw.getString("icon_url"))
                .proxyIconUrl(footerRaw.getString("proxy_icon_url"))
                .build();

        final JsonObject imageRaw = data.getJsonObject("image");
        final Image image = imageRaw == null ? null : Image.builder()
                .url(imageRaw.getString("url"))
                .proxyUrl(imageRaw.getString("proxy_url"))
                .height(imageRaw.getInteger("height", -1))
                .width(imageRaw.getInteger("width", -1))
                .build();

        final JsonObject thumbnailRaw = data.getJsonObject("thumbnail");
        final Thumbnail thumbnail = thumbnailRaw == null ? null : Thumbnail.builder()
                .url(thumbnailRaw.getString("url"))
                .proxyUrl(thumbnailRaw.getString("proxy_url"))
                .height(thumbnailRaw.getInteger("height", -1))
                .width(thumbnailRaw.getInteger("width", -1))
                .build();

        final JsonObject videoRaw = data.getJsonObject("video");
        final Video video = videoRaw == null ? null : Video.builder()
                .url(videoRaw.getString("url"))
                .height(videoRaw.getInteger("height", -1))
                .width(videoRaw.getInteger("width", -1))
                .build();

        final JsonObject providerRaw = data.getJsonObject("provider");
        final Provider provider = providerRaw == null ? null : Provider.builder()
                .name(providerRaw.getString("name"))
                .url(providerRaw.getString("url"))
                .build();

        final JsonObject authorRaw = data.getJsonObject("author");
        final Author author = authorRaw == null ? null : Author.builder()
                .name(authorRaw.getString("name"))
                .url(authorRaw.getString("url"))
                .iconUrl(authorRaw.getString("icon_url"))
                .proxyIconUrl(authorRaw.getString("proxy_icon_url"))
                .build();

        final JsonArray fieldsRaw = data.getJsonArray("fields", new JsonArray());
        final Collection<Field> fields = new ArrayList<>(fieldsRaw.size());
        for(final Object object : fieldsRaw) {
            if(!(object instanceof JsonObject)) {
                throw new IllegalArgumentException("Expected all embed fields to be JsonObjects, but found " +
                        (object == null ? "null" : object.getClass())
                );
            }
            final JsonObject fieldObject = (JsonObject)object;
            fields.add(Field.builder()
                    .name(fieldObject.getString("name"))
                    .value(fieldObject.getString("value"))
                    .inline(fieldObject.getBoolean("inline", false))
                    .build()
            );
        }


        return Embed.builder()
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
        return Role.builder()
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
        return User.builder()
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
        return Member.builder()
                .id(id)
                .deaf(data.getBoolean("deaf"))
                .mute(data.getBoolean("mute"))
                .nick(data.getString("nick"))
                .joinedAt(joinedAtRaw == null ? null : OffsetDateTime.parse(joinedAtRaw))
                .roles(ImmutableSet.of()) //TODO: fetch roles from cache? or at least give the ids
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
    public Message createMessage(@Nonnull final JsonObject data) {
        final List<User> mentionedUsers = data.getJsonArray("mentions").stream().filter(e -> e instanceof JsonObject)
                .map(e -> (JsonObject) e).map(this::createUser).collect(Collectors.toList());

        final User author = createUser(data.getJsonObject("author"));

        final JsonObject memberRaw = data.getJsonObject("member");
        final Member member = memberRaw == null ? null : createMember(author, memberRaw);

        final String timestampRaw = data.getString("timestamp");
        final String editedTimestampRaw = data.getString("edited_timestamp");

        final JsonArray embedsRaw = data.getJsonArray("embeds", new JsonArray());
        final Collection<Embed> embeds = new ArrayList<>(embedsRaw.size());
        for(final Object object : embedsRaw) {
            if(!(object instanceof JsonObject)) {
                throw new IllegalArgumentException("Expected all embeds to be JsonObjects, but found " +
                        (object == null ? "null" : object.getClass())
                );
            }
            embeds.add(createEmbed((JsonObject)object));
        }

        return Message.builder()
                .type(MessageType.byId(data.getInteger("type")))
                .tts(data.getBoolean("tts"))
                .timestamp(timestampRaw == null ? null : OffsetDateTime.parse(timestampRaw))
                .pinned(data.getBoolean("pinned"))
                .nonce(data.getString("nonce"))
                .mentionedUsers(mentionedUsers)
                .mentionedRoles(ImmutableList.of())
                .member(member)
                .id(data.getString("id"))
                .embeds(ImmutableList.copyOf(embeds))
                .editedTimestamp(editedTimestampRaw == null ? null : OffsetDateTime.parse(editedTimestampRaw))
                .content(data.getString("content"))
                .channelId(data.getString("channel_id"))
                .author(author)
                .attachments(ImmutableList.of())
                .guildId(data.getString("guild_id"))
                .build();
    }
}
