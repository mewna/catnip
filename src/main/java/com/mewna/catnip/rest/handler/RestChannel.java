package com.mewna.catnip.rest.handler;

import com.google.common.collect.ImmutableMap;
import com.mewna.catnip.entity.impl.MessageImpl;
import com.mewna.catnip.entity.builder.MessageBuilder;
import com.mewna.catnip.internal.CatnipImpl;
import com.mewna.catnip.rest.ResponsePayload;
import com.mewna.catnip.rest.RestRequester.OutboundRequest;
import com.mewna.catnip.rest.Routes;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.CompletableFuture;

/**
 * @author amy
 * @since 9/3/18.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
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
    public CompletableFuture<MessageImpl> sendMessage(@Nonnull final String channelId, @Nonnull final String content) {
        return sendMessage(channelId, new MessageBuilder().content(content).build());
    }
    
    @Nonnull
    public CompletableFuture<MessageImpl> sendMessage(@Nonnull final String channelId, @Nonnull final MessageImpl message) {
        final JsonObject json = new JsonObject();
        if(message.content() != null && !message.content().isEmpty()) {
            json.put("content", message.content());
        }
        if(message.embeds() != null && !message.embeds().isEmpty()) {
            json.put("embeds", new JsonArray(message.embeds()));
        }
        if(json.getValue("embeds", null) == null && json.getValue("content", null) == null) {
            throw new IllegalArgumentException("Can't build a message with no content and no embeds!");
        }
        
        return getCatnip().requester().
                queue(new OutboundRequest(Routes.CREATE_MESSAGE.withMajorParam(channelId), ImmutableMap.of(), json))
                .thenApply(ResponsePayload::object)
                .thenApply(getEntityBuilder()::createMessage);
    }
    
    @Nonnull
    @CheckReturnValue
    public CompletableFuture<MessageImpl> getMessage(@Nonnull final String channelId, @Nonnull final String messageId) {
        return getCatnip().requester().queue(
                new OutboundRequest(Routes.GET_CHANNEL_MESSAGE.withMajorParam(channelId),
                        ImmutableMap.of("message.id", messageId), null))
                .thenApply(ResponsePayload::object)
                .thenApply(getEntityBuilder()::createMessage);
    }
    
    @Nonnull
    public CompletableFuture<MessageImpl> editMessage(@Nonnull final String channelId, @Nonnull final String messageId,
                                                      @Nonnull final String content) {
        return editMessage(channelId, messageId, new MessageBuilder().content(content).build());
    }
    
    @Nonnull
    public CompletableFuture<MessageImpl> editMessage(@Nonnull final String channelId, @Nonnull final String messageId,
                                                      @Nonnull final MessageImpl message) {
        final JsonObject json = new JsonObject();
        if(message.content() != null && !message.content().isEmpty()) {
            json.put("content", message.content());
        }
        if(message.embeds() != null && !message.embeds().isEmpty()) {
            json.put("embeds", new JsonArray(message.embeds()));
        }
        if(json.getValue("embeds", null) == null && json.getValue("content", null) == null) {
            throw new IllegalArgumentException("Can't build a message with no content and no embeds!");
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
    public CompletableFuture<Void> addReaction(@Nonnull final String channelId, @Nonnull final String messageId,
                                               @Nonnull final String emoji) {
        return getCatnip().requester().queue(new OutboundRequest(Routes.CREATE_REACTION.withMajorParam(channelId),
                ImmutableMap.of("message.id", messageId, "emoji", encodeUTF8(emoji)), new JsonObject()))
                .thenApply(__ -> null);
    }
}
