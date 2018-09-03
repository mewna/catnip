package com.mewna.catnip.rest;

import com.google.common.collect.ImmutableMap;
import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.EntityBuilder;
import com.mewna.catnip.entity.Message;
import com.mewna.catnip.entity.MessageBuilder;
import com.mewna.catnip.rest.RestRequester.OutboundRequest;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.Getter;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * @author amy
 * @since 9/1/18.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Rest {
    @Getter
    private final Catnip catnip;
    
    public Rest(final Catnip catnip) {
        this.catnip = catnip;
    }
    
    @Nonnull
    public Future<Message> createMessage(@Nonnull final String channelId, @Nonnull final String content) {
        return createMessage(channelId, new MessageBuilder().content(content).build());
    }
    
    @Nonnull
    public Future<Message> createMessage(@Nonnull final String channelId, @Nonnull final Message message) {
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
        
        return catnip._requester().queue(new OutboundRequest(Routes.CREATE_MESSAGE.withMajorParam(channelId),
                ImmutableMap.of(), json)).map(EntityBuilder::createMessage);
    }
    
    @Nonnull
    @CheckReturnValue
    public Future<Message> getMessage(@Nonnull final String channelId, @Nonnull final String messageId) {
        return catnip._requester().queue(
                new OutboundRequest(Routes.GET_CHANNEL_MESSAGE.withMajorParam(channelId),
                        ImmutableMap.of("message.id", messageId), null)).map(EntityBuilder::createMessage);
    }
    
    @Nonnull
    public Future<Message> editMessage(@Nonnull final String channelId, @Nonnull final String messageId,
                                       @Nonnull final String content) {
        return editMessage(channelId, messageId, new MessageBuilder().content(content).build());
    }
    
    @Nonnull
    public Future<Message> editMessage(@Nonnull final String channelId, @Nonnull final String messageId,
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
        return catnip._requester().queue(new OutboundRequest(Routes.EDIT_MESSAGE.withMajorParam(channelId),
                ImmutableMap.of("message.id", messageId), json)).map(EntityBuilder::createMessage);
    }
}
