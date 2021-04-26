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

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import com.grack.nanojson.JsonWriter;
import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.impl.lifecycle.RestRatelimitHitImpl;
import com.mewna.catnip.extension.Extension;
import com.mewna.catnip.extension.hook.CatnipHook;
import com.mewna.catnip.rest.*;
import com.mewna.catnip.rest.Routes.HttpMethod;
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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.mewna.catnip.rest.Routes.HttpMethod.DELETE;
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
        final String apiHostVersion = catnip.options().apiHost() + "/api/v" + catnip.options().apiVersion();
        final Builder builder = HttpRequest.newBuilder(URI.create(apiHostVersion + route.baseRoute()));
        
        if(route.method() == GET) {
            // No body
            builder.GET();
        } else if(route.method() == DELETE) {
            // Also no body
            builder.DELETE();
        } else {
            final var fakeBody = request.request.emptyBody();
            builder.setHeader("Content-Type", mediaType)
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
        
        // As of API v8, `X-Ratelimit-Precision` is no longer respected; the relevant headers are ms-precise by default.
        
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
                    final long requestEnd = System.nanoTime();
                    
                    catnip.rxScheduler().scheduleDirect(() ->
                            handleResponse(route, code, requestEnd, res.body(), res.headers(), request));
                })
                .exceptionally(e -> {
                    request.bucket.failedRequest(request, e);
                    return null;
                });
    }
    
    protected void handleResponse(@Nonnull final Route route, final int statusCode,
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
            // Retry-After is now returned in seconds, so convert to be useful
            final long retryAfter = TimeUnit.SECONDS.toMillis(Long.parseLong(retry));
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
                final var throwable = catnip.options().captureRestStacktraces() ? new RuntimeException("REST error context") : null;
                if(payload.string() != null && payload.string().startsWith("{")) {
                    // If the payload HAS a body, AND it looks like a JSON object, try to parse it for info
                    final JsonObject response = payload.object();
                    final var code = JsonErrorCode.byCode(catnip, response.getInt("code", -1));
                    if(statusCode == 400 && (code.code() == 0 || code.code() >= 10_000)) {
                        // 1000 was just the easiest number to check to skip over http error codes.
                        // Discord error codes are all >=10000 afaik, so this should be safe.
                        // Note that 0 is a valid JSON error code
                        catnip.logAdapter().trace("Status code 400 + JSON code, creating RestPayloadException...");
                        
                        if(code == JsonErrorCode.UNAUTHORIZED) {
                            if(route.baseRoute().replaceAll("\\d+", ":channel").equalsIgnoreCase(Routes.CREATE_MESSAGE.baseRoute())
                                    && route.method() == HttpMethod.POST) {
                                catnip.logAdapter().error("Attempted to send a message without ever connecting to the gateway! " +
                                        "Please connect to the gateway at least once (ex." +
                                        "`var catnip = Catnip.catnip(TOKEN); catnip.connect(); Thread.sleep(10_000); catnip.shutdown();`), " +
                                        "then try again.");
                            }
                            // The error-handling stuff below does not apply.
                            catnip.logAdapter().trace("Rejected (UNAUTHORIZED), returning empty error");
                            request.future().completeExceptionally(new RestPayloadException(code, Map.of()).initCause(throwable));
                            return;
                        }
                        
                        // Error messages in APIv8 are actually useful! :tada:
                        //
                        // There's 3 fields we care about:
                        // - `code`: The JSON error code. See JsonErrorCode.java
                        // - `message`: Probably just says "Invalid Form Body" or something like that,
                        //              but I don't want to just assume
                        // - `errors`: The interesting one!
                        //
                        // The object `errors` will contain a list of keys that contained incorrect data.
                        // For a given key:
                        // - If it's an object:
                        //   You get back an object that looks like this:
                        //   ```
                        //   {
                        //     "_errors": [
                        //       {
                        //         "code": "BASE_TYPE_REQUIRED",
                        //         "message": "This field is required"
                        //       }
                        //     ]
                        //   }
                        //   ```
                        // - If it's an array:
                        //   You get back an object that looks like this:
                        //   ```
                        //   {
                        //     "0": {
                        //       "inner_key": {
                        //         "_errors": {
                        //           "code": "BASE_TYPE_CHOICES",
                        //           "message": "Value must be one of (0, 1, 2, 3, 4, 5)
                        //         }
                        //       }
                        //     }
                        //   }
                        //   ```
                        //
                        // So when creating the RestPayloadException, we try to be useful:
                        // - The specific JSON error code is added to the resulting exception. This wasn't present
                        //   before catnip v3, so hopefully this makes it a little more usable
                        // - Each error key is effectively just directly added to a map. Trying to parse everything
                        //   out into a specialised set of data structures just doesn't feel worth it,
                        //   especially since Discord refuses to document all the inner error codes/messages
                        //
                        // Thus, """error handling""" is just raising an exception here and letting the
                        // API consumer bother parsing through the map or whatever.
                        //
                        // IDEALLY this will never have to happen. We should be providing a correct-enough
                        // interface to the REST API -- especially via convenience methods etc -- that an end
                        // user won't generally run into this. This is much more useful for, say, adding support
                        // for a new API route and debugging it.
                        //
                        // See: https://discord.com/developers/docs/reference#error-messages
                        
                        if(throwable != null) {
                            throwable.setStackTrace(request.stacktrace());
                        }
                        request.future().completeExceptionally(new RestPayloadException(
                                        code,
                                        response.getObject("errors") != null
                                                ? Map.copyOf(response.getObject("errors"))
                                                : Map.of()
                                ).initCause(throwable)
                        );
                    } else {
                        catnip.logAdapter().trace("Status code != 400, creating ResponseException...");
                        final String message = response.getString("message", "No message.");
                        if(throwable != null) {
                            throwable.setStackTrace(request.stacktrace());
                        }
                        request.future().completeExceptionally(new ResponseException(route.toString(), statusCode, code,
                                message, response).initCause(throwable));
                    }
                } else {
                    catnip.logAdapter().trace("Status code != 400 and no JSON body, creating ResponseException...");
                    if(throwable != null) {
                        throwable.setStackTrace(request.stacktrace());
                    }
                    request.future().completeExceptionally(new ResponseException(route.toString(), statusCode,
                            JsonErrorCode.UNKNOWN_ERROR_CODE, "No message.", null).initCause(throwable));
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
