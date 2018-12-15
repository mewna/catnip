package com.mewna.catnip.cache.view;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class EmptyCacheView<T> implements CacheView<T> {
    public static final CacheView<?> INSTANCE = new EmptyCacheView<>();
    
    @Override
    public long size() {
        return 0;
    }
    
    @Override
    public T getById(final String id) {
        return null;
    }
    
    @Override
    public T findAny(@Nonnull final Predicate<T> filter) {
        return null;
    }
    
    @Nonnull
    @Override
    public Collection<T> find(@Nonnull final Predicate<T> filter) {
        return Collections.emptyList();
    }
    
    @Nonnull
    @Override
    public <C extends Collection<T>> C find(@Nonnull final Predicate<T> filter, @Nonnull final Supplier<C> supplier) {
        return supplier.get();
    }
    
    @Nonnull
    @Override
    public Set<String> keys() {
        return Collections.emptySet();
    }
    
    @Nonnull
    @Override
    public Collection<T> values() {
        return Collections.emptyList();
    }
    
    @Nonnull
    @Override
    public Collection<T> snapshot() {
        return Collections.emptyList();
    }
    
    @Nonnull
    @Override
    public <C extends Collection<T>> C snapshot(@Nonnull final Supplier<C> supplier) {
        return supplier.get();
    }
    
    @Nonnull
    @Override
    public Iterator<T> iterator() {
        return Collections.emptyIterator();
    }
}
