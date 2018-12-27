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
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.*;
import java.util.stream.Collector;

/**
 * Default {@link CacheView CacheView} implementation.
 *
 * @param <T> Type of the entity held by this cache.
 *
 * @author natanbc
 * @since 12/15/18
 */
public class DefaultCacheView<T> implements MutableCacheView<T> {
    protected final Map<Long, T> map = new ConcurrentHashMap<>();
    
    @Nonnull
    public Map<Long, T> map() {
        return map;
    }
    
    @Nullable
    @Override
    public T put(final long key, @Nonnull final T value) {
        return map.put(key, value);
    }
    
    @Nullable
    @Override
    public T remove(final long key) {
        return map.remove(key);
    }
    
    @Override
    public void forEach(final Consumer<? super T> action) {
        map.values().forEach(action);
    }
    
    @Nonnegative
    @Override
    public long size() {
        return map.size();
    }
    
    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }
    
    @Override
    public T getById(final long id) {
        return map.get(id);
    }
    
    @Override
    public T findAny(@Nonnull final Predicate<? super T> filter) {
        return map.values()
                .stream()
                .filter(filter)
                .findAny()
                .orElse(null);
    }
    
    @Nonnull
    @Override
    public Collection<T> find(@Nonnull final Predicate<? super T> filter) {
        return find(filter, ArrayList::new);
    }
    
    @Nonnull
    @Override
    public <C extends Collection<T>> C find(@Nonnull final Predicate<? super T> filter, @Nonnull final Supplier<C> supplier) {
        final C collection = Objects.requireNonNull(supplier.get(), "Provided collection may not be null");
        for(final T element : map.values()) {
            if(filter.test(element)) {
                collection.add(element);
            }
        }
        return collection;
    }
    
    @Nonnull
    @Override
    public <A, R> R collect(@Nonnull final Collector<? super T, A, R> collector) {
        final A a = collector.supplier().get();
        final BiConsumer<A, ? super T> accumulator = collector.accumulator();
        for(final T element : map.values()) {
            accumulator.accept(a, element);
        }
        return collector.finisher().apply(a);
    }
    
    @Override
    public <R> R collect(@Nonnull final Supplier<R> supplier, @Nonnull final BiConsumer<R, ? super T> accumulator, @Nonnull final BiConsumer<R, R> combiner) {
        final R result = supplier.get();
        for(final T element : map.values()) {
            accumulator.accept(result, element);
        }
        return result;
    }
    
    @Override
    public <U> U reduce(final U identity, @Nonnull final BiFunction<U, ? super T, U> accumulator, @Nonnull final BinaryOperator<U> combiner) {
        U result = identity;
        for(final T element : map.values()) {
            result = accumulator.apply(result, element);
        }
        return result;
    }
    
    @Nonnull
    @Override
    public Optional<T> reduce(@Nonnull final BinaryOperator<T> accumulator) {
        final Iterator<T> it = map.values().iterator();
        if(!it.hasNext()) {
            return Optional.empty();
        }
        T result = it.next();
        while(it.hasNext()) {
            final T element = it.next();
            result = accumulator.apply(result, element);
        }
        return Optional.of(result);
    }
    
    @Override
    public T reduce(final T identity, @Nonnull final BinaryOperator<T> accumulator) {
        T result = identity;
        for (final T element : map.values()) {
            result = accumulator.apply(result, element);
        }
        return result;
    }
    
    @Override
    public boolean anyMatch(@Nonnull final Predicate<? super T> predicate) {
        for(final T element : map.values()) {
            if(predicate.test(element)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean allMatch(@Nonnull final Predicate<? super T> predicate) {
        for(final T element : map.values()) {
            if(!predicate.test(element)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public boolean noneMatch(@Nonnull final Predicate<? super T> predicate) {
        return !anyMatch(predicate);
    }
    
    @Nonnull
    @Override
    public Optional<T> min(@Nonnull final Comparator<? super T> comparator) {
        final Iterator<T> it = map.values().iterator();
        if(!it.hasNext()) {
            return Optional.empty();
        }
        T min = it.next();
        while(it.hasNext()) {
            final T element = it.next();
            if(comparator.compare(min, element) > 0) {
                min = element;
            }
        }
        return Optional.of(min);
    }
    
    @Nonnull
    @Override
    public Optional<T> max(@Nonnull final Comparator<? super T> comparator) {
        final Iterator<T> it = map.values().iterator();
        if(!it.hasNext()) {
            return Optional.empty();
        }
        T max = it.next();
        while(it.hasNext()) {
            final T element = it.next();
            if(comparator.compare(max, element) < 0) {
                max = element;
            }
        }
        return Optional.of(max);
    }
    
    @Override
    public long count(@Nonnull final Predicate<? super T> filter) {
        long count = 0;
        for(final T element : map.values()) {
            if(filter.test(element)) {
                count++;
            }
        }
        return count;
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
        final Collection<T> values = map.values();
        final Collection<T> r = new ArrayList<>((int)size());
        //this is actually more efficient than addAll(),
        //as addAll() on ArrayList requires calls Collection#toArray(),
        //while this won't allocate any temporary array due to the
        //initial size of the list.
        //noinspection UseBulkOperation
        values.forEach(r::add);
        return r;
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
        return Collections.unmodifiableCollection(map.values()).iterator();
    }
}
