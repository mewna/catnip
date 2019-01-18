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

package com.mewna.catnip.rest;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.extension.Extension;
import com.mewna.catnip.extension.hook.CatnipHook;
import com.mewna.catnip.rest.Routes.Route;
import com.mewna.catnip.rest.bucket.BucketBackend;
import com.mewna.catnip.util.CatnipMeta;
import com.mewna.catnip.util.SafeVertxCompletableFuture;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.impl.NoStackTraceThrowable;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.*;
import lombok.experimental.Accessors;
import okhttp3.*;
import okhttp3.internal.http.HttpMethod;
import okio.BufferedSink;
import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;

import static io.vertx.core.http.HttpMethod.GET;

/**
 * TODO: Refactor this out into interface and implementation to allow plugging in other impls
 *
 * @author amy
 * @since 8/31/18.
 */
public class RestRequester {
    /**
     * TODO: Allow injecting other URLs for eg. mocks
     */
    public static final String API_HOST = "https://discordapp.com";
    private static final int API_VERSION = 6;
    public static final String API_BASE = "/api/v" + API_VERSION;
    private static final RequestBody EMPTY_BODY = RequestBody.create(null, new byte[0]);
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final Catnip catnip;
    private final OkHttpClient _http;
    private final Collection<Bucket> submittedBuckets = new ConcurrentHashSet<>();
    private final BucketBackend bucketBackend;
    
    public RestRequester(final Catnip catnip, final BucketBackend bucketBackend, final OkHttpClient _http) {
        this.catnip = catnip;
        this.bucketBackend = bucketBackend;
        this._http = _http;
    }
    
    public CompletionStage<ResponsePayload> queue(final OutboundRequest r) {
        final Future<ResponsePayload> future = Future.future();
        getBucket(r.route.baseRoute()).queue(future, r);
        return SafeVertxCompletableFuture.from(catnip, future);
    }
    
    private Bucket getBucket(final String key) {
        return buckets.computeIfAbsent(key, Bucket::new);
    }
    
    private void handleResponse(final OutboundRequest r, final Bucket bucket, final int statusCode, final String statusMessage,
                                final Buffer body, final MultiMap headers, final boolean succeeded,
                                final Throwable failureCause) {
        final long now = System.currentTimeMillis();
        
        //noinspection CodeBlock2Expr
        bucket.lastRequest().thenAccept(lastRequest -> {
            bucket.latency(now - lastRequest).thenAccept(__ -> {
                if(succeeded) {
                    catnip.logAdapter().debug("Completed request {}", r);
                    if(statusCode < 200 || statusCode > 299) {
                        if(statusCode == 401) {
                            catnip.logAdapter().warn("Token probably invalid: got {} '{}', route: {} {}, request {}, body {}",
                                    statusCode, statusMessage, r.route.method().name(), r.route.baseRoute(), r, body.toString());
                            r.future.fail("Invalid token");
                            return;
                        } else if(statusCode != 429) {
                            catnip.logAdapter().warn("Got unexpected HTTP status: {} '{}', route: {} {}, request {}, body {}", statusCode,
                                    statusMessage, r.route.method().name(), r.route.baseRoute(), r, body.toString());
                        }
                    }
                    boolean ratelimited = false;
                    final boolean hasMemeReactionRatelimits = r.route.method() != GET
                            && r.route.baseRoute().contains("/reactions/");
                    if(statusCode == 429) {
                        ratelimited = true;
                        // Reactions are a HUGE meme
                        // We hit *roughly* one 429 / reaction if we're adding many
                        // reactions. I *think* this is ok?
                        // TODO: Warn if we hit the meme ratelimit a lot
                        if(!hasMemeReactionRatelimits) {
                            catnip.logAdapter().error("Hit 429! Route: {}, X-Ratelimit-Global: {}, X-Ratelimit-Limit: {}, X-Ratelimit-Reset: {}",
                                    r.route.baseRoute(),
                                    headers.get("X-Ratelimit-Global"),
                                    headers.get("X-Ratelimit-Limit"),
                                    headers.get("X-Ratelimit-Reset")
                            );
                        }
                    }
                    // This is a final 1-element array so we can mutate it inside of callbacks
                    // *sigh*
                    final ResponsePayload payload = new ResponsePayload(body);
                    if(headers.contains("X-Ratelimit-Global")) {
                        handleGlobalRatelimitPost(headers, bucket, r);
                    } else if(ratelimited) {
                        handleRouteRatelimitPost(headers, bucket, r, hasMemeReactionRatelimits);
                    } else {
                        finishRequestPost(headers, bucket, r, payload, statusCode);
                    }
                } else {
                    failRequestPost(bucket, r, failureCause);
                }
            });
        });
    }
    
    private void handleGlobalRatelimitPost(final MultiMap headers, final Bucket bucket, final OutboundRequest r) {
        // We hit a global ratelimit, update
        final Bucket global = getBucket("GLOBAL");
        final long retry = Long.parseLong(headers.get("Retry-After"));
        global.remaining(0).thenAcceptBoth(global.limit(1), (___, ____) -> {
            // 500ms buffer for safety; we don't use a specific bucket's
            // latency because the ratelimit is global, not per-bucket.
            final long globalReset = System.currentTimeMillis() + retry + 500L;
            // CatnipImpl.vertx().setTimer(globalReset, __ -> global.resetBucket());
            global.reset(TimeUnit.MILLISECONDS.toSeconds(globalReset)).thenAccept(_____ -> bucket.retry(r));
        });
    }
    
    private void handleRouteRatelimitPost(final MultiMap headers, final Bucket bucket, final OutboundRequest r,
                                          final boolean hasMemeReactionRatelimits) {
        // We got ratelimited, back the fuck off
        bucket.updateFromHeaders(headers).thenAccept(___ -> {
            if(hasMemeReactionRatelimits) {
                // Ratelimits are a meme with reactions
                catnip.vertx().setTimer(250L, ____ -> bucket.retry(r));
            } else {
                // Try and compute from headers
                bucket.retry(r);
            }
        });
    }
    
    private void finishRequestPost(final MultiMap headers, final Bucket bucket, final OutboundRequest r,
                                   final ResponsePayload finalPayload, final int statusCode) {
        final ResponsePayload[] payload = {finalPayload};
        // We're good, run it through hooks and complete the future.
        bucket.updateFromHeaders(headers).thenAccept(___ -> {
            for(final Extension extension : catnip.extensionManager().extensions()) {
                for(final CatnipHook hook : extension.hooks()) {
                    payload[0] = hook.rawRestReceiveDataHook(r.route, payload[0]);
                }
            }
            
            // We got a 400, meaning there's errors. Fail the request with this and move on.
            if(statusCode == 400) {
                final RestPayloadException exception = new RestPayloadException();
                final JsonObject errors = payload[0].object();
                errors.forEach(e -> {
                    //noinspection unchecked
                    exception.addFailure(e.getKey(), (List<String>) e.getValue());
                });
                r.future.fail(exception);
                bucket.finishRequest();
                bucket.submit();
            } else {
                r.future.complete(payload[0]);
                bucket.finishRequest();
                bucket.submit();
            }
        });
    }
    
    private void failRequestPost(final Bucket bucket, final OutboundRequest r, final Throwable failureCause) {
        // Fail request, resubmit to queue if failed less than 3 times, complete with error otherwise.
        r.failed();
        if(r.failedAttempts() >= 3) {
            catnip.logAdapter().debug("Request {} failed, giving up!", r);
            r.future.fail(failureCause);
            bucket.finishRequest();
            bucket.submit();
        } else {
            catnip.logAdapter().debug("Request {} failed, retrying ({} / 3)!", r, r.failedAttempts() + 1);
            bucket.retry(r);
        }
    }
    
    private void request(final OutboundRequest r) {
        Route route = r.route;
        final Route bucketRoute = route.copy();
        // Compile route for usage
        for(final Entry<String, String> stringStringEntry : r.params.entrySet()) {
            route = route.compile(stringStringEntry.getKey(), stringStringEntry.getValue());
        }
        
        final Bucket bucket = getBucket(bucketRoute.baseRoute());
        final Bucket global = getBucket("GLOBAL");
        
        final Route finalRoute = route;
        
        global.remaining().thenAcceptBoth(global.reset(), (globalRemaining, globalReset) -> {
            final CompletableFuture<Void> resetFuture;
            if(globalRemaining == 0 && globalReset < System.currentTimeMillis()) {
                resetFuture = global.resetBucket();
            } else {
                resetFuture = SafeVertxCompletableFuture.completedFuture(null);
            }
            
            resetFuture.thenAccept(__ -> {
                if(globalRemaining > 0) {
                    // Can do some requests
                    bucket.remaining().thenAcceptBoth(bucket.reset(), (remaining, reset) -> {
                        final CompletableFuture<Void> future;
                        if(remaining == 0 && reset < System.currentTimeMillis()) {
                            future = bucket.resetBucket();
                        } else {
                            future = SafeVertxCompletableFuture.completedFuture(null);
                        }
                        future.thenAccept(___ -> {
                            // add/remove/remove_all routes for reactions have a meme 1/0.25s
                            // ratelimit, which isn't accurately reflected in the responses
                            // from the API. Instead, we just try anyway and re-queue if we get
                            // a 429.
                            // Assuming you're messing with N reactions, where N > 1, you'll
                            // run into ~(N-1) 429s. I *think* this is okay?
                            final boolean hasMemeReactionRatelimits = finalRoute.method() != GET
                                    && finalRoute.baseRoute().contains("/reactions/");
                            if(remaining > 0 || hasMemeReactionRatelimits) {
                                // Do request and update bucket
                                catnip.logAdapter().debug("Making request: {} {} (bucket {})", finalRoute.method().name(),
                                        API_BASE + finalRoute.baseRoute(), bucket.route);
                                // v.x is dumb and doesn't support multipart, so we use okhttp instead /shrug
                                if(r.buffers != null) {
                                    handleRouteBufferBodySend(bucket, finalRoute, r);
                                } else {
                                    handleRouteJsonBodySend(bucket, finalRoute, r);
                                }
                            } else {
                                handleRouteRatelimitPre(bucket, bucketRoute, finalRoute, r, reset);
                            }
                        });
                    });
                } else {
                    handleGlobalRatelimitPre(bucket, global, bucketRoute, finalRoute, r, globalReset);
                }
            });
        });
    }
    
    private void handleRouteBufferBodySend(final Bucket bucket, final Route finalRoute, final OutboundRequest r) {
        try {
            @SuppressWarnings("UnnecessarilyQualifiedInnerClassAccess")
            final MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        
            for(int index = 0; index < r.buffers.size(); index++) {
                final ImmutablePair<String, Buffer> pair = r.buffers.get(index);
                builder.addFormDataPart("file" + index, pair.left, new MultipartRequestBody(pair.right));
            }
            if(r.object != null) {
                for(final Extension extension : catnip.extensionManager().extensions()) {
                    for(final CatnipHook hook : extension.hooks()) {
                        r.object = hook.rawRestSendObjectHook(finalRoute, r.object);
                    }
                }
                builder.addFormDataPart("payload_json", r.object.encode());
            } else if(r.array != null) {
                builder.addFormDataPart("payload_json", r.array.encode());
            } else {
                builder.addFormDataPart("payload_json", new JsonObject()
                        .putNull("content")
                        .putNull("embed").encode());
            }
        
            executeHttpRequest(r, finalRoute, bucket, builder.build());
        } catch(final Exception e) {
            catnip.logAdapter().error("Failed to send multipart request", e);
        }
    }
    
    private void handleRouteJsonBodySend(final Bucket bucket, final Route finalRoute, final OutboundRequest r) {
        final String encoded;
        if(r.object != null) {
            for(final Extension extension : catnip.extensionManager().extensions()) {
                for(final CatnipHook hook : extension.hooks()) {
                    r.object = hook.rawRestSendObjectHook(finalRoute, r.object);
                }
            }
            encoded = r.object.encode();
        } else if(r.array != null) {
            encoded = r.array.encode();
        } else {
            encoded = null;
        }
        RequestBody body = null;
        if(encoded != null) {
            body = RequestBody.create(MediaType.parse("application/json"), encoded);
        } else if(HttpMethod.requiresRequestBody(r.route.method().name().toUpperCase())) {
            body = EMPTY_BODY;
        }
        executeHttpRequest(r, finalRoute, bucket, body);
    }
    
    private void handleRouteRatelimitPre(final Bucket bucket, final Route bucketRoute, final Route finalRoute,
                                         final OutboundRequest r, final long reset) {
        // Route ratelimited, retry later
        bucket.latency().thenAccept(latency -> {
            final long wait = reset - System.currentTimeMillis() + latency;
            catnip.logAdapter().debug("Hit ratelimit on bucket {} for route {}, waiting {}ms and retrying...",
                    bucketRoute.baseRoute(), finalRoute.baseRoute(), wait);
            catnip.vertx().setTimer(wait, __ -> bucket.resetBucket().thenAccept(___ -> bucket.retry(r)));
        });
    }
    
    private void handleGlobalRatelimitPre(final Bucket bucket, final Bucket global, final Route bucketRoute,
                                          final Route finalRoute, final OutboundRequest r, final long globalReset) {
        // Global rl, retry later
        bucket.latency().thenAccept(latency -> {
            final long wait = globalReset - System.currentTimeMillis() + latency;
            catnip.logAdapter().debug("Hit ratelimit on bucket {} for route {}, waiting {}ms and retrying...",
                    bucketRoute.baseRoute(), finalRoute.baseRoute(), wait);
            catnip.logAdapter().warn("Hit GLOBAL ratelimit, waiting {}ms and retrying...", wait);
            catnip.vertx().setTimer(wait, ___ -> global.resetBucket().thenAccept(____ -> bucket.retry(r)));
        });
    }
    
    private void executeHttpRequest(final OutboundRequest r, final Route route, final Bucket bucket, final RequestBody body) {
        bucket.lastRequest(System.currentTimeMillis()).thenAccept(__ -> {
            final Context context = catnip.vertx().getOrCreateContext();
            final Request.Builder requestBuilder = new Request.Builder().url(API_HOST + API_BASE + route.baseRoute())
                    .method(route.method().name(), body)
                    .header("User-Agent", "DiscordBot (https://github.com/mewna/catnip, " + CatnipMeta.VERSION + ')');
            if(r.needsToken()) {
                requestBuilder.header("Authorization", "Bot " + catnip.token());
            }
            _http.newCall(requestBuilder.build()).enqueue(new Callback() {
                @Override
                public void onFailure(@Nonnull final Call call, @Nonnull final IOException e) {
                    handleResponse(r, bucket, -1, "", null, null,
                            false, e);
                }
                
                @Override
                public void onResponse(@Nonnull final Call call, @Nonnull final Response resp) throws IOException {
                    //ensure we close it no matter what
                    try(final Response response = resp) {
                        final int code = response.code();
                        final String message = response.message();
                        if(response.body() == null) {
                            context.runOnContext(__ ->
                                    handleResponse(r, bucket, code, message, null, null,
                                            false, new NoStackTraceThrowable("body == null")));
                        } else {
                            final byte[] bodyBytes = response.body().bytes();
                            
                            final MultiMap headers = MultiMap.caseInsensitiveMultiMap();
                            response.headers().toMultimap().forEach(headers::add);
                            context.runOnContext(__ ->
                                    handleResponse(r, bucket, code, message, Buffer.buffer(bodyBytes),
                                            headers, true, null));
                        }
                    }
                }
            });
        });
    }
    
    @Getter
    @Accessors(fluent = true)
    @SuppressWarnings("unused")
    public static final class OutboundRequest {
        private Route route;
        private Map<String, String> params;
        private JsonObject object;
        private JsonArray array;
        @Setter
        private boolean needsToken = true;
        
        @Setter
        private List<ImmutablePair<String, Buffer>> buffers;
        
        @Setter
        private Future<ResponsePayload> future;
        private int failedAttempts;
        @Setter
        private boolean emptyBody;
        
        public OutboundRequest() {
        }
        
        public OutboundRequest(final Route route, final Map<String, String> params) {
            this.route = route;
            this.params = params;
        }
        
        public OutboundRequest(final Route route, final Map<String, String> params, final JsonObject object) {
            this(route, params);
            this.object = object;
        }
        
        public OutboundRequest(final Route route, final Map<String, String> params, final JsonArray array) {
            this(route, params);
            this.array = array;
        }
        
        void failed() {
            failedAttempts++;
        }
        
        int failedAttempts() {
            return failedAttempts;
        }
        
        @Override
        public String toString() {
            return String.format("OutboundRequest (%s, %s, object=%s, array=%s, buffers=%s, failures=%s)",
                    route, params, object != null, array != null, buffers != null && !buffers.isEmpty(), failedAttempts);
        }
    }
    
    @RequiredArgsConstructor
    private final class MultipartRequestBody extends RequestBody {
        private final Buffer buffer;
        
        private final MediaType contentType = MediaType.parse("application/octet-stream; charset=utf-8");
        
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
    
    @Data
    @AllArgsConstructor
    private final class Bucket {
        /**
         * Name of the route. Ratelimits are per-route-per-major-param, so ex.
         * {@code /channels/1} and {@code /channels/2} would have different
         * buckets.
         */
        private final String route;
        private final Deque<OutboundRequest> queue = new ConcurrentLinkedDeque<>();
        
        @CheckReturnValue
        CompletableFuture<Long> remaining() {
            return bucketBackend.remaining(route);
        }
        
        @CheckReturnValue
        @SuppressWarnings("SameParameterValue")
        CompletableFuture<Void> remaining(final long l) {
            return bucketBackend.remaining(route, l).thenApply(__ -> null);
        }
        
        @CheckReturnValue
        CompletableFuture<Long> reset() {
            return bucketBackend.reset(route);
        }
        
        @CheckReturnValue
        CompletableFuture<Void> reset(final long l) {
            return bucketBackend.reset(route, l).thenApply(__ -> null);
        }
        
        @CheckReturnValue
        @SuppressWarnings("unused")
        CompletableFuture<Long> limit() {
            return bucketBackend.limit(route);
        }
        
        @CheckReturnValue
        @SuppressWarnings("SameParameterValue")
        CompletableFuture<Void> limit(final long l) {
            return bucketBackend.limit(route, l).thenApply(__ -> null);
        }
        
        @CheckReturnValue
        CompletableFuture<Long> latency() {
            return bucketBackend.latency(route);
        }
        
        @CheckReturnValue
        CompletableFuture<Void> latency(final long l) {
            return bucketBackend.latency(route, l).thenApply(__ -> null);
        }
        
        @CheckReturnValue
        CompletableFuture<Long> lastRequest() {
            return bucketBackend.lastRequest(route);
        }
        
        @CheckReturnValue
        CompletableFuture<Void> lastRequest(final long l) {
            return bucketBackend.lastRequest(route, l).thenApply(__ -> null);
        }
        
        @CheckReturnValue
        CompletableFuture<Void> resetBucket() {
            final Future<Void> future = Future.future();
            bucketBackend.limit(route).thenAccept(limit -> {
                bucketBackend.remaining(route, limit);
                bucketBackend.reset(route, -1L);
                future.complete(null);
            });
            
            return SafeVertxCompletableFuture.from(catnip, future);
        }
        
        @CheckReturnValue
        CompletableFuture<Void> updateFromHeaders(final MultiMap headers) {
            if(!(headers.contains("X-Ratelimit-Limit") && headers.contains("X-Ratelimit-Remaining") && headers.contains("X-Ratelimit-Reset"))) {
                return SafeVertxCompletableFuture.completedFuture(null);
            }
            final List<CompletableFuture<?>> futures = new ArrayList<>();
            futures.add(bucketBackend.limit(route, Integer.parseInt(headers.get("X-Ratelimit-Limit"))));
            futures.add(bucketBackend.remaining(route, Integer.parseInt(headers.get("X-Ratelimit-Remaining"))));
            futures.add(bucketBackend.reset(route, TimeUnit.SECONDS.toMillis(Integer.parseInt(headers.get("X-Ratelimit-Reset")))));
            return SafeVertxCompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        }
        
        void queue(final Future<ResponsePayload> future, final OutboundRequest request) {
            request.future(future);
            queue.addLast(request);
            submit();
        }
        
        void requeue(final OutboundRequest request) {
            queue.addFirst(request);
        }
        
        void submit() {
            if(!submittedBuckets.contains(this)) {
                submittedBuckets.add(this);
                if(!queue.isEmpty()) {
                    final OutboundRequest r = queue.removeFirst();
                    request(r);
                } else {
                    // If bucket has nothing queued, we can just immediately finish the submit
                    finishRequest();
                }
            }
        }
        
        void finishRequest() {
            submittedBuckets.remove(this);
        }
        
        void retry(final OutboundRequest request) {
            requeue(request);
            finishRequest();
            submit();
        }
    }
}
