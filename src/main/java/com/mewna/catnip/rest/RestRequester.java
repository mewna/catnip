package com.mewna.catnip.rest;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.rest.Routes.Route;
import com.mewna.catnip.util.CatnipMeta;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.escoffier.vertx.completablefuture.VertxCompletableFuture;

import java.util.Collection;
import java.util.Deque;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

import static io.vertx.core.http.HttpMethod.DELETE;
import static io.vertx.core.http.HttpMethod.GET;

/**
 * TODO: Refactor this out into interface and implementation to allow plugging in other impls
 *
 * @author amy
 * @since 8/31/18.
 */
public class RestRequester {
    public static final String API_HOST = "https://discordapp.com";
    private static final int API_VERSION = 6;
    public static final String API_BASE = "/api/v" + API_VERSION;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final Catnip catnip;
    private final WebClient client;
    private final Collection<Bucket> submittedBuckets = new ConcurrentHashSet<>();
    
    public RestRequester(final Catnip catnip) {
        this.catnip = catnip;
        client = WebClient.create(catnip.vertx());
    }
    
    public CompletableFuture<ResponsePayload> queue(final OutboundRequest r) {
        final Future<ResponsePayload> future = Future.future();
        getBucket(r.route.baseRoute()).queue(future, r);
        return VertxCompletableFuture.from(catnip.vertx(), future);
    }
    
    private Bucket getBucket(final String key) {
        return buckets.computeIfAbsent(key, k -> new Bucket(k, 5, 1, -1));
    }
    
    private void handleResponse(final OutboundRequest r, final Bucket bucket, final AsyncResult<HttpResponse<Buffer>> res) {
        if(res.succeeded()) {
            final HttpResponse<Buffer> result = res.result();
            if(result.statusCode() < 200 || result.statusCode() > 299) {
                if(result.statusCode() != 429) {
                    catnip.logAdapter().warn("Got unexpected HTTP status: {} {}", result.statusCode(), result.statusMessage());
                }
            }
            boolean ratelimited = false;
            final boolean hasMemeReactionRatelimits = r.route.method() != GET
                    && r.route.baseRoute().contains("/reactions/");
            if(result.statusCode() == 429) {
                ratelimited = true;
                // Reactions are a HUGE meme
                // We hit *roughly* one 429 / reaction if we're adding many
                // reactions. I *think* this is ok?
                // TODO: Warn if we hit the meme ratelimit a lot
                if(!hasMemeReactionRatelimits) {
                    catnip.logAdapter().error("Hit 429! Route: {}, X-Ratelimit-Global: {}, X-Ratelimit-Limit: {}, X-Ratelimit-Reset: {}",
                            r.route.baseRoute(),
                            result.getHeader("X-Ratelimit-Global"),
                            result.getHeader("X-Ratelimit-Limit"),
                            result.getHeader("X-Ratelimit-Reset")
                    );
                }
            }
            final ResponsePayload payload = new ResponsePayload(result.bodyAsBuffer());
            final MultiMap headers = result.headers();
            if(headers.contains("X-Ratelimit-Global")) {
                // We hit a global ratelimit, update
                final Bucket global = getBucket("GLOBAL");
                final long retry = Long.parseLong(headers.get("Retry-After"));
                global.setRemaining(0);
                global.setLimit(1);
                // 500ms buffer for safety
                final long globalReset = System.currentTimeMillis() + retry + 500L;
                // CatnipImpl.vertx().setTimer(globalReset, __ -> global.reset());
                global.setReset(TimeUnit.MILLISECONDS.toSeconds(globalReset));
                bucket.retry(r);
            } else if(ratelimited) {
                // We got ratelimited, back the fuck off
                bucket.updateFromHeaders(headers);
                if(hasMemeReactionRatelimits) {
                    // Ratelimits are a meme with reactions
                    catnip.vertx().setTimer(250L, __ -> bucket.retry(r));
                } else {
                    // Try and compute from headers
                    bucket.updateFromHeaders(headers);
                    bucket.retry(r);
                }
            } else {
                bucket.updateFromHeaders(headers);
                r.future.complete(payload);
                bucket.finishRequest();
                bucket.submit();
            }
        } else {
            // Fail request, resubmit to queue if failed less than 3 times, complete with error otherwise.
            r.failed();
            if(r.failedAttempts() >= 3) {
                r.future.fail(res.cause());
                bucket.finishRequest();
                bucket.submit();
            } else {
                bucket.retry(r);
            }
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
        
        if(global.getRemaining() == 0 && global.getReset() < System.currentTimeMillis()) {
            global.reset();
        }
        
        if(global.getRemaining() > 0) {
            // Can request
            if(bucket.getRemaining() == 0 && bucket.getReset() < System.currentTimeMillis()) {
                bucket.reset();
            }
            // add/remove/remove_all routes for reactions have a meme 1/0.25s
            // ratelimit, which isn't accurately reflected in the responses
            // from the API. Instead, we just try anyway and re-queue if we get
            // a 429.
            // Assuming you're messing with N reactions, where N > 1, you'll
            // run into ~(N-1) 429s. I *think* this is okay?
            final boolean hasMemeReactionRatelimits = route.method() != GET
                    && route.baseRoute().contains("/reactions/");
            if(bucket.getRemaining() > 0 || hasMemeReactionRatelimits) {
                // Do request and update bucket
                catnip.logAdapter().debug("Making request: {} (bucket {})", API_BASE + route.baseRoute(), bucket.route);
                final HttpRequest<Buffer> req = client.requestAbs(bucketRoute.method(),
                        API_HOST + API_BASE + route.baseRoute()).ssl(true)
                        .putHeader("Authorization", "Bot " + catnip.token())
                        .putHeader("User-Agent", "DiscordBot (https://github.com/mewna/catnip, " + CatnipMeta.VERSION + ')'); // 0.3.0)");
                // GET and DELETE don't have payloads, but the rest do
                if(route.method() != GET && route.method() != DELETE) {
                    req.sendJsonObject(r.data, res -> handleResponse(r, bucket, res));
                } else {
                    req.send(res -> handleResponse(r, bucket, res));
                }
            } else {
                // Add an extra 500ms buffer to be safe
                final long wait = bucket.getReset() - System.currentTimeMillis() + 500L;
                catnip.logAdapter().debug("Hit ratelimit on bucket {} for route {}, waiting {}ms and retrying...",
                        bucketRoute.baseRoute(), route.baseRoute(), wait);
                catnip.vertx().setTimer(wait, __ -> {
                    bucket.reset();
                    bucket.retry(r);
                });
            }
        } else {
            // Global rl, retry later
            // Add an extra 500ms buffer to be safe
            final long wait = global.getReset() - System.currentTimeMillis() + 500L;
            catnip.logAdapter().debug("Hit ratelimit on bucket {} for route {}, waiting {}ms and retrying...",
                    bucketRoute.baseRoute(), route.baseRoute(), wait);
            catnip.vertx().setTimer(wait, __ -> {
                global.reset();
                bucket.retry(r);
            });
        }
    }
    
    @Getter
    @Accessors(fluent = true)
    @SuppressWarnings("unused")
    public static final class OutboundRequest {
        private Route route;
        private Map<String, String> params;
        private JsonObject data;
        @Setter
        private Future<ResponsePayload> future;
        private int failedAttempts;
        
        public OutboundRequest() {
        }
        
        public OutboundRequest(final Route route, final Map<String, String> params, final JsonObject data) {
            this.route = route;
            this.params = params;
            this.data = data;
        }
        
        void failed() {
            failedAttempts++;
        }
        
        int failedAttempts() {
            return failedAttempts;
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
        private long limit;
        private long remaining;
        private long reset;
        
        void reset() {
            remaining = limit;
            reset = -1L;
        }
        
        void updateFromHeaders(final MultiMap headers) {
            if(!(headers.contains("X-Ratelimit-Limit") && headers.contains("X-Ratelimit-Remaining") && headers.contains("X-Ratelimit-Reset"))) {
                return;
            }
            limit = Integer.parseInt(headers.get("X-Ratelimit-Limit"));
            remaining = Integer.parseInt(headers.get("X-Ratelimit-Remaining"));
            reset = TimeUnit.SECONDS.toMillis(Integer.parseInt(headers.get("X-Ratelimit-Reset")));
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
