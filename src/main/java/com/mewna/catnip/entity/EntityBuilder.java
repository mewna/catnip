package com.mewna.catnip.entity;

import com.google.common.collect.ImmutableList;
import com.mewna.catnip.entity.Embed.Author;
import com.mewna.catnip.entity.Embed.EmbedType;
import com.mewna.catnip.entity.Embed.Field;
import com.mewna.catnip.entity.Embed.Footer;
import com.mewna.catnip.entity.Embed.Image;
import com.mewna.catnip.entity.Embed.Provider;
import com.mewna.catnip.entity.Embed.Thumbnail;
import com.mewna.catnip.entity.Embed.Video;
import com.mewna.catnip.entity.Message.MessageType;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author natanbc
 * @since 9/2/18.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public final class EntityBuilder {
    private EntityBuilder() {}

    public static Embed createEmbed(final JsonObject data) {
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

    public static User createUser(final JsonObject data) {
        return data.mapTo(User.class);
    }

    public static Member createMember(final String id, final JsonObject data) {
        return Member.builder()
                .id(id)
                .deaf(data.getBoolean("deaf"))
                .mute(data.getBoolean("mute"))
                .nick(data.getString("nick", null))
                .build();
    }

    public static Member createMember(final User user, final JsonObject data) {
        return createMember(user.getId(), data);
    }

    public static Member createMember(final JsonObject data) {
        return createMember(createUser(data.getJsonObject("user")), data);
    }

    public static Message createMessage(final JsonObject data) {
        // TODO: This WILL go :fire: if a user is mentioned in a message, because a User object can't hold Member data
        final List<User> mentionedUsers = data.getJsonArray("mentions").stream().filter(e -> e instanceof JsonObject)
                .map(e -> ((JsonObject) e).mapTo(User.class)).collect(Collectors.toList());

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
