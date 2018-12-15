package com.mewna.catnip.cache.view;

import javax.annotation.Nonnull;
import java.util.Collection;

public interface NamedCacheView<T> extends CacheView<T> {
    @Nonnull
    Collection<T> findByName(@Nonnull String name, boolean ignoreCase);
    
    @Nonnull
    default Collection<T> findByName(@Nonnull final String name) {
        return findByName(name, false);
    }
    
    @Nonnull
    Collection<T> findByNameContains(@Nonnull final String name, boolean ignoreCase);
    
    @Nonnull
    default Collection<T> findByNameContains(@Nonnull final String name) {
        return findByNameContains(name, false);
    }
    
    @Nonnull
    Collection<T> findByNameStartsWith(@Nonnull final String name, boolean ignoreCase);
    
    @Nonnull
    default Collection<T> findByNameStartsWith(@Nonnull final String name) {
        return findByNameStartsWith(name, false);
    }
    
    @Nonnull
    Collection<T> findByNameEndsWith(@Nonnull final String name, boolean ignoreCase);
    
    @Nonnull
    default Collection<T> findByNameEndsWith(@Nonnull final String name) {
        return findByNameEndsWith(name, false);
    }
    
    @Nonnull
    @SuppressWarnings("unchecked")
    static <T> NamedCacheView<T> empty() {
        return (NamedCacheView<T>)EmptyNamedCacheView.INSTANCE;
    }
}
