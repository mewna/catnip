package com.mewna.catnip.rest.handler;

import com.google.common.collect.ImmutableMap;
import com.mewna.catnip.entity.channel.Channel;
import com.mewna.catnip.entity.channel.GuildChannel;
import com.mewna.catnip.entity.channel.GuildChannel.ChannelEditFields;
import com.mewna.catnip.entity.builder.MessageBuilder;
import com.mewna.catnip.entity.channel.Webhook;
import com.mewna.catnip.entity.guild.PermissionOverride;
import com.mewna.catnip.entity.guild.PermissionOverride.OverrideType;
import com.mewna.catnip.entity.impl.EntityBuilder;
import com.mewna.catnip.entity.impl.WebhookImpl;
import com.mewna.catnip.entity.message.Embed;
import com.mewna.catnip.entity.message.Message;
import com.mewna.catnip.entity.misc.CreatedInvite;
import com.mewna.catnip.entity.misc.Emoji;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.entity.util.Permission;
import com.mewna.catnip.internal.CatnipImpl;
import com.mewna.catnip.rest.ResponsePayload;
import com.mewna.catnip.rest.RestRequester.OutboundRequest;
import com.mewna.catnip.rest.Routes;
import com.mewna.catnip.rest.invite.InviteCreateOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @author amy
 * @since 9/3/18.
 */
@SuppressWarnings({"unused", "WeakerAccess", "ConstantConditions"})
public class RestChannel extends RestHandler {
    public RestChannel(final CatnipImpl catnip) {
        super(catnip);
    }
    
    // Copied from JDA:
    // https://github.com/DV8FromTheWorld/JDA/blob/9e593c5d5e1abf0967998ac5fcc0d915495e0758/src/main/java/net/dv8tion/jda/core/utils/MiscUtil.java#L179-L198
    // Thank JDA devs! <3
    private static String encodeUTF8(final String chars) {
        try {
            return URLEncoder.encode(chars, "UTF-8");
        } catch(final UnsupportedEncodingException e) {
            throw new AssertionError(e); // thanks JDK 1.4
        }
    }
    
    @Nonnull
    public CompletableFuture<Message> sendMessage(@Nonnull final String channelId, @Nonnull final String content) {
        return sendMessage(channelId, new MessageBuilder().content(content).build());
    }
    
    @Nonnull
    public CompletableFuture<Message> sendMessage(@Nonnull final String channelId, @Nonnull final Embed embed) {
        return sendMessage(channelId, new MessageBuilder().embed(embed).build());
    }
    
    @Nonnull
    public CompletableFuture<Message> sendMessage(@Nonnull final String channelId, @Nonnull final Message message) {
        final JsonObject json = new JsonObject();
        if(message.content() != null && !message.content().isEmpty()) {
            json.put("content", message.content());
        }
        if(message.embeds() != null && !message.embeds().isEmpty()) {
            json.put("embed", getEntityBuilder().embedToJson(message.embeds().get(0)));
        }
        if(json.getValue("embed", null) == null && json.getValue("content", null) == null) {
            throw new IllegalArgumentException("Can't build a message with no content and no embeds!");
        }
        
        return getCatnip().requester().
                queue(new OutboundRequest(Routes.CREATE_MESSAGE.withMajorParam(channelId), ImmutableMap.of(), json))
                .thenApply(ResponsePayload::object)
                .thenApply(getEntityBuilder()::createMessage);
    }
    
    @Nonnull
    public CompletableFuture<Message> sendMessage(@Nonnull final String channelId, @Nonnull final Message message,
                                                  @Nonnull final String filename, @Nonnull final byte[] file) {
        final JsonObject json = new JsonObject();
        if(message.content() != null && !message.content().isEmpty()) {
            json.put("content", message.content());
        }
        if(message.embeds() != null && !message.embeds().isEmpty()) {
            json.put("embed", getEntityBuilder().embedToJson(message.embeds().get(0)));
        }
        if(json.getValue("embed", null) == null && json.getValue("content", null) == null) {
            throw new IllegalArgumentException("Can't build a message with no content and no embeds!");
        }
        
        return getCatnip().requester().
                queue(new OutboundRequest(Routes.CREATE_MESSAGE.withMajorParam(channelId), ImmutableMap.of(), json)
                        .binary(Buffer.buffer(file)).filename(filename))
                .thenApply(ResponsePayload::object)
                .thenApply(getEntityBuilder()::createMessage);
    }
    
    @Nonnull
    @CheckReturnValue
    public CompletableFuture<Message> getMessage(@Nonnull final String channelId, @Nonnull final String messageId) {
        return getCatnip().requester().queue(
                new OutboundRequest(Routes.GET_CHANNEL_MESSAGE.withMajorParam(channelId),
                        ImmutableMap.of("message.id", messageId), null))
                .thenApply(ResponsePayload::object)
                .thenApply(getEntityBuilder()::createMessage);
    }
    
    @Nonnull
    public CompletableFuture<Message> editMessage(@Nonnull final String channelId, @Nonnull final String messageId,
                                                  @Nonnull final String content) {
        return editMessage(channelId, messageId, new MessageBuilder().content(content).build());
    }
    
    @Nonnull
    public CompletableFuture<Message> editMessage(@Nonnull final String channelId, @Nonnull final String messageId,
                                                  @Nonnull final Embed embed) {
        return editMessage(channelId, messageId, new MessageBuilder().embed(embed).build());
    }
    
    @Nonnull
    public CompletableFuture<Message> editMessage(@Nonnull final String channelId, @Nonnull final String messageId,
                                                  @Nonnull final Message message) {
        final JsonObject json = new JsonObject();
        if(message.content() != null && !message.content().isEmpty()) {
            json.put("content", message.content());
        }
        if(message.embeds() != null && !message.embeds().isEmpty()) {
            json.put("embed", getEntityBuilder().embedToJson(message.embeds().get(0)));
        }
        if(json.getValue("embed", null) == null && json.getValue("content", null) == null) {
            throw new IllegalArgumentException("Can't build a message with no content and no embed!");
        }
        return getCatnip().requester()
                .queue(new OutboundRequest(Routes.EDIT_MESSAGE.withMajorParam(channelId),
                        ImmutableMap.of("message.id", messageId), json))
                .thenApply(ResponsePayload::object)
                .thenApply(getEntityBuilder()::createMessage);
    }
    
    @Nonnull
    public CompletableFuture<Void> deleteMessage(@Nonnull final String channelId, @Nonnull final String messageId) {
        return getCatnip().requester().queue(new OutboundRequest(Routes.DELETE_MESSAGE.withMajorParam(channelId),
                ImmutableMap.of("message.id", messageId), null)).thenApply(__ -> null);
    }
    
    @Nonnull
    public CompletableFuture<Void> deleteMessages(@Nonnull final String channelId, @Nonnull final List<String> messageIds) {
        return getCatnip().requester()
                .queue(new OutboundRequest(Routes.BULK_DELETE_MESSAGES.withMajorParam(channelId),
                        ImmutableMap.of(), new JsonObject().put("messages", new JsonArray(messageIds))))
                .thenApply(__ -> null);
    }
    
    @Nonnull
    public CompletableFuture<Void> addReaction(@Nonnull final String channelId, @Nonnull final String messageId,
                                               @Nonnull final String emoji) {
        return getCatnip().requester().queue(new OutboundRequest(Routes.CREATE_REACTION.withMajorParam(channelId),
                ImmutableMap.of("message.id", messageId, "emojis", encodeUTF8(emoji)), new JsonObject()))
                .thenApply(__ -> null);
    }
    
    @Nonnull
    public CompletableFuture<Void> addReaction(@Nonnull final String channelId, @Nonnull final String messageId,
                                               @Nonnull final Emoji emoji) {
        return addReaction(channelId, messageId, emoji.forReaction());
    }
    
    @Nonnull
    public CompletableFuture<Void> deleteOwnReaction(@Nonnull final String channelId, @Nonnull final String messageId,
                                                     @Nonnull final String emoji) {
        return getCatnip().requester().queue(new OutboundRequest(Routes.DELETE_OWN_REACTION.withMajorParam(channelId),
                ImmutableMap.of("message.id", messageId, "emojis", encodeUTF8(emoji)), null))
                .thenApply(__ -> null);
    }
    
    @Nonnull
    public CompletableFuture<Void> deleteOwnReaction(@Nonnull final String channelId, @Nonnull final String messageId,
                                                     @Nonnull final Emoji emoji) {
        return deleteOwnReaction(channelId, messageId, emoji.forReaction());
    }
    
    @Nonnull
    public CompletableFuture<Void> deleteAllReactions(@Nonnull final String channelId, @Nonnull final String messageId) {
        return getCatnip().requester().queue(new OutboundRequest(Routes.DELETE_ALL_REACTIONS.withMajorParam(channelId),
                ImmutableMap.of("message.id", messageId), null))
                .thenApply(__ -> null);
    }
    
    @Nonnull
    @CheckReturnValue
    public CompletableFuture<List<User>> getReactions(@Nonnull final String channelId, @Nonnull final String messageId,
                                                      @Nonnull final String emoji, @Nullable final String before,
                                                      @Nullable final String after, @Nonnegative final int limit) {
        final Collection<String> params = new ArrayList<>();
        if (limit > 0) {
            params.add("limit=" + limit);
        }
        if (before != null) {
            params.add("before=" + before);
        }
        if (after != null) {
            params.add("after=" + after);
        }
        String query = String.join("&", params);
        if (!query.isEmpty()) {
            query = '?' + query;
        }
        return getCatnip().requester()
                .queue(new OutboundRequest(Routes.GET_REACTIONS.withMajorParam(channelId).withQueryString(query),
                        ImmutableMap.of("message.id", messageId, "emojis", encodeUTF8(emoji)), null))
                .thenApply(ResponsePayload::array)
                .thenApply(mapObjectContents(getEntityBuilder()::createUser))
                .thenApply(Collections::unmodifiableList);
    }
    
    @Nonnull
    @CheckReturnValue
    public CompletableFuture<List<User>> getReactions(@Nonnull final String channelId, @Nonnull final String messageId,
                                                      @Nonnull final Emoji emoji, @Nullable final String before,
                                                      @Nullable final String after, @Nonnegative final int limit) {
        return getReactions(channelId, messageId, emoji.forReaction(), before, after, limit);
    }
    
    @Nonnull
    @CheckReturnValue
    public CompletableFuture<List<Message>> getChannelMessages(@Nonnull final String channelId, @Nullable final String before,
                                                               @Nullable final String after, @Nullable final String around,
                                                               @Nonnegative final int limit) {
        final Collection<String> params = new ArrayList<>();
        if(limit > 0) {
            params.add("limit=" + limit);
        }
        if(after != null) {
            params.add("after=" + after);
        }
        if(around != null) {
            params.add("around=" + around);
        }
        if(before != null) {
            params.add("before=" + before);
        }
        String query = String.join("&", params);
        if(!query.isEmpty()) {
            query = '?' + query;
        }
        return getCatnip().requester()
                .queue(new OutboundRequest(Routes.GET_CHANNEL_MESSAGES.withMajorParam(channelId).withQueryString(query),
                        ImmutableMap.of(), null))
                .thenApply(ResponsePayload::array)
                .thenApply(mapObjectContents(getEntityBuilder()::createMessage))
                .thenApply(Collections::unmodifiableList);
    }
    
    @Nonnull
    public CompletableFuture<Void> triggerTypingIndicator(@Nonnull final String channelId) {
        return getCatnip().requester().queue(new OutboundRequest(Routes.TRIGGER_TYPING_INDICATOR.withMajorParam(channelId),
                ImmutableMap.of(), null))
                .thenApply(__ -> null);
    }
    
    @Nonnull
    @CheckReturnValue
    public CompletableFuture<Channel> getChannelById(@Nonnull final String channelId) {
        return getCatnip().requester().queue(new OutboundRequest(Routes.GET_CHANNEL.withMajorParam(channelId),
                ImmutableMap.of(), null))
                .thenApply(ResponsePayload::object)
                .thenApply(getEntityBuilder()::createChannel);
    }
    
    @Nonnull
    @CheckReturnValue
    public CompletableFuture<Channel> deleteChannel(@Nonnull final String channelId) {
        return getCatnip().requester().queue(new OutboundRequest(Routes.DELETE_CHANNEL.withMajorParam(channelId),
                ImmutableMap.of(), null))
                .thenApply(ResponsePayload::object)
                .thenApply(getEntityBuilder()::createChannel);
    }
    
    @Nonnull
    @CheckReturnValue
    public CompletableFuture<CreatedInvite> createInvite(@Nonnull final String channelId, @Nullable final InviteCreateOptions options) {
        return getCatnip().requester().queue(new OutboundRequest(Routes.CREATE_CHANNEL_INVITE.withMajorParam(channelId),
                ImmutableMap.of(), (options == null ? InviteCreateOptions.create() : options).toJson()))
                .thenApply(ResponsePayload::object)
                .thenApply(getEntityBuilder()::createCreatedInvite);
    }
    
    @Nonnull
    @CheckReturnValue
    public CompletableFuture<List<CreatedInvite>> getChannelInvites(@Nonnull final String channelId) {
        return getCatnip().requester().queue(new OutboundRequest(Routes.GET_CHANNEL_INVITES.withMajorParam(channelId),
                ImmutableMap.of(), null))
                .thenApply(ResponsePayload::array)
                .thenApply(mapObjectContents(getEntityBuilder()::createCreatedInvite));
    }
    
    @Nonnull
    public CompletableFuture<GuildChannel> modifyChannel(@Nonnull final String channelId, @Nonnull final ChannelEditFields fields) {
        return getCatnip().requester().queue(new OutboundRequest(Routes.MODIFY_CHANNEL.withMajorParam(channelId),
                ImmutableMap.of(), fields.payload()))
                .thenApply(ResponsePayload::object)
                .thenApply(getEntityBuilder()::createGuildChannel);
    }
    
    @Nonnull
    public CompletableFuture<Void> deletePermissionOverride(@Nonnull final String channelId, @Nonnull final String overwriteId) {
        return getCatnip().requester().queue(new OutboundRequest(Routes.DELETE_CHANNEL_PERMISSION.withMajorParam(channelId),
                ImmutableMap.of("overwrite.id", overwriteId), null))
                .thenApply(__ -> null);
    }
    
    @Nonnull
    public CompletableFuture<Void> deletePermissionOverride(@Nonnull final String channelId, @Nonnull final PermissionOverride overwrite) {
        return deletePermissionOverride(channelId, overwrite.id());
    }
    
    @Nonnull
    public CompletableFuture<Void> editPermissionOverride(@Nonnull final String channelId, @Nonnull final String overwriteId,
                                                          @Nonnull final Collection<Permission> allowed,
                                                          @Nonnull final Collection<Permission> denied, final boolean isMember) {
        return getCatnip().requester().queue(new OutboundRequest(Routes.EDIT_CHANNEL_PERMISSIONS.withMajorParam(channelId),
                ImmutableMap.of("overwrite.id", overwriteId), new JsonObject()
                .put("allow", Permission.from(allowed))
                .put("deny", Permission.from(denied))
                .put("type", isMember ? "member" : "role")))
                .thenApply(__ -> null);
    }
    
    @Nonnull
    public CompletableFuture<Void> editPermissionOverride(@Nonnull final String channelId, @Nonnull final PermissionOverride overwrite,
                                                          @Nonnull final Collection<Permission> allowed,
                                                          @Nonnull final Collection<Permission> denied) {
        return editPermissionOverride(channelId, overwrite.id(), allowed, denied, overwrite.type() == OverrideType.MEMBER);
    }
    
    @Nonnull
    @CheckReturnValue
    public CompletableFuture<List<Message>> getPinnedMessages(@Nonnull final String channelId) {
        return getCatnip().requester().queue(new OutboundRequest(Routes.GET_PINNED_MESSAGES.withMajorParam(channelId),
                ImmutableMap.of(), null))
                .thenApply(ResponsePayload::array)
                .thenApply(mapObjectContents(getEntityBuilder()::createMessage));
    }
    
    @Nonnull
    public CompletableFuture<Void> deletePinnedMessage(@Nonnull final String channelId, @Nonnull final String messageId) {
        return getCatnip().requester().queue(new OutboundRequest(Routes.DELETE_PINNED_CHANNEL_MESSAGE.withMajorParam(channelId),
                ImmutableMap.of("message.id", messageId), null))
                .thenApply(__ -> null);
    }
    
    @Nonnull
    public CompletableFuture<Void> deletePinnedMessage(@Nonnull final Message message) {
        return deletePinnedMessage(message.channelId(), message.id());
    }
    
    @Nonnull
    public CompletableFuture<Void> addPinnedMessage(@Nonnull final String channelId, @Nonnull final String messageId) {
        return getCatnip().requester().queue(new OutboundRequest(Routes.ADD_PINNED_CHANNEL_MESSAGE.withMajorParam(channelId),
                ImmutableMap.of("message.id", messageId), new JsonObject()))
                .thenApply(__ -> null);
    }
    
    @Nonnull
    public CompletableFuture<Void> addPinnedMessage(@Nonnull final Message message) {
        return addPinnedMessage(message.channelId(), message.id());
    }
    
    @Nonnull
    public CompletableFuture<Webhook> createWebhook(@Nonnull final String channelId, @Nonnull final String name, @Nullable final String avatar) {
        return getCatnip().requester().queue(new OutboundRequest(Routes.CREATE_WEBHOOK.withMajorParam(channelId),
                ImmutableMap.of(), new JsonObject().put("name", name).put("avatar", avatar)))
                .thenApply(ResponsePayload::object)
                .thenApply(getEntityBuilder()::createWebhook);
    }
}
