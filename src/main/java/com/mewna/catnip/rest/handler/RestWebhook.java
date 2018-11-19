/*
 * Copyright (c) 2018 amy, All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.mewna.catnip.rest.handler;

import com.google.common.collect.ImmutableMap;
import com.mewna.catnip.entity.channel.Webhook;
import com.mewna.catnip.entity.channel.Webhook.WebhookEditFields;
import com.mewna.catnip.entity.message.Message;
import com.mewna.catnip.entity.message.MessageOptions;
import com.mewna.catnip.internal.CatnipImpl;
import com.mewna.catnip.rest.ResponsePayload;
import com.mewna.catnip.rest.RestRequester.OutboundRequest;
import com.mewna.catnip.rest.Routes;
import io.vertx.core.json.JsonObject;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
    @SuppressWarnings("unused")
    public CompletionStage<Webhook> getWebhook(@Nonnull final String webhookId) {
        return getCatnip().requester().queue(new OutboundRequest(Routes.GET_WEBHOOK.withMajorParam(webhookId),
                ImmutableMap.of()))
                .thenApply(ResponsePayload::object)
                .thenApply(getEntityBuilder()::createWebhook);
    }
    
    @Nonnull
    @CheckReturnValue
    public CompletionStage<List<Webhook>> getGuildWebhooks(@Nonnull final String guildId) {
        return getCatnip().requester().queue(new OutboundRequest(Routes.GET_GUILD_WEBHOOKS.withMajorParam(guildId),
                ImmutableMap.of()))
                .thenApply(ResponsePayload::array)
                .thenApply(mapObjectContents(getEntityBuilder()::createWebhook));
    }
    
    @Nonnull
    @CheckReturnValue
    public CompletionStage<List<Webhook>> getChannelWebhooks(@Nonnull final String channelId) {
        return getCatnip().requester().queue(new OutboundRequest(Routes.GET_CHANNEL_WEBHOOKS.withMajorParam(channelId),
                ImmutableMap.of()))
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
                ImmutableMap.of()))
                .thenApply(__ -> null);
    }
    
    @Nonnull
    @CheckReturnValue
    @SuppressWarnings("unused")
    public CompletionStage<Message> executeWebhook(@Nonnull final String webhookId, @Nonnull final String webhookToken,
                                                   @Nonnull final MessageOptions options) {
        return executeWebhook(webhookId, webhookToken, null, null, options);
    }
    
    @Nonnull
    @CheckReturnValue
    @SuppressWarnings("WeakerAccess")
    public CompletionStage<Message> executeWebhook(@Nonnull final String webhookId, @Nonnull final String webhookToken,
                                                   @Nullable final String username, @Nullable final String avatarUrl,
                                                   @Nonnull final MessageOptions options) {
        final JsonObject json = new JsonObject();
        
        if(options.content() != null && !options.content().isEmpty()) {
            json.put("content", options.content());
        }
        
        if(options.embed() != null) {
            json.put("embed", getEntityBuilder().embedToJson(options.embed()));
        }
        
        if(json.getValue("embed", null) == null && json.getValue("content", null) == null
                && !options.hasFiles()) {
            throw new IllegalArgumentException("Can't build a message with no content, no embeds and no files!");
        }
        
        if(username != null && !username.isEmpty()) {
            json.put("username", username);
        }
        if(avatarUrl != null && !avatarUrl.isEmpty()) {
            json.put("avatar_url", avatarUrl);
        }
        
        return getCatnip().requester().
                queue(new OutboundRequest(Routes.EXECUTE_WEBHOOK.withMajorParam(webhookId),
                        ImmutableMap.of("webhook.token", webhookToken), json).needsToken(false)
                        .buffers(options.files()))
                .thenApply(ResponsePayload::object)
                .thenApply(getEntityBuilder()::createMessage);
    }
}
