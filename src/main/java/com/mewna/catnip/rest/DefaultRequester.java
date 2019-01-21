/*
 * Copyright (c) 2019 amy, All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *  3. Neither the name of the copyright holder nor the names of its contributors
 *     may be used to endorse or promote products derived from this software without
 *     specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 *  FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 *  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *  CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.mewna.catnip.rest;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.extension.Extension;
import com.mewna.catnip.extension.hook.CatnipHook;
import com.mewna.catnip.rest.Routes.Route;
import com.mewna.catnip.util.CatnipMeta;
import com.mewna.catnip.util.SafeVertxCompletableFuture;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.NoStackTraceThrowable;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import okhttp3.Request.Builder;
import okhttp3.internal.http.HttpMethod;
import okio.BufferedSink;
import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static io.vertx.core.http.HttpMethod.GET;

public class DefaultRequester implements Requester {
    public static final String API_HOST = "https://discordapp.com";
    private static final int API_VERSION = 6;
    public static final String API_BASE = "/api/v" + API_VERSION;
    private static final RequestBody EMPTY_BODY = RequestBody.create(null, new byte[0]);
    
    private final Catnip catnip;
    private final RateLimiter rateLimiter;
    private final OkHttpClient client;
    
    public DefaultRequester(final Catnip catnip, final RateLimiter rateLimiter, final OkHttpClient client) {
        this.catnip = catnip;
        this.rateLimiter = rateLimiter;
        this.client = client;
    }
    
    @Nonnull
    @Override
    public CompletionStage<ResponsePayload> queue(@Nonnull final OutboundRequest r) {
        final CompletableFuture<ResponsePayload> future = new SafeVertxCompletableFuture<>(catnip);
        rateLimiter.requestExecution(r.route().baseRoute())
                .thenRun(() -> executeRequest(r, future))
                .exceptionally(e -> {
                    future.completeExceptionally(e);
                    return null;
                });
        return future;
    }
    
    private void executeRequest(@Nonnull final OutboundRequest r, @Nonnull final CompletableFuture<ResponsePayload> future) {
        Route route = r.route();
        // Compile route for usage
        for(final Entry<String, String> stringStringEntry : r.params().entrySet()) {
            route = route.compile(stringStringEntry.getKey(), stringStringEntry.getValue());
        }
        if(r.buffers() != null) {
            handleRouteBufferBodySend(route, r, future);
        } else {
            handleRouteJsonBodySend(route, r, future);
        }
    }
    
    private void handleRouteBufferBodySend(final Route finalRoute, final OutboundRequest r, final CompletableFuture<ResponsePayload> future) {
        try {
            @SuppressWarnings("UnnecessarilyQualifiedInnerClassAccess")
            final MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            
            for(int index = 0; index < r.buffers().size(); index++) {
                final ImmutablePair<String, Buffer> pair = r.buffers().get(index);
                builder.addFormDataPart("file" + index, pair.left, new MultipartRequestBody(pair.right));
            }
            if(r.object() != null) {
                for(final Extension extension : catnip.extensionManager().extensions()) {
                    for(final CatnipHook hook : extension.hooks()) {
                        r.object(hook.rawRestSendObjectHook(finalRoute, r.object()));
                    }
                }
                builder.addFormDataPart("payload_json", r.object().encode());
            } else if(r.array() != null) {
                builder.addFormDataPart("payload_json", r.array().encode());
            } else {
                builder.addFormDataPart("payload_json", new JsonObject()
                        .putNull("content")
                        .putNull("embed").encode());
            }
            
            executeHttpRequest(r, finalRoute, builder.build(), future);
        } catch(final Exception e) {
            catnip.logAdapter().error("Failed to send multipart request", e);
        }
    }
    
    private void handleRouteJsonBodySend(final Route finalRoute, final OutboundRequest r, final CompletableFuture<ResponsePayload> future) {
        final String encoded;
        if(r.object() != null) {
            for(final Extension extension : catnip.extensionManager().extensions()) {
                for(final CatnipHook hook : extension.hooks()) {
                    r.object(hook.rawRestSendObjectHook(finalRoute, r.object()));
                }
            }
            encoded = r.object().encode();
        } else if(r.array() != null) {
            encoded = r.object().encode();
        } else {
            encoded = null;
        }
        RequestBody body = null;
        if(encoded != null) {
            body = RequestBody.create(MediaType.parse("application/json"), encoded);
        } else if(HttpMethod.requiresRequestBody(r.route().method().name().toUpperCase())) {
            body = EMPTY_BODY;
        }
        executeHttpRequest(r, finalRoute, body, future);
    }
    
    private void executeHttpRequest(final OutboundRequest r, final Route route, final RequestBody body, final CompletableFuture<ResponsePayload> future) {
        final Context context = catnip.vertx().getOrCreateContext();
        final Builder requestBuilder = new Builder().url(API_HOST + API_BASE + route.baseRoute())
                .method(route.method().name(), body)
                .header("User-Agent", "DiscordBot (https://github.com/mewna/catnip, " + CatnipMeta.VERSION + ')');
        if(r.needsToken()) {
            requestBuilder.header("Authorization", "Bot " + catnip.token());
        }
        client.newCall(requestBuilder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@Nonnull final Call call, @Nonnull final IOException e) {
                //massive failure
                future.completeExceptionally(e);
            }
        
            @Override
            public void onResponse(@Nonnull final Call call, @Nonnull final Response resp) throws IOException {
                //ensure we close it no matter what
                try(final Response response = resp) {
                    final int code = response.code();
                    final String message = response.message();
                    if(response.body() == null) {
                        context.runOnContext(__ ->
                                handleResponse(r, code, message, null, response.headers(),
                                        new NoStackTraceThrowable("body == null"), future));
                    } else {
                        final byte[] bodyBytes = response.body().bytes();
                    
                        context.runOnContext(__ ->
                                handleResponse(r, code, message, Buffer.buffer(bodyBytes),
                                        response.headers(), null, future));
                    }
                }
            }
        });
    }
    
    private void handleResponse(final OutboundRequest r, final int statusCode, final String statusMessage,
                                final Buffer body, final Headers headers,
                                final Throwable failureCause, final CompletableFuture<ResponsePayload> future) {
        final long latency;
        final String serverTime = headers.get("Date");
        if(serverTime != null) {
            final long now = System.currentTimeMillis();
            // Parse date header
            // According to JDA, this is the correct format for it.
            // I trust them more than I trust my own attempts to get it right.
            // https://github.com/DV8FromTheWorld/JDA/blob/2e771e053d6ad94c1aebddbfe72e0aa519d9b3ed/src/main/java/net/dv8tion/jda/core/requests/ratelimit/BotRateLimiter.java#L150-L166
            latency =  OffsetDateTime.parse(serverTime, DateTimeFormatter.RFC_1123_DATE_TIME)
                    .toInstant().toEpochMilli() - now;
        } else {
            // We set the bucket's "last request" time to the timestamp of
            // right before we started the request. If it happens that we
            // don't get a Date header from Discord, we can use this
            // timestamp to try to get a somewhat-accurate idea of latency.
            latency = 0;
        }
        if(statusCode == 429) {
            System.out.println("FUCK WE GOT 429");
            String retry = headers.get("Retry-After");
            if(retry == null || retry.isEmpty()) {
                retry = body.toJsonObject().getValue("retry_after").toString();
            }
            final long retryAfter = Long.parseLong(retry);
            System.out.println("retry after = " + retryAfter);
            if(Boolean.parseBoolean(headers.get("X-RateLimit-Global"))) {
                System.out.println("global");
                rateLimiter.updateGlobalRateLimit(System.currentTimeMillis() + latency + retryAfter);
            } else {
                System.out.println("not global");
                updateBucket(r.route().baseRoute(), headers, retryAfter, latency);
            }
            rateLimiter.requestExecution(r.route().baseRoute())
                    .thenRun(() -> executeRequest(r, future))
                    .exceptionally(e -> {
                        future.completeExceptionally(e);
                        return null;
                    });
        } else {
            ResponsePayload payload = new ResponsePayload(body);
            for(final Extension extension : catnip.extensionManager().extensions()) {
                for(final CatnipHook hook : extension.hooks()) {
                    payload = hook.rawRestReceiveDataHook(r.route(), payload);
                }
            }
            // We got a 400, meaning there's errors. Fail the request with this and move on.
            if(statusCode == 400) {
                final RestPayloadException exception = new RestPayloadException();
                final JsonObject errors = payload.object();
                errors.forEach(e -> {
                    //noinspection unchecked
                    exception.addFailure(e.getKey(), (List<String>) e.getValue());
                });
                future.completeExceptionally(exception);
                updateBucket(r.route().baseRoute(), headers, -1, latency);
            } else {
                future.complete(payload);
                final Handler<Long> callback = __ -> {
                    updateBucket(r.route().baseRoute(), headers, -1, latency);
                };
                final boolean hasMemeReactionRateLimits = r.route().method() != GET
                        && r.route().baseRoute().contains("/reactions/");
                if(hasMemeReactionRateLimits) {
                    catnip.vertx().setTimer(250L, callback);
                } else {
                    callback.handle(null);
                }
            }
        }
        
    }
    
    private void updateBucket(@Nonnull final String bucket, @Nonnull final Headers headers,
                              final long retryAfter, final long latency) {
        final String rateLimitReset = headers.get("X-RateLimit-Reset");
        final String rateLimitRemaining = headers.get("X-RateLimit-Remaining");
        final String rateLimitLimit = headers.get("X-RateLimit-Limit");
        
        if(retryAfter > 0) {
            rateLimiter.updateRemaining(bucket, 0);
            rateLimiter.updateReset(bucket, retryAfter);
        }
        
        if(rateLimitReset != null) {
            rateLimiter.updateReset(bucket, latency + Long.parseLong(rateLimitReset) * 1000);
        }
        
        if(rateLimitRemaining != null) {
            rateLimiter.updateRemaining(bucket, Integer.parseInt(rateLimitRemaining));
        }
        
        if(rateLimitLimit != null) {
            rateLimiter.updateLimit(bucket, Integer.parseInt(rateLimitLimit));
        }
        
        rateLimiter.updateDone(bucket);
    }
    
    @RequiredArgsConstructor
    private static class MultipartRequestBody extends RequestBody {
        private static final MediaType contentType = MediaType.parse("application/octet-stream; charset=utf-8");
        
        private final Buffer buffer;
        
        @Nullable
        @Override
        public MediaType contentType() {
            return contentType;
        }
        
        @Override
        public void writeTo(final BufferedSink sink) throws IOException {
            sink.write(buffer.getBytes());
        }
    }
}
