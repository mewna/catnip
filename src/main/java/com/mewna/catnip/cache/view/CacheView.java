/*
 * Copyright (c) 2018 amy, All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.mewna.catnip.cache.view;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Represents a view of a given cache. When the cache is updated, the view is also updated.
 * This interface represents a low overhead API of reading the cache, without exposing methods
 * that may modify it, possibly leading to an inconsistent state.
 *
 * @param <T> Type of the entity held by this cache.
 *
 * @author natanbc
 * @since 12/15/18
 */
public interface CacheView<T> extends Iterable<T> {
    /**
     * @return An always empty cache view. Cannot be modified.
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    static <T> CacheView<T> empty() {
        return (CacheView<T>) EmptyCacheView.INSTANCE;
    }
    
    /**
     * @return The size of this cache.
     */
    @Nonnegative
    long size();
    
    /**
     * @param id ID of the entity to fetch.
     *
     * @return The element with the provided ID, or {@code null} if it isn't cached.
     */
    T getById(long id);
    
    /**
     * @param id ID of the entity to fetch.
     *
     * @return The element with the provided ID, or {@code null} if it isn't cached.
     */
    default T getById(@Nonnull final String id) {
        return getById(Long.parseUnsignedLong(id));
    }
    
    /**
     * Returns any element in this cache that matches the given filter. There are no order
     * guarantees if multiple elements match. Use with caution.
     *
     * @param filter Filter to find matching elements.
     *
     * @return Any element that matches the provided filter, or {@code null} if none match.
     */
    T findAny(@Nonnull Predicate<T> filter);
    
    /**
     * Returns all elements in this cache that matches the given filter. There are no order
     * guarantees if multiple elements match.
     *
     * @param filter Filter to find matching elements.
     *
     * @return A collection with all the matching elements. May be empty.
     */
    @Nonnull
    Collection<T> find(@Nonnull Predicate<T> filter);
    
    /**
     * Returns all elements in this cache that matches the given filter. There are no order
     * guarantees if multiple elements match.
     *
     * @param filter   Filter to find matching elements.
     * @param supplier Supplier for the collection to add the elements to. The returned
     *                 collection <b>must</b> be mutable.
     *
     * @return The collection returned by {@code supplier}, after adding the matching
     * elements. May be empty.
     */
    @Nonnull
    <C extends Collection<T>> C find(@Nonnull Predicate<T> filter, @Nonnull Supplier<C> supplier);
    
    /**
     * @return A view of all the keys in this cache. Updated if this cache is modified.
     *
     * @see Map#keySet()
     */
    @Nonnull
    Set<Long> keys();
    
    /**
     * @return A view of all the values in this cache. Updated if this cache is modified.
     *
     * @see Map#values()
     */
    @Nonnull
    Collection<T> values();
    
    /**
     * @return A snapshot of all the values in this cache. <b>Not</b> updated if this cache is modified.
     *
     * @see #values()
     * @see #snapshot(Supplier)
     */
    @Nonnull
    Collection<T> snapshot();
    
    /**
     * @param supplier Supplier for the collection to add the elements to. The returned
     *                 collection <b>must</b> be mutable.
     *
     * @return The collection returned by {@code supplier}, after adding the cached
     * elements. May be empty.
     *
     * @see #values()
     * @see #snapshot()
     */
    @Nonnull
    <C extends Collection<T>> C snapshot(@Nonnull Supplier<C> supplier);
    
    /**
     * @return A stream with the elements cached, in no specific order.
     *
     * @see Collection#stream()
     */
    @Nonnull
    default Stream<T> stream() {
        return StreamSupport.stream(spliterator(), false);
    }
}
