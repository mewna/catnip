package com.mewna.catnip.cache.view;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Default {@link CacheView CacheView} implementation.
 *
 * @param <T> Type of the entity held by this cache.
 *
 * @author natanbc
 * @since 12/15/18
 */
public class DefaultCacheView<T> implements CacheView<T> {
    protected final Map<String, T> map = new ConcurrentHashMap<>();
    
    @Nonnull
    public Map<String, T> map() {
        return map;
    }
    
    @Nullable
    public T put(@Nonnull final String key, @Nonnull final T value) {
        return map.put(key, value);
    }
    
    @Nullable
    public T remove(@Nonnull final String key) {
        return map.remove(key);
    }
    
    @Override
    public long size() {
        return map.size();
    }
    
    @Override
    public T getById(final String id) {
        return map.get(id);
    }
    
    @Override
    public T findAny(@Nonnull final Predicate<T> filter) {
        return map.values()
                .stream()
                .filter(filter)
                .findAny()
                .orElse(null);
    }
    
    @Nonnull
    @Override
    public Collection<T> find(@Nonnull final Predicate<T> filter) {
        return map.values()
                .stream()
                .filter(filter)
                .collect(Collectors.toList());
    }
    
    @Nonnull
    @Override
    public <C extends Collection<T>> C find(@Nonnull final Predicate<T> filter, @Nonnull final Supplier<C> supplier) {
        final C collection = Objects.requireNonNull(supplier.get(), "Provided collection may not be null");
        return map.values()
                .stream()
                .filter(filter)
                .collect(Collectors.toCollection(() -> collection));
    }
    
    @Nonnull
    @Override
    public Set<String> keys() {
        return Collections.unmodifiableSet(map.keySet());
    }
    
    @Nonnull
    @Override
    public Collection<T> values() {
        return Collections.unmodifiableCollection(map.values());
    }
    
    @Nonnull
    @Override
    public Collection<T> snapshot() {
        return snapshot(() -> new ArrayList<>((int)size()));
    }
    
    @Nonnull
    @Override
    public <C extends Collection<T>> C snapshot(@Nonnull final Supplier<C> supplier) {
        final Collection<T> values = map.values();
        final C r = Objects.requireNonNull(supplier.get(), "Provided collection may not be null");
        r.addAll(values);
        return r;
    }
    
    @Nonnull
    @Override
    public Iterator<T> iterator() {
        return map.values().iterator();
    }
}
