package com.mewna.catnip.cache.view;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * A {@link CacheView CacheView} that provides utilities for finding
 * elements based on their name.
 *
 * @param <T> Type of the entity held by this cache.
 *
 * @author natanbc
 * @since 12/15/18
 */
public interface NamedCacheView<T> extends CacheView<T> {
    /**
     * Finds all entities with a name equal to the provided value.
     *
     * @param name Name to search for.
     * @param ignoreCase Ignore casing differences between the entity
     *                   names and the provided name.
     *
     * @return All elements that have a name equal to the provided.
     *
     * @see String#equals(Object)
     * @see String#equalsIgnoreCase(String)
     */
    @Nonnull
    Collection<T> findByName(@Nonnull String name, boolean ignoreCase);
    
    /**
     * Finds all entities with a name equal to the provided value.
     *
     * @param name Name to search for.
     *
     * @return All elements that have a name equal to the provided.
     *
     * @see String#equals(Object)
     */
    @Nonnull
    default Collection<T> findByName(@Nonnull final String name) {
        return findByName(name, false);
    }
    
    /**
     * Finds all entities whose name contains the provided value.
     *
     * @param name Name to search for.
     * @param ignoreCase Ignore casing differences between the entity
     *                   names and the provided name.
     *
     * @return All elements that have a name containing the provided.
     *
     * @see String#contains(CharSequence)
     * @see com.mewna.catnip.util.Utils#containsIgnoreCase(String, String)
     */
    @Nonnull
    Collection<T> findByNameContains(@Nonnull final String name, boolean ignoreCase);
    
    /**
     * Finds all entities whose name contains the provided value.
     *
     * @param name Name to search for.
     *
     * @return All elements that have a name containing the provided.
     *
     * @see String#contains(CharSequence)
     */
    @Nonnull
    default Collection<T> findByNameContains(@Nonnull final String name) {
        return findByNameContains(name, false);
    }
    
    /**
     * Finds all entities whose name starts with the provided value.
     *
     * @param name Name to search for.
     * @param ignoreCase Ignore casing differences between the entity
     *                   names and the provided name.
     *
     * @return All elements that have a name starting with the provided.
     *
     * @see String#startsWith(String)
     * @see com.mewna.catnip.util.Utils#startsWithIgnoreCase(String, String)
     */
    @Nonnull
    Collection<T> findByNameStartsWith(@Nonnull final String name, boolean ignoreCase);
    
    /**
     * Finds all entities whose name starts with the provided value.
     *
     * @param name Name to search for.
     *
     * @return All elements that have a name starting with the provided.
     *
     * @see String#startsWith(String)
     */
    @Nonnull
    default Collection<T> findByNameStartsWith(@Nonnull final String name) {
        return findByNameStartsWith(name, false);
    }
    
    /**
     * Finds all entities whose name ends with the provided value.
     *
     * @param name Name to search for.
     * @param ignoreCase Ignore casing differences between the entity
     *                   names and the provided name.
     *
     * @return All elements that have a name ending with the provided.
     *
     * @see String#endsWith(String)
     * @see com.mewna.catnip.util.Utils#endsWithIgnoreCase(String, String)
     */
    @Nonnull
    Collection<T> findByNameEndsWith(@Nonnull final String name, boolean ignoreCase);
    
    /**
     * Finds all entities whose name ends with the provided value.
     *
     * @param name Name to search for.
     *
     * @return All elements that have a name ending with the provided.
     *
     * @see String#endsWith(String)
     */
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
