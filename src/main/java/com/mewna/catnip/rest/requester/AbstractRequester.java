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

package com.mewna.catnip.rest.requester;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.extension.Extension;
import com.mewna.catnip.extension.hook.CatnipHook;
import com.mewna.catnip.rest.ResponsePayload;
import com.mewna.catnip.rest.RestPayloadException;
import com.mewna.catnip.rest.Routes.Route;
import com.mewna.catnip.rest.ratelimit.RateLimiter;
import com.mewna.catnip.util.CatnipMeta;
import com.mewna.catnip.util.SafeVertxCompletableFuture;
import io.vertx.core.Context;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import okhttp3.*;
import okhttp3.Request.Builder;
import okhttp3.internal.http.HttpMethod;
import okio.BufferedSink;
import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static io.vertx.core.http.HttpMethod.PUT;

@SuppressWarnings("WeakerAccess")
public abstract class AbstractRequester implements Requester {
    public static final RequestBody EMPTY_BODY = RequestBody.create(null, new byte[0]);
    
    protected final RateLimiter rateLimiter;
    protected final OkHttpClient.Builder clientBuilder;
    protected Catnip catnip;
    private volatile OkHttpClient client;
    
    public AbstractRequester(@Nonnull final RateLimiter rateLimiter, @Nonnull final OkHttpClient.Builder clientBuilder) {
        this.rateLimiter = rateLimiter;
        this.clientBuilder = clientBuilder;
    }
    
    @Override
    public void catnip(@Nonnull final Catnip catnip) {
        this.catnip = catnip;
        rateLimiter.catnip(catnip);
    }
    
    @Nonnull
    @Override
    public CompletionStage<ResponsePayload> queue(@Nonnull final OutboundRequest r) {
        final CompletableFuture<ResponsePayload> future = new SafeVertxCompletableFuture<>(catnip);
        final Bucket bucket = getBucket(r.route());
        bucket.queueRequest(new QueuedRequest(r, r.route(), future, bucket));
        return future;
    }
    
    @Nonnull
    @CheckReturnValue
    protected abstract Bucket getBucket(@Nonnull Route route);
    
    @Nonnull
    @CheckReturnValue
    protected synchronized OkHttpClient client() {
        if(client != null) {
            return client;
        }
        return client = clientBuilder.build();
    }
    
    protected void executeRequest(@Nonnull final QueuedRequest request) {
        Route route = request.route();
        // Compile route for usage
        for(final Entry<String, String> stringStringEntry : request.request().params().entrySet()) {
            route = route.compile(stringStringEntry.getKey(), stringStringEntry.getValue());
        }
        if(request.request().buffers() != null) {
            handleRouteBufferBodySend(route, request);
        } else {
            handleRouteJsonBodySend(route, request);
        }
    }
    
    protected void handleRouteBufferBodySend(@Nonnull final Route finalRoute, @Nonnull final QueuedRequest request) {
        try {
            @SuppressWarnings("UnnecessarilyQualifiedInnerClassAccess")
            final MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            final OutboundRequest r = request.request();
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
            
            executeHttpRequest(finalRoute, builder.build(), request);
        } catch(final Exception e) {
            catnip.logAdapter().error("Failed to send multipart request", e);
        }
    }
    
    protected void handleRouteJsonBodySend(@Nonnull final Route finalRoute, @Nonnull final QueuedRequest request) {
        final OutboundRequest r = request.request();
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
        executeHttpRequest(finalRoute, body, request);
    }
    
    protected void executeHttpRequest(@Nonnull final Route route, @Nullable final RequestBody body,
                                      @Nonnull final QueuedRequest request) {
        final Context context = catnip.vertx().getOrCreateContext();
        final Builder requestBuilder = new Builder().url(API_HOST + API_BASE + route.baseRoute())
                .method(route.method().name(), body)
                .header("User-Agent", "DiscordBot (https://github.com/mewna/catnip, " + CatnipMeta.VERSION + ')');
        if(request.request().needsToken()) {
            requestBuilder.header("Authorization", "Bot " + catnip.token());
        }
        client().newCall(requestBuilder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@Nonnull final Call call, @Nonnull final IOException e) {
                request.bucket.failedRequest(request, e);
            }
        
            @Override
            public void onResponse(@Nonnull final Call call, @Nonnull final Response resp) throws IOException {
                //ensure we close it no matter what
                try(final Response response = resp) {
                    final int code = response.code();
                    final long requestEnd = System.currentTimeMillis();
                    if(response.body() == null) {
                        context.runOnContext(__ ->
                                handleResponse(code, requestEnd, null, response.headers(), request));
                    } else {
                        final byte[] bodyBytes = response.body().bytes();
                    
                        context.runOnContext(__ ->
                                handleResponse(code, requestEnd, Buffer.buffer(bodyBytes),
                                        response.headers(), request));
                    }
                }
            }
        });
    }
    
    protected void handleResponse(final int statusCode, final long requestEnd,
                                final Buffer body, final Headers headers,
                                @Nonnull final QueuedRequest request) {
        final OutboundRequest r = request.request();
        final long latency;
        final String serverTime = headers.get("Date");
        if(serverTime != null) {
            // Parse date header
            // According to JDA, this is the correct format for it.
            // I trust them more than I trust my own attempts to get it right.
            // https://github.com/DV8FromTheWorld/JDA/blob/2e771e053d6ad94c1aebddbfe72e0aa519d9b3ed/src/main/java/net/dv8tion/jda/core/requests/ratelimit/BotRateLimiter.java#L150-L166
            latency = OffsetDateTime.parse(serverTime, DateTimeFormatter.RFC_1123_DATE_TIME)
                    .toInstant().toEpochMilli() - requestEnd;
        } else {
            latency = 0;
        }
        if(statusCode == 429) {
            catnip.logAdapter().error("Hit 429! Route: {}, X-Ratelimit-Global: {}, X-Ratelimit-Limit: {}, X-Ratelimit-Reset: {}",
                    r.route().baseRoute(),
                    headers.get("X-Ratelimit-Global"),
                    headers.get("X-Ratelimit-Limit"),
                    headers.get("X-Ratelimit-Reset")
            );
            String retry = headers.get("Retry-After");
            if(retry == null || retry.isEmpty()) {
                retry = body.toJsonObject().getValue("retry_after").toString();
            }
            final long retryAfter = Long.parseLong(retry);
            if(Boolean.parseBoolean(headers.get("X-RateLimit-Global"))) {
                rateLimiter.updateGlobalRateLimit(System.currentTimeMillis() + latency + retryAfter);
            } else {
                updateBucket(r.route(), headers,
                        System.currentTimeMillis() + latency + retryAfter, latency);
            }
            rateLimiter.requestExecution(r.route())
                    .thenRun(() -> executeRequest(request))
                    .exceptionally(e -> {
                        request.future().completeExceptionally(e);
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
                final Map<String, List<String>> failures = new HashMap<>();
                final JsonObject errors = payload.object();
                errors.forEach(e -> {
                    //noinspection unchecked
                    failures.put(e.getKey(), (List<String>) e.getValue());
                });
                request.future().completeExceptionally(new RestPayloadException(failures));
                updateBucket(r.route(), headers, -1, latency);
                request.bucket().requestDone();
            } else {
                request.future().complete(payload);
                updateBucket(r.route(), headers, -1, latency);
                request.bucket().requestDone();
            }
        }
        
    }
    
    protected void updateBucket(@Nonnull final Route route, @Nonnull final Headers headers,
                              final long retryAfter, final long latency) {
        final String rateLimitReset = headers.get("X-RateLimit-Reset");
        final String rateLimitRemaining = headers.get("X-RateLimit-Remaining");
        final String rateLimitLimit = headers.get("X-RateLimit-Limit");
        
        if(retryAfter > 0) {
            rateLimiter.updateRemaining(route, 0);
            rateLimiter.updateReset(route, retryAfter);
        }
        
        if(route.method() == PUT && route.baseRoute().contains("/reactions/")) {
            rateLimiter.updateLimit(route, 1);
            rateLimiter.updateReset(route, System.currentTimeMillis()
                    //somehow adding the latency here actually makes 429s happen?????
                    // + latency
                    + 250);
        } else {
            if(rateLimitReset != null) {
                //there used to be a + latency here but, just like above, it also made 429s more likely
                //to happen. don't ask me why.
                rateLimiter.updateReset(route, Long.parseLong(rateLimitReset) * 1000);
            }
    
            if(rateLimitLimit != null) {
                rateLimiter.updateLimit(route, Integer.parseInt(rateLimitLimit));
            }
        }
    
        if(rateLimitRemaining != null) {
            rateLimiter.updateRemaining(route, Integer.parseInt(rateLimitRemaining));
        }
        
        rateLimiter.updateDone(route);
    }
    
    @RequiredArgsConstructor
    protected static class MultipartRequestBody extends RequestBody {
        private static final MediaType contentType = MediaType.parse("application/octet-stream; charset=utf-8");
        
        private final Buffer buffer;
        
        @Nullable
        @Override
        public MediaType contentType() {
            return contentType;
        }
        
        @Override
        public void writeTo(@Nonnull final BufferedSink sink) throws IOException {
            sink.write(buffer.getBytes());
        }
    }
    
    @RequiredArgsConstructor
    @Getter
    @Accessors(fluent = true)
    protected static class QueuedRequest {
        protected final OutboundRequest request;
        protected final Route route;
        protected final CompletableFuture<ResponsePayload> future;
        protected final Bucket bucket;
        protected int failedAttempts;
        
        protected void failed() {
            failedAttempts++;
        }
        
        protected boolean shouldRetry() {
            return failedAttempts < 3;
        }
    }
    
    protected interface Bucket {
        void queueRequest(@Nonnull QueuedRequest request);
        
        void failedRequest(@Nonnull QueuedRequest request, @Nonnull Throwable failureCause);
        
        void requestDone();
    }
}
