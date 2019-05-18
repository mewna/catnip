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

import com.mewna.catnip.entity.channel.Webhook;
import com.mewna.catnip.entity.channel.Webhook.WebhookEditFields;
import com.mewna.catnip.entity.message.Message;
import com.mewna.catnip.entity.message.MessageOptions;
import com.mewna.catnip.internal.CatnipImpl;
import com.mewna.catnip.rest.ResponsePayload;
import com.mewna.catnip.rest.Routes;
import com.mewna.catnip.rest.requester.Requester.OutboundRequest;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

import static com.mewna.catnip.util.JsonUtil.mapObjectContents;

/**
 * @author natanbc
 * @since 9/15/18
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class RestWebhook extends RestHandler {
    public RestWebhook(final CatnipImpl catnip) {
        super(catnip);
    }
    
    @Nonnull
    @CheckReturnValue
    public Single<Webhook> getWebhook(@Nonnull final String webhookId) {
        return Single.fromObservable(getWebhookRaw(webhookId).map(entityBuilder()::createWebhook));
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<JsonObject> getWebhookRaw(@Nonnull final String webhookId) {
        return catnip().requester().queue(new OutboundRequest(Routes.GET_WEBHOOK.withMajorParam(webhookId),
                Map.of()))
                .map(ResponsePayload::object);
    }
    
    @Nonnull
    @CheckReturnValue
    public Single<Webhook> getWebhookToken(@Nonnull final String webhookId, @Nonnull final String token) {
        return Single.fromObservable(getWebhookTokenRaw(webhookId, token).map(entityBuilder()::createWebhook));
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<JsonObject> getWebhookTokenRaw(@Nonnull final String webhookId, @Nonnull final String token) {
        return catnip().requester().queue(new OutboundRequest(Routes.GET_WEBHOOK_TOKEN.withMajorParam(webhookId),
                Map.of("webhook.token", token)))
                .map(ResponsePayload::object);
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<Webhook> getGuildWebhooks(@Nonnull final String guildId) {
        return getGuildWebhooksRaw(guildId)
                .map(e -> mapObjectContents(entityBuilder()::createWebhook).apply(e))
                .flatMapIterable(e -> e);
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<JsonArray> getGuildWebhooksRaw(@Nonnull final String guildId) {
        return catnip().requester().queue(new OutboundRequest(Routes.GET_GUILD_WEBHOOKS.withMajorParam(guildId),
                Map.of()))
                .map(ResponsePayload::array);
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<Webhook> getChannelWebhooks(@Nonnull final String channelId) {
        return getChannelWebhooksRaw(channelId)
                .map(e -> mapObjectContents(entityBuilder()::createWebhook).apply(e))
                .flatMapIterable(e -> e);
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<JsonArray> getChannelWebhooksRaw(@Nonnull final String channelId) {
        return catnip().requester().queue(new OutboundRequest(Routes.GET_CHANNEL_WEBHOOKS.withMajorParam(channelId),
                Map.of()))
                .map(ResponsePayload::array);
    }
    
    @Nonnull
    @CheckReturnValue
    public Single<Webhook> modifyWebhook(@Nonnull final String webhookId, @Nonnull final WebhookEditFields fields,
                                         @Nullable final String reason) {
        return Single.fromObservable(modifyWebhookRaw(webhookId, fields, reason).map(entityBuilder()::createWebhook));
    }
    
    @Nonnull
    @CheckReturnValue
    public Single<Webhook> modifyWebhook(@Nonnull final String webhookId, @Nonnull final WebhookEditFields fields) {
        return modifyWebhook(webhookId, fields, null);
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<JsonObject> modifyWebhookRaw(@Nonnull final String webhookId,
                                                   @Nonnull final WebhookEditFields fields,
                                                   @Nullable final String reason) {
        return catnip().requester().queue(new OutboundRequest(Routes.MODIFY_WEBHOOK.withMajorParam(webhookId),
                Map.of(), fields.payload(), reason))
                .map(ResponsePayload::object);
    }
    
    @Nonnull
    @CheckReturnValue
    public Completable deleteWebhook(@Nonnull final String webhookId, @Nullable final String reason) {
        return Completable.fromObservable(catnip().requester()
                .queue(new OutboundRequest(Routes.DELETE_WEBHOOK.withMajorParam(webhookId),
                        Map.of()).reason(reason)));
    }
    
    @Nonnull
    @CheckReturnValue
    public Completable deleteWebhook(@Nonnull final String webhookId) {
        return deleteWebhook(webhookId, null);
    }
    
    @Nonnull
    @CheckReturnValue
    @SuppressWarnings("unused")
    public Single<Message> executeWebhook(@Nonnull final String webhookId, @Nonnull final String webhookToken,
                                          @Nonnull final MessageOptions options) {
        return executeWebhook(webhookId, webhookToken, null, null, options);
    }
    
    @Nonnull
    @CheckReturnValue
    @SuppressWarnings("WeakerAccess")
    public Single<Message> executeWebhook(@Nonnull final String webhookId, @Nonnull final String webhookToken,
                                          @Nullable final String username, @Nullable final String avatarUrl,
                                          @Nonnull final MessageOptions options) {
        return Single.fromObservable(executeWebhookRaw(webhookId, webhookToken, username, avatarUrl, options)
                .map(entityBuilder()::createMessage));
    }
    
    @Nonnull
    @CheckReturnValue
    @SuppressWarnings("WeakerAccess")
    public Observable<JsonObject> executeWebhookRaw(@Nonnull final String webhookId, @Nonnull final String webhookToken,
                                                    @Nullable final String username, @Nullable final String avatarUrl,
                                                    @Nonnull final MessageOptions options) {
        final JsonObject body = new JsonObject();
        
        if(options.content() != null && !options.content().isEmpty()) {
            body.put("content", options.content());
        }
        
        if(options.embed() != null) {
            body.put("embeds", new JsonArray().add(entityBuilder().embedToJson(options.embed())));
        }
        
        if(body.getValue("embeds", null) == null && body.getValue("content", null) == null
                && !options.hasFiles()) {
            throw new IllegalArgumentException("Can't build a message with no content, no embeds and no files!");
        }
        
        if(username != null && !username.isEmpty()) {
            body.put("username", username);
        }
        if(avatarUrl != null && !avatarUrl.isEmpty()) {
            body.put("avatar_url", avatarUrl);
        }
        
        return catnip().requester().
                queue(new OutboundRequest(Routes.EXECUTE_WEBHOOK.withMajorParam(webhookId),
                        Map.of("webhook.token", webhookToken), body).needsToken(false)
                        .buffers(options.files()))
                .map(ResponsePayload::object);
    }
}
