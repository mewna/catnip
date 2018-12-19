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
    protected final Map<Long, T> map = new ConcurrentHashMap<>();
    
    @Nonnull
    public Map<Long, T> map() {
        return map;
    }
    
    @Nullable
    public T put(final long key, @Nonnull final T value) {
        return map.put(key, value);
    }
    
    @Nullable
    public T put(@Nonnull final String key, @Nonnull final T value) {
        return put(Long.parseUnsignedLong(key), value);
    }
    
    @Nullable
    public T remove(final long key) {
        return map.remove(key);
    }
    
    @Nullable
    public T remove(@Nonnull final String key) {
        return remove(Long.parseUnsignedLong(key));
    }
    
    @Override
    public long size() {
        return map.size();
    }
    
    @Override
    public T getById(final long id) {
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
    public Set<Long> keys() {
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
        return snapshot(() -> new ArrayList<>((int) size()));
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
