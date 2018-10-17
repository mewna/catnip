package com.mewna.catnip.rest.handler;

import com.google.common.collect.ImmutableMap;
import com.mewna.catnip.entity.channel.Webhook;
import com.mewna.catnip.entity.channel.Webhook.WebhookEditFields;
import com.mewna.catnip.internal.CatnipImpl;
import com.mewna.catnip.rest.ResponsePayload;
import com.mewna.catnip.rest.RestRequester.OutboundRequest;
import com.mewna.catnip.rest.Routes;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * @author natanbc
 * @since 9/15/18
 */
public class RestWebhook extends RestHandler {
    public RestWebhook(final CatnipImpl catnip) {
        super(catnip);
    }
    
    @Nonnull
    @CheckReturnValue
    public CompletionStage<Webhook> getWebhook(@Nonnull final String webhookId) {
        return getCatnip().requester().queue(new OutboundRequest(Routes.GET_WEBHOOK.withMajorParam(webhookId),
                ImmutableMap.of(), null))
                .thenApply(ResponsePayload::object)
                .thenApply(getEntityBuilder()::createWebhook);
    }
    
    @Nonnull
    @CheckReturnValue
    public CompletionStage<List<Webhook>> getGuildWebhooks(@Nonnull final String guildId) {
        return getCatnip().requester().queue(new OutboundRequest(Routes.GET_GUILD_WEBHOOKS.withMajorParam(guildId),
                ImmutableMap.of(), null))
                .thenApply(ResponsePayload::array)
                .thenApply(mapObjectContents(getEntityBuilder()::createWebhook));
    }
    
    @Nonnull
    @CheckReturnValue
    public CompletionStage<List<Webhook>> getChannelWebhooks(@Nonnull final String channelId) {
        return getCatnip().requester().queue(new OutboundRequest(Routes.GET_CHANNEL_WEBHOOKS.withMajorParam(channelId),
                ImmutableMap.of(), null))
                .thenApply(ResponsePayload::array)
                .thenApply(mapObjectContents(getEntityBuilder()::createWebhook));
    }
    
    @Nonnull
    @CheckReturnValue
    public CompletionStage<Webhook> modifyWebhook(@Nonnull final String webhookId, @Nonnull final WebhookEditFields fields) {
        return getCatnip().requester().queue(new OutboundRequest(Routes.MODIFY_WEBHOOK.withMajorParam(webhookId),
                ImmutableMap.of(), fields.payload()))
                .thenApply(ResponsePayload::object)
                .thenApply(getEntityBuilder()::createWebhook);
    }
    
    @Nonnull
    @CheckReturnValue
    public CompletionStage<Void> deleteWebhook(@Nonnull final String webhookId) {
        return getCatnip().requester().queue(new OutboundRequest(Routes.DELETE_WEBHOOK.withMajorParam(webhookId),
                ImmutableMap.of(), null))
                .thenApply(__ -> null);
    }
}
