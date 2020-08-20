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

import com.grack.nanojson.*;
import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.impl.lifecycle.RestRatelimitHitImpl;
import com.mewna.catnip.extension.Extension;
import com.mewna.catnip.extension.hook.CatnipHook;
import com.mewna.catnip.rest.MultipartBodyPublisher;
import com.mewna.catnip.rest.ResponseException;
import com.mewna.catnip.rest.ResponsePayload;
import com.mewna.catnip.rest.RestPayloadException;
import com.mewna.catnip.rest.Routes.Route;
import com.mewna.catnip.rest.ratelimit.RateLimiter;
import com.mewna.catnip.shard.LifecycleEvent.Raw;
import com.mewna.catnip.util.CatnipMeta;
import com.mewna.catnip.util.Utils;
import com.mewna.catnip.util.rx.RxHelpers;
import io.reactivex.rxjava3.core.Observable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.net.URI;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.mewna.catnip.rest.Routes.HttpMethod.GET;

@SuppressWarnings("WeakerAccess")
public abstract class AbstractRequester implements Requester {
    private static final StackWalker STACK_WALKER = StackWalker.getInstance(Set.of(Option.RETAIN_CLASS_REFERENCE));
    
    protected final RateLimiter rateLimiter;
    protected Catnip catnip;
    
    public AbstractRequester(@Nonnull final RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }
    
    @Override
    public void catnip(@Nonnull final Catnip catnip) {
        this.catnip = catnip;
        rateLimiter.catnip(catnip);
    }
    
    @Nonnull
    @Override
    public Observable<ResponsePayload> queue(@Nonnull final OutboundRequest r) {
        final CompletableFuture<ResponsePayload> future = new CompletableFuture<>();
        final Bucket bucket = getBucket(r.route());
        // Capture stacktrace if possible
        final StackTraceElement[] stacktrace;
        if(catnip.options().captureRestStacktraces()) {
            stacktrace = STACK_WALKER.walk(stream -> stream
                    .map(StackFrame::toStackTraceElement)
                    .toArray(StackTraceElement[]::new));
        } else {
            stacktrace = new StackTraceElement[0];
        }
        bucket.queueRequest(new QueuedRequest(r, r.route(), future, bucket, stacktrace));
        return RxHelpers.futureToObservable(future)
                .subscribeOn(catnip.rxScheduler())
                .observeOn(catnip.rxScheduler());
    }
    
    @Nonnull
    @CheckReturnValue
    protected abstract Bucket getBucket(@Nonnull Route route);
    
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
            final OutboundRequest r = request.request();
            for(int index = 0; index < r.buffers().size(); index++) {
                final ImmutablePair<String, byte[]> pair = r.buffers().get(index);
                publisher.addPart("file" + index, pair.left, pair.right);
            }
            if(r.object() != null) {
                for(final Extension extension : catnip.extensionManager().extensions()) {
                    for(final CatnipHook hook : extension.hooks()) {
                        r.object(hook.rawRestSendObjectHook(finalRoute, r.object()));
                    }
                }
                publisher.addPart("payload_json", JsonWriter.string(r.object()));
            } else if(r.array() != null) {
                publisher.addPart("payload_json", JsonWriter.string(r.array()));
            } else {
                
                publisher.addPart("payload_json",
                        JsonWriter.string()
                                .object()
                                .nul("content")
                                .nul("embed")
                                .end()
                                .done());
            }
            executeHttpRequest(finalRoute, publisher.build(), request, "multipart/form-data;boundary=" + publisher.boundary());
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
            encoded = JsonWriter.string(r.object());
        } else if(r.array() != null) {
            encoded = JsonWriter.string(r.array());
        } else {
            encoded = null;
        }
        executeHttpRequest(finalRoute, encoded == null ? BodyPublishers.noBody() : BodyPublishers.ofString(encoded), request, "application/json");
    }
    
    protected void executeHttpRequest(@Nonnull final Route route, @Nullable final BodyPublisher body,
                                      @Nonnull final QueuedRequest request, @Nonnull final String mediaType) {
        final Builder builder;
        final String apiHostVersion = catnip.options().apiHost() + "/api/v" + catnip.options().apiVersion();
        
        if(route.method() == GET) {
            // No body
            builder = HttpRequest.newBuilder(URI.create(apiHostVersion + route.baseRoute())).GET();
        } else {
            final var fakeBody = request.request.emptyBody();
            builder = HttpRequest.newBuilder(URI.create(apiHostVersion + route.baseRoute()))
                    .setHeader("Content-Type", mediaType)
                    .method(route.method().name(), fakeBody ? BodyPublishers.ofString(" ") : body);
            if(fakeBody) {
                // If we don't have a body, then the body param is null, which
                // seems to not set a Content-Length. This explicitly tries to set
                // up a request shaped in a way that makes Discord not complain.
                catnip.logAdapter().trace("Set fake body due to lack of body.");
            }
        }
        
        // Required by Discord
        builder.setHeader("User-Agent", "DiscordBot (https://github.com/mewna/catnip, " + CatnipMeta.VERSION + ')');
        // Request more precise ratelimit headers for better timing
        // NOTE: THIS SHOULD NOT BE CONFIGURABLE BY THE END USER
        // This is pretty important for getting timing of things like reaction
        // routes correct, so there's no use for the end-user messing around
        // with this.
        // If they REALLY want it, they can write their own requester.
        builder.setHeader("X-RateLimit-Precision", "millisecond");
        
        if(request.request().needsToken()) {
            builder.setHeader("Authorization", "Bot " + catnip.options().token());
        }
        if(request.request().reason() != null) {
            catnip.logAdapter().trace("Adding reason header due to specific needs.");
            builder.header(Requester.REASON_HEADER, Utils.encodeUTF8(request.request().reason()).replace('+', ' '));
        }
        
        // Update request start time as soon as possible
        // See QueuedRequest docs for why we do this
        request.start = System.nanoTime();
        catnip.options().httpClient().sendAsync(builder.build(), BodyHandlers.ofString())
                .thenAccept(res -> {
                    final int code = res.statusCode();
                    final String message = "Unavailable to due Java's HTTP client.";
                    final long requestEnd = System.nanoTime();
                    
                    catnip.rxScheduler().scheduleDirect(() ->
                            handleResponse(route, code, message, requestEnd, res.body(), res.headers(), request));
                })
                .exceptionally(e -> {
                    request.bucket.failedRequest(request, e);
                    return null;
                });
    }
    
    protected void handleResponse(@Nonnull final Route route, final int statusCode,
                                  @SuppressWarnings("SameParameterValue") @Nonnull final String statusMessage,
                                  final long requestEnd, final String body, final HttpHeaders headers,
                                  @Nonnull final QueuedRequest request) {
        final String dateHeader = headers.firstValue("Date").orElse(null);
        final long requestDuration = TimeUnit.NANOSECONDS.toMillis(requestEnd - request.start);
        final long timeDifference;
        if(dateHeader == null || route.requiresMsPrecision()) {
            timeDifference = requestDuration;
            catnip.logAdapter().trace("No date header, time difference = request duration = {}", timeDifference);
        } else {
            final long now = System.currentTimeMillis();
            final long date = OffsetDateTime.parse(dateHeader, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant()
                    .toEpochMilli();
            timeDifference = now - date + requestDuration;
            catnip.logAdapter().trace("Have date header, time difference = now - date + request duration = " +
                            "{} - {} + {} = {}",
                    now, date, requestDuration, timeDifference);
        }
        if(statusCode == 429) {
            if(catnip.options().logLifecycleEvents()) {
                catnip.logAdapter().error(
                        "Hit 429! Route: {}, X-Ratelimit-Global: {}, X-Ratelimit-Limit: {}, X-Ratelimit-Reset: {}",
                        route.baseRoute(),
                        headers.firstValue("X-Ratelimit-Global").orElse(null),
                        headers.firstValue("X-Ratelimit-Limit").orElse(null),
                        headers.firstValue("X-Ratelimit-Reset").orElse(null)
                );
            }
            catnip.dispatchManager().dispatchEvent(Raw.REST_RATELIMIT_HIT,
                    new RestRatelimitHitImpl(catnip, route.baseRoute(),
                            Boolean.parseBoolean(headers.firstValue("X-RateLimit-Global").orElse(null))));
            
            String retry = headers.firstValue("Retry-After").orElse(null);
            if(retry == null || retry.isEmpty()) {
                try {
                    retry = JsonParser.object().from(body).get("retry_after").toString();
                } catch(final JsonParserException e) {
                    throw new IllegalStateException(e);
                }
            }
            final long retryAfter = Long.parseLong(retry);
            if(Boolean.parseBoolean(headers.firstValue("X-RateLimit-Global").orElse(null))) {
                catnip.logAdapter().trace("Updating global bucket due to ratelimit.");
                rateLimiter.updateGlobalRateLimit(System.currentTimeMillis() + timeDifference + retryAfter);
            } else {
                catnip.logAdapter().trace("Updating bucket headers due to ratelimit.");
                updateBucket(route, headers,
                        System.currentTimeMillis() + timeDifference + retryAfter, timeDifference);
            }
            // It should get autodisposed anyway, so we don't need to worry
            // about handling the method result
            //noinspection ResultOfMethodCallIgnored
            rateLimiter.requestExecution(route)
                    .subscribe(() -> executeRequest(request),
                            e -> {
                                final Throwable throwable = new RuntimeException("REST error context");
                                throwable.setStackTrace(request.stacktrace());
                                request.future().completeExceptionally(e.initCause(throwable));
                            });
        } else {
            catnip.logAdapter().trace("Updating bucket headers from successful completion with code {}.", statusCode);
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
                catnip.logAdapter().trace("Request received an error code ({} >= 400), processing...", statusCode);
                if(payload.string() != null && payload.string().startsWith("{")) {
                    // If the payload HAS a body, AND it looks like a JSON object, try to parse it for info
                    final JsonObject response = payload.object();
                    if(statusCode == 400 && response.getInt("code", -1) > 1000) {
                        // 1000 was just the easiest number to check to skip over http error codes
                        // Discord error codes are all >=10000 afaik, so this should be safe?
                        catnip.logAdapter().trace("Status code 400 + JSON code, creating RestPayloadException...");
                        final Map<String, List<String>> failures = new HashMap<>();
                        response.forEach((key, value) -> {
                            if(value instanceof JsonArray) {
                                final JsonArray arr = (JsonArray) value;
                                final List<String> errorStrings = new ArrayList<>();
                                arr.stream().map(element -> (String) element).forEach(errorStrings::add);
                                failures.put(key, errorStrings);
                            } else if(value instanceof Integer) {
                                failures.put(key, List.of(String.valueOf(value)));
                            } else if(value instanceof String) {
                                failures.put(key, List.of((String) value));
                            } else {
                                // If we don't know what it is, just stringify it and log a warning so that people can tell us
                                catnip.logAdapter().warn("Got unknown error response type: {} (Please report this!)",
                                        value.getClass().getName());
                                failures.put(key, List.of(String.valueOf(value)));
                            }
                        });
                        final Throwable throwable = new RuntimeException("REST error context");
                        throwable.setStackTrace(request.stacktrace());
                        request.future().completeExceptionally(new RestPayloadException(failures).initCause(throwable));
                    } else {
                        catnip.logAdapter().trace("Status code != 400, creating ResponseException...");
                        final String message = response.getString("message", "No message.");
                        final int code = response.getInt("code", -1);
                        final Throwable throwable = new RuntimeException("REST error context");
                        throwable.setStackTrace(request.stacktrace());
                        request.future().completeExceptionally(new ResponseException(route.toString(), statusCode,
                                statusMessage, code, message, response).initCause(throwable));
                    }
                } else {
                    catnip.logAdapter().trace("Status code != 400 and no JSON body, creating ResponseException...");
                    final Throwable throwable = new RuntimeException("REST error context");
                    throwable.setStackTrace(request.stacktrace());
                    request.future().completeExceptionally(new ResponseException(route.toString(), statusCode,
                            statusMessage, -1, "No message.", null).initCause(throwable));
                }
            } else {
                catnip.logAdapter().trace("Successfully completed request future.");
                request.future().complete(payload);
            }
        }
    }
    
    protected void updateBucket(@Nonnull final Route route, @Nonnull final HttpHeaders headers, final long retryAfter,
                                final long timeDifference) {
        final Optional<Long> rateLimitReset = headers.firstValue("X-RateLimit-Reset")
                .map(s -> Long.parseLong(s.replace(".", "")));
        final OptionalLong rateLimitRemaining = headers.firstValueAsLong("X-RateLimit-Remaining");
        final OptionalLong rateLimitLimit = headers.firstValueAsLong("X-RateLimit-Limit");
        final Optional<Long> rateLimitResetAfter = headers.firstValue("X-RateLimit-Reset-After")
                .map(s -> Long.parseLong(s.replace(".", "")));
        
        catnip.logAdapter().trace(
                "Updating headers for {} ({}): remaining = {}, limit = {}, reset = {}, retryAfter = {}, timeDifference = {}",
                route, route.ratelimitKey(), rateLimitRemaining.orElse(-1L), rateLimitLimit.orElse(-1L),
                rateLimitReset.orElse(-1L), retryAfter, timeDifference
        );
        
        if(retryAfter > 0) {
            rateLimiter.updateRemaining(route, 0);
            if(catnip.options().restRatelimitsWithoutClockSync() && rateLimitResetAfter.isPresent()) {
                rateLimiter.updateReset(route, System.currentTimeMillis() + timeDifference
                        + rateLimitResetAfter.get());
            } else {
                rateLimiter.updateReset(route, retryAfter);
            }
        }
        
        rateLimitReset.ifPresent(aLong -> rateLimiter.updateReset(route, aLong + timeDifference));
        rateLimitLimit.ifPresent(aLong -> rateLimiter.updateLimit(route, Math.toIntExact(aLong)));
        rateLimitRemaining.ifPresent(aLong -> rateLimiter.updateRemaining(route, Math.toIntExact(aLong)));
        
        rateLimiter.updateDone(route);
    }
    
    public interface Bucket {
        void queueRequest(@Nonnull QueuedRequest request);
        
        void failedRequest(@Nonnull QueuedRequest request, @Nonnull Throwable failureCause);
        
        void requestDone();
    }
    
    @Getter
    @Accessors(fluent = true)
    @RequiredArgsConstructor
    public static class QueuedRequest {
        protected final OutboundRequest request;
        protected final Route route;
        protected final CompletableFuture<ResponsePayload> future;
        protected final Bucket bucket;
        protected final StackTraceElement[] stacktrace;
        protected int failedAttempts;
        private long start;
        
        public void failed() {
            failedAttempts++;
        }
        
        public boolean shouldRetry() {
            return failedAttempts < 3;
        }
    }
}
