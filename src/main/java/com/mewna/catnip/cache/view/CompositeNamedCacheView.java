package com.mewna.catnip.cache.view;

import com.mewna.catnip.util.Utils;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.function.Function;

/**
 * A {@link NamedCacheView NamedCacheView} that's equivalent to a collection of provided ones.
 * Any update to any of the provided views will update this view.
 *
 * @param <T> Type of the entity held by this cache.
 *
 * @author natanbc
 * @since 12/15/18
 */
public class CompositeNamedCacheView<T> extends CompositeCacheView<T> implements NamedCacheView<T> {
    protected final Function<T, String> nameFunction;
    
    public CompositeNamedCacheView(@Nonnull final Collection<? extends CacheView<T>> sources, final Function<T, String> nameFunction) {
        super(sources);
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
