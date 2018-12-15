package com.mewna.catnip.cache.view;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author natanbc
 * @since 12/15/18
 */
public interface CacheView<T> extends Iterable<T> {
    @Nonnegative
    long size();
    
    T getById(String id);
    
    default T getById(final long id) {
        return getById(Long.toUnsignedString(id));
    }
    
    T findAny(@Nonnull Predicate<T> filter);
    
    @Nonnull
    Collection<T> find(@Nonnull Predicate<T> filter);
    
    @Nonnull
    <C extends Collection<T>> C find(@Nonnull Predicate<T> filter, @Nonnull Supplier<C> supplier);
    
    @Nonnull
    Set<String> keys();
    
    @Nonnull
    Collection<T> values();
    
    @Nonnull
    Collection<T> snapshot();
    
    @Nonnull
    <C extends Collection<T>> C snapshot(@Nonnull Supplier<C> supplier);
    
    @Nonnull
    default Stream<T> stream() {
        return StreamSupport.stream(spliterator(), false);
    }
    
    @Nonnull
    @SuppressWarnings("unchecked")
    static <T> CacheView<T> empty() {
        return (CacheView<T>)EmptyCacheView.INSTANCE;
    }
}
