package com.mewna.catnip.cache.view;

import com.mewna.catnip.util.Utils;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.function.Function;

public class DefaultNamedCacheView<T> extends DefaultCacheView<T> implements NamedCacheView<T> {
    private final Function<T, String> nameFunction;
    
    public DefaultNamedCacheView(final Function<T, String> nameFunction) {
        this.nameFunction = nameFunction;
    }
    
    @Nonnull
    @Override
    public Collection<T> findByName(@Nonnull final String name, final boolean ignoreCase) {
        return find(e -> ignoreCase ? name(e).equalsIgnoreCase(name) : name(e).equals(name));
    }
    
    @Nonnull
    @Override
    public Collection<T> findByNameContains(@Nonnull final String name, final boolean ignoreCase) {
        return find(e -> ignoreCase ? Utils.containsIgnoreCase(name(e), name) : name(e).contains(name));
    }
    
    @Nonnull
    @Override
    public Collection<T> findByNameStartsWith(@Nonnull final String name, final boolean ignoreCase) {
        return find(e -> ignoreCase ? Utils.startsWithIgnoreCase(name(e), name) : name(e).startsWith(name));
    }
    
    @Nonnull
    @Override
    public Collection<T> findByNameEndsWith(@Nonnull final String name, final boolean ignoreCase) {
        return find(e -> ignoreCase ? Utils.endsWithIgnoreCase(name(e), name) : name(e).endsWith(name));
    }
    
    private String name(@Nonnull final T element) {
        final String name = nameFunction.apply(element);
        return name == null ? "" : name;
    }
}
