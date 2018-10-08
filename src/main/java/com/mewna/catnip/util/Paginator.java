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
    
    public Paginator<T> limit(@Nonnegative final int limit) {
        this.limit = limit;
        return this;
    }
    
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
    private CompletionStage<Void> fetch(@Nonnull final Consumer<T> action) {
        return fetch(null, new AtomicInteger(), action);
    }
    
    @Nonnull
    @CheckReturnValue
    private CompletionStage<Void> fetch(@Nullable final String id, @Nonnull final AtomicInteger fetched,
                                        @Nonnull final Consumer<T> action) {
        return fetchNext.apply(id, requestSize).thenCompose(array -> {
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
            return fetch(idOf.apply(last), fetched, action);
        });
    }
}
