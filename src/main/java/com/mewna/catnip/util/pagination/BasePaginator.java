package com.mewna.catnip.util.pagination;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Utility class for iterating paginated endpoints, handling fetching the
 * next objects automatically, until a given limit is reached or no more
 * entities can be fetched.
 *
 * @param <T> Type of the fetched entities.
 * @param <J> (Internal use)
 * @param <P> (Internal use)
 *
 * @author natanbc
 * @since 10/8/18.
 */
public abstract class BasePaginator<T, J, P extends BasePaginator<T, J, P>> {
    protected final Function<T, String> idOf;
    protected final int maxRequestSize;
    protected int requestSize;
    protected int limit;
    
    public BasePaginator(@Nonnull final Function<T, String> idOf,
                         @Nonnegative final int maxRequestSize) {
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
    public P limit(@Nonnegative final int limit) {
        this.limit = limit;
        return uncheck(this);
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
    public P requestSize(@Nonnegative final int requestSize) {
        if(requestSize > maxRequestSize) {
            throw new IllegalArgumentException("Request size (" + requestSize +
                    ") greater than maximum request size (" + maxRequestSize + ')');
        }
        this.requestSize = requestSize;
        return uncheck(this);
    }
    
    /**
     * Fetches up to {@link #limit(int) limit} entities, returning a list
     * containing all of them.
     * <br><b>This method will keep all entities in memory</b>, so for unbounded
     * pagination it should be avoided.
     *
     * @return A list containing all the fetched entities.
     */
    @Nonnull
    @CheckReturnValue
    public CompletionStage<List<T>> fetch() {
        final List<T> list = new ArrayList<>();
        return forEach(list::add).thenApply(__ -> Collections.unmodifiableList(list));
    }
    
    /**
     * Fetches up to {@link #limit(int) limit} entities, providing them to a
     * given callback.
     * <br>This method will not cache the provided entities, so it's
     * recommended for unbounded pagination.
     * <br>If the provided callback throws an exception, <b>pagination
     * will stop</b> and the returned {@link CompletionStage completion stage}
     * will be failed.
     *
     * @param action Callback for fetched entities.
     *
     * @return A completion stage representing the end of the iteration.
     */
    @Nonnull
    public CompletionStage<Void> forEach(@Nonnull final Consumer<T> action) {
        return fetchWhile(e -> {
            action.accept(e);
            return true;
        });
    }
    
    /**
     * Fetches entities until the provided callback returns false or the
     * {@link #limit(int) limit} is reached.
     * <br>This method will not cache the provided entities, so it's
     * recommended for unbounded pagination.
     * <br>If the provided callback throws an exception, <b>pagination
     * will stop</b> and the returned {@link CompletionStage completion stage}
     * will be failed.
     *
     * @param callback Callback for fetched entities.
     *
     * @return A completion stage representing the end of the iteration.
     */
    @Nonnull
    public CompletionStage<Void> fetchWhile(@Nonnull final PaginationCallback<T> callback) {
        return fetch(callback);
    }
    
    @Nonnull
    @CheckReturnValue
    protected CompletionStage<Void> fetch(@Nonnull final PaginationCallback<T> action) {
        return fetch(null, new RequestState<>(limit, requestSize, action));
    }
    
    @Nonnull
    @CheckReturnValue
    protected CompletionStage<Void> fetch(@Nullable final String id, @Nonnull final RequestState<T> state) {
        final int fetchCount = state.entitiesToFetch();
        return fetchNext(state, id, fetchCount).thenCompose(data -> {
            final int remaining = state.remaining();
            update(state, data);
            final T last = state.last();
            if(state.done() || remaining - fetchCount != state.remaining() || last == null) {
                return CompletableFuture.completedFuture(null);
            }
            return fetch(idOf.apply(last), state);
        });
    }
    
    protected abstract void update(@Nonnull RequestState<T> state, @Nonnull J data);
    
    @Nonnull
    @CheckReturnValue
    protected abstract CompletionStage<J> fetchNext(@Nonnull RequestState<T> state, @Nullable String lastId,
                                                    @Nonnegative int requestSize);
    
    @Nonnull
    @CheckReturnValue
    @SuppressWarnings("unchecked")
    private static <T, J, P extends BasePaginator<T, J, P>> P uncheck(@Nonnull final BasePaginator<?, ?, ?> paginator) {
        return (P)paginator;
    }
    
    protected static class RequestState<T> {
        private final Map<String, Object> extras = new HashMap<>();
        private final int limit;
        private final int requestSize;
        private final PaginationCallback<T> callback;
        private int fetched;
        private T last;
        private boolean callbackDone;
    
        public RequestState(final int limit, final int requestSize, final PaginationCallback<T> callback) {
            this.limit = limit;
            this.requestSize = requestSize;
            this.callback = callback;
        }
        
        public void update(@Nonnull final T entity) {
            if(done()) {
                return;
            }
            callbackDone = !callback.accept(entity);
            fetched++;
            last = entity;
        }
    
        @CheckReturnValue
        public boolean done() {
            return callbackDone || limit > 0 && fetched == limit;
        }
    
        @CheckReturnValue
        public int entitiesToFetch() {
            return Math.min(requestSize, remaining());
        }
    
        @CheckReturnValue
        public int remaining() {
            return limit - fetched;
        }
        
        @Nullable
        @CheckReturnValue
        public T last() {
            return last;
        }
        
        @Nonnull
        public RequestState<T> extra(@Nonnull final String key, @Nonnull final Object value) {
            extras.put(key, value);
            return this;
        }
        
        @Nullable
        @CheckReturnValue
        @SuppressWarnings("unchecked")
        public <U> U extra(@Nonnull final String key) {
            return (U)extras.get(key);
        }
    }
}
