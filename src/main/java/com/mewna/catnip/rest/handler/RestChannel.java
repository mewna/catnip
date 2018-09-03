package com.mewna.catnip.rest.handler;

import com.google.common.collect.ImmutableMap;
import com.mewna.catnip.entity.Message;
import com.mewna.catnip.entity.MessageBuilder;
import com.mewna.catnip.internal.CatnipImpl;
import com.mewna.catnip.rest.ResponsePayload;
import com.mewna.catnip.rest.RestRequester.OutboundRequest;
import com.mewna.catnip.rest.Routes;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
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
    
    @Nonnull
    public CompletableFuture<Message> createMessage(@Nonnull final String channelId, @Nonnull final String content) {
        return createMessage(channelId, new MessageBuilder().content(content).build());
    }
    
    @Nonnull
    public CompletableFuture<Message> createMessage(@Nonnull final String channelId, @Nonnull final Message message) {
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
                                                  @Nonnull final Message message) {
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
}
