package com.mewna.catnip.rest;

import com.google.common.collect.ImmutableMap;
import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.EntityBuilder;
import com.mewna.catnip.entity.Message;
import com.mewna.catnip.rest.RestRequester.OutboundRequest;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import lombok.Getter;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * @author amy
 * @since 9/1/18.
 */
public class Rest {
    @Getter
    private final Catnip catnip;
    
    public Rest(final Catnip catnip) {
        this.catnip = catnip;
    }
    
    @Nonnull
    public Future<Message> createMessage(@Nonnull final String channelId, @Nonnull final String message) {
        return catnip._requester().queue(new OutboundRequest(Routes.CREATE_MESSAGE.withMajorParam(channelId),
                ImmutableMap.of(), new JsonObject().put("content", message)))
                .map(EntityBuilder::createMessage);
    }
    
    @Nonnull
    @CheckReturnValue
    public Future<Message> getMessage(@Nonnull final String channelId, @Nonnull final String messageId) {
        return catnip._requester().queue(new OutboundRequest(Routes.GET_CHANNEL_MESSAGE.withMajorParam(channelId).compile("message.id", messageId),
                ImmutableMap.of(), null
        )).map(EntityBuilder::createMessage);
    }
}
