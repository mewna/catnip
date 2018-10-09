package com.mewna.catnip.util;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Utility class for iterating paginated endpoints, handling fetching the
 * next objects automatically, until a given limit is reached or no more
 * entities can be fetched.
 *
 * @param <T>
 *
 * @author natanbc
 * @since 10/8/18.
 */
public class Paginator<T> {
    private final Function<JsonObject, T> mapper;
    private final BiFunction<String, Integer, CompletionStage<JsonArray>> fetchNext;
    private final Function<T, String> idOf;
    private final int maxRequestSize;
    private int requestSize;
    private int limit;
    
    public Paginator(@Nonnull final Function<JsonObject, T> mapper,
                     @Nonnull final BiFunction<String, Integer, CompletionStage<JsonArray>> fetchNext,
                     @Nonnull final Function<T, String> idOf,
                     @Nonnegative final int maxRequestSize) {
        this.mapper = mapper;
        this.fetchNext = fetchNext;
        this.idOf = idOf;
        this.maxRequestSize = maxRequestSize;
        requestSize = maxRequestSize;
    }
    
    /**
     * Sets a limit to how many objects will be requested.
     * <br>Only affects future calls to methods that start a
     * pagination.
     * <br>The actual amount of entities fetched may be smaller
     * than the limit.
     *
     * @param limit Maximum amount of entities to fetch.
     *
     * @return {@code this}, for chaining calls.
     */
    public Paginator<T> limit(@Nonnegative final int limit) {
        this.limit = limit;
        //if limit < maxRequestSize we can optimize it a bit,
        //otherwise just use max size to do as few requests
        //as possible
        requestSize = Math.min(maxRequestSize, limit);
        return this;
    }
    
    /**
     * Sets how many entities to fetch per request.
     * <br>Usually you don't need to touch this method,
     * as an appropriate value will be chosen by default.
     *
     * @param requestSize Amount of entities to fetch per request.
     *
     * @return {@code this}, for chaining calls.
     */
    public Paginator<T> requestSize(@Nonnegative final int requestSize) {
        if(requestSize > maxRequestSize) {
            throw new IllegalArgumentException("Request size (" + requestSize +
                    ") greater than maximum request size (" + maxRequestSize + ')');
        }
        this.requestSize = requestSize;
        return this;
    }
    
    @Nonnull
    @CheckReturnValue
    public CompletionStage<List<T>> fetch() {
        final List<T> list = new ArrayList<>();
        return fetch(list::add).thenApply(__ -> Collections.unmodifiableList(list));
    }
    
    @Nonnull
    @CheckReturnValue
    public CompletionStage<Void> forEach(@Nonnull final Consumer<T> action) {
        return fetch(action);
    }
    
    @Nonnull
    @CheckReturnValue
    protected CompletionStage<Void> fetch(@Nonnull final Consumer<T> action) {
        return fetch(null, new AtomicInteger(), action, limit, requestSize);
    }
    
    @Nonnull
    @CheckReturnValue
    protected CompletionStage<Void> fetch(@Nullable final String id, @Nonnull final AtomicInteger fetched,
                                        @Nonnull final Consumer<T> action, @Nonnegative final int limit,
                                        @Nonnegative final int requestSize) {
        return fetchNext.apply(id, Math.min(requestSize, limit - fetched.get())).thenCompose(array -> {
            T last = null;
            for(final Object object : array) {
                if(!(object instanceof JsonObject)) {
                    throw new IllegalArgumentException("Expected array to contain only objects, but found " +
                            (object == null ? "null" : object.getClass())
                    );
                }
                last = mapper.apply((JsonObject) object);
                action.accept(last);
                if(fetched.incrementAndGet() == limit) {
                    return CompletableFuture.completedFuture(null);
                }
            }
            if(array.size() < requestSize || last == null) {
                return CompletableFuture.completedFuture(null);
            }
            return fetch(idOf.apply(last), fetched, action, limit, requestSize);
        });
    }
}
