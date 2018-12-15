package com.mewna.catnip.cache.view;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

public class EmptyNamedCacheView<T> extends EmptyCacheView<T> implements NamedCacheView<T> {
    public static final NamedCacheView<?> INSTANCE = new EmptyNamedCacheView<>();
    
    @Nonnull
    @Override
    public Collection<T> findByName(@Nonnull final String name, final boolean ignoreCase) {
        return Collections.emptyList();
    }
    
    @Nonnull
    @Override
    public Collection<T> findByNameContains(@Nonnull final String name, final boolean ignoreCase) {
        return Collections.emptyList();
    }
    
    @Nonnull
    @Override
    public Collection<T> findByNameStartsWith(@Nonnull final String name, final boolean ignoreCase) {
        return Collections.emptyList();
    }
    
    @Nonnull
    @Override
    public Collection<T> findByNameEndsWith(@Nonnull final String name, final boolean ignoreCase) {
        return Collections.emptyList();
    }
}
