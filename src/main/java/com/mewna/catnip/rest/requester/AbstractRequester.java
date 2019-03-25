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

import com.google.common.collect.ImmutableList;
import com.mewna.catnip.Catnip;
import com.mewna.catnip.extension.Extension;
import com.mewna.catnip.extension.hook.CatnipHook;
import com.mewna.catnip.rest.MultipartBodyPublisher;
import com.mewna.catnip.rest.ResponseException;
import com.mewna.catnip.rest.ResponsePayload;
import com.mewna.catnip.rest.RestPayloadException;
import com.mewna.catnip.rest.Routes.Route;
import com.mewna.catnip.rest.ratelimit.RateLimiter;
import com.mewna.catnip.util.CatnipMeta;
import com.mewna.catnip.util.SafeVertxCompletableFuture;
import com.mewna.catnip.util.Utils;
import io.vertx.core.Context;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Builder;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import static io.vertx.core.http.HttpMethod.PUT;

@SuppressWarnings("WeakerAccess")
public abstract class AbstractRequester implements Requester {
    public static final BodyPublisher EMPTY_BODY = BodyPublishers.noBody();
    
    protected final RateLimiter rateLimiter;
    protected final Builder clientBuilder;
    protected Catnip catnip;
    private volatile HttpClient client;
    
    public AbstractRequester(@Nonnull final RateLimiter rateLimiter, @Nonnull final Builder clientBuilder) {
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
        // Capture stacktrace if possible
        final StackTraceElement[] stacktrace;
        if(catnip.captureRestStacktraces()) {
            stacktrace = Thread.currentThread().getStackTrace();
        } else {
            stacktrace = new StackTraceElement[0];
        }
        bucket.queueRequest(new QueuedRequest(r, r.route(), future, bucket, stacktrace));
        return future;
    }
    
    @Nonnull
    @CheckReturnValue
    protected abstract Bucket getBucket(@Nonnull Route route);
    
    @Nonnull
    @CheckReturnValue
    protected synchronized HttpClient client() {
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
            final MultipartBodyPublisher publisher = new MultipartBodyPublisher();
            //final HttpRequest.Builder builder = HttpRequest.newBuilder().headers("Content-Type","multipart/form-data;boundary="+publisher.getBoundary());
            
            final OutboundRequest r = request.request();
            for(int index = 0; index < r.buffers().size(); index++) {
                final ImmutablePair<String, Buffer> pair = r.buffers().get(index);
                publisher.addPart("file" + index, pair.left, pair.right, null);
            }
            if(r.object() != null) {
                for(final Extension extension : catnip.extensionManager().extensions()) {
                    for(final CatnipHook hook : extension.hooks()) {
                        r.object(hook.rawRestSendObjectHook(finalRoute, r.object()));
                    }
                }
                publisher.addPart("payload_json", r.object().encode());
            } else if(r.array() != null) {
                publisher.addPart("payload_json", r.array().encode());
            } else {
                publisher.addPart("payload_json", new JsonObject().putNull("content").putNull("embed").encode());
            }
            executeHttpRequest(finalRoute, publisher.build(), request, "multipart/form-data;boundary=" + publisher.getBoundary());
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
    
        executeHttpRequest(finalRoute, encoded == null ? null : BodyPublishers.ofString(encoded), request, "application/json");
    }
    
    protected void executeHttpRequest(@Nonnull final Route route, @Nullable final BodyPublisher body,
                                      @Nonnull final QueuedRequest request, @Nonnull final String mediaType) {
        final Context context = catnip.vertx().getOrCreateContext();
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(URI.create(API_HOST + API_BASE + route.baseRoute()))
                .method(route.method().name(), Objects.requireNonNullElseGet(body, BodyPublishers::noBody))
                .header("User-Agent", "DiscordBot (https://github.com/mewna/catnip, " + CatnipMeta.VERSION + ')')
                .header("Content-Type", mediaType);
        if(request.request().needsToken()) {
            requestBuilder.header("Authorization", "Bot " + catnip.token());
        }
        if(request.request().reason() != null) {
            requestBuilder.header(Requester.REASON_HEADER, Utils.encodeUTF8(request.request().reason()));
        }
        // Update request start time as soon as possible
        // See QueuedRequest docs for why we do this
        request.start = System.nanoTime();
        client().sendAsync(requestBuilder.build(), BodyHandlers.ofByteArray()).thenAcceptAsync(response -> {
            final int code = response.statusCode();
            final byte[] s = response.body();
            // See QueuedRequest docs for why we do this.
            final long requestEnd = System.nanoTime();
            context.runOnContext(__ -> handleResponse(route, code, "Catnip Internal: Unsupported by Java 11", requestEnd, s == null ? Buffer.buffer() : Buffer.buffer(s), response.headers(), request));
        }).exceptionally(t -> {
            request.bucket.failedRequest(request, t);
            return null;
        });
    }
    
    protected void handleResponse(@Nonnull final Route route, final int statusCode, @Nonnull final String statusMessage,
                                  final long requestEnd, final Buffer body, final HttpHeaders headers,
                                  @Nonnull final QueuedRequest request) {
        final Optional<String> dateHeader = headers.firstValue("Date");
        final long requestDuration = TimeUnit.NANOSECONDS.toMillis(requestEnd - request.start);
        final long timeDifference;
        if(dateHeader.isEmpty()) {
            timeDifference = requestDuration;
            catnip.logAdapter().trace("No date header, time difference = request duration = {}", timeDifference);
        } else {
            final long now = System.currentTimeMillis();
            final long date = OffsetDateTime.parse(dateHeader.get(), DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli();
            timeDifference = now - date + requestDuration;
            catnip.logAdapter().trace("Have date header, time difference = now - date + request duration = {}",
                    timeDifference);
        }
        if(statusCode == 429) {
            catnip.logAdapter().error("Hit 429! Route: {}, X-Ratelimit-Global: {}, X-Ratelimit-Limit: {}, X-Ratelimit-Reset: {}",
                    route.baseRoute(),
                    headers.firstValue("X-Ratelimit-Global").orElse("Unknown"),
                    headers.firstValue("X-Ratelimit-Limit").orElse("Unknown"),
                    headers.firstValue("X-Ratelimit-Reset").orElse("Unknown")
            );
            String retry = headers.firstValue("Retry-After").orElseGet(() -> body.toJsonObject().getValue("retry_after").toString());
            if(retry.isEmpty()) {
                retry = body.toJsonObject().getValue("retry_after").toString();
            }
            final long retryAfter = Long.parseLong(retry);
            if(Boolean.parseBoolean(headers.firstValue("X-RateLimit-Global").orElse("false"))) {
                rateLimiter.updateGlobalRateLimit(System.currentTimeMillis() + timeDifference + retryAfter);
            } else {
                updateBucket(route, headers,
                        System.currentTimeMillis() + timeDifference + retryAfter, timeDifference);
            }
            rateLimiter.requestExecution(route)
                    .thenRun(() -> executeRequest(request))
                    .exceptionally(e -> {
                        final Throwable throwable = new RuntimeException("REST error context");
                        throwable.setStackTrace(request.stacktrace());
                        request.future().completeExceptionally(e.initCause(throwable));
                        return null;
                    });
        } else {
            updateBucket(route, headers, -1, timeDifference);
            request.bucket().requestDone();
            
            ResponsePayload payload = new ResponsePayload(body);
            for(final Extension extension : catnip.extensionManager().extensions()) {
                for(final CatnipHook hook : extension.hooks()) {
                    payload = hook.rawRestReceiveDataHook(route, payload);
                }
            }
            // We got a 4xx, meaning there's errors. Fail the request with this and move on.
            if(statusCode >= 400) {
                final JsonObject response = payload.object();
                if(statusCode == 400 && response.getInteger("code", -1) > 1000) {
                    // 1000 was just the easiest number to check to skip over http error codes
                    // Discord error codes are all >=10000 afaik, so this should be safe?
                    
                    final Map<String, List<String>> failures = new HashMap<>();
                    response.forEach(e -> {
                        if(e.getValue() instanceof JsonArray) {
                            final JsonArray arr = (JsonArray) e.getValue();
                            final List<String> errorStrings = new ArrayList<>();
                            arr.stream().map(element -> (String) element).forEach(errorStrings::add);
                            failures.put(e.getKey(), errorStrings);
                        } else if(e.getValue() instanceof Integer) {
                            failures.put(e.getKey(), ImmutableList.of(String.valueOf(e.getValue())));
                        } else {
                            // If we don't know what it is, just stringify it and log a warning so that people can tell us
                            catnip.logAdapter().warn("Got unknown error response type: {} (Please report this!)",
                                    e.getValue().getClass().getName());
                            failures.put(e.getKey(), ImmutableList.of(String.valueOf(e.getValue())));
                        }
                    });
                    final Throwable throwable = new RuntimeException("REST error context");
                    throwable.setStackTrace(request.stacktrace());
                    request.future().completeExceptionally(new RestPayloadException(failures).initCause(throwable));
                } else {
                    final String message = response.getString("message", "No message.");
                    final int code = response.getInteger("code", -1);
                    final Throwable throwable = new RuntimeException("REST error context");
                    throwable.setStackTrace(request.stacktrace());
                    request.future().completeExceptionally(new ResponseException(route.toString(), statusCode,
                            statusMessage, code, message, response).initCause(throwable));
                }
            } else {
                request.future().complete(payload);
            }
        }
    }
    
    protected void updateBucket(@Nonnull final Route route, @Nonnull final HttpHeaders headers,
                                final long retryAfter, final long timeDifference) {
        final Optional<String> rateLimitReset = headers.firstValue("X-RateLimit-Reset");
        final Optional<String> rateLimitRemaining = headers.firstValue("X-RateLimit-Remaining");
        final Optional<String> rateLimitLimit = headers.firstValue("X-RateLimit-Limit");
    
        catnip.logAdapter().trace(
                "Updating headers for {} ({}): remaining = {}, limit = {}, reset = {}, retryAfter = {}, timeDifference = {}",
                route, route.ratelimitKey(), rateLimitRemaining, rateLimitLimit, rateLimitReset, retryAfter, timeDifference
        );
    
        if(retryAfter > 0) {
            rateLimiter.updateRemaining(route, 0);
            rateLimiter.updateReset(route, retryAfter);
        }
    
        if(route.method() == PUT && route.baseRoute().contains("/reactions/")) {
            rateLimiter.updateLimit(route, 1);
            rateLimiter.updateReset(route, System.currentTimeMillis() + timeDifference + 250);
        } else {
            rateLimitReset.ifPresent(s -> rateLimiter.updateReset(route, Long.parseLong(s) * 1000 + timeDifference));
            rateLimitLimit.ifPresent(s -> rateLimiter.updateLimit(route, Integer.parseInt(s)));
        }
    
        rateLimitRemaining.ifPresent(s -> rateLimiter.updateRemaining(route, Integer.parseInt(s)));
        rateLimiter.updateDone(route);
    }
    protected interface Bucket {
        void queueRequest(@Nonnull QueuedRequest request);
        
        void failedRequest(@Nonnull QueuedRequest request, @Nonnull Throwable failureCause);
        
        void requestDone();
    }
    
    @Getter
    @Accessors(fluent = true)
    @RequiredArgsConstructor
    protected static class QueuedRequest {
        protected final OutboundRequest request;
        protected final Route route;
        protected final CompletableFuture<ResponsePayload> future;
        protected final Bucket bucket;
        protected final StackTraceElement[] stacktrace;
        protected int failedAttempts;
        private long start;
        
        protected void failed() {
            failedAttempts++;
        }
        
        protected boolean shouldRetry() {
            return failedAttempts < 3;
        }
    }
}
