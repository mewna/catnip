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
import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;

/**
 * Always empty {@link CacheView cache view}.
 *
 * @author natanbc
 * @see CacheView#empty()
 * @since 12/15/18
 */
public class EmptyCacheView<T> implements CacheView<T> {
    public static final CacheView<?> INSTANCE = new EmptyCacheView<>();
    
    @Override
    public void forEach(final Consumer<? super T> action) {
    
    }
    
    @Override
    public long size() {
        return 0;
    }
    
    @Override
    public boolean isEmpty() {
        return true;
    }
    
    @Override
    public T getById(final long id) {
        return null;
    }
    
    @Override
    public T findAny(@Nonnull final Predicate<? super T> filter) {
        return null;
    }
    
    @Nonnull
    @Override
    public Collection<T> find(@Nonnull final Predicate<? super T> filter) {
        return Collections.emptyList();
    }
    
    @Nonnull
    @Override
    public <C extends Collection<T>> C find(@Nonnull final Predicate<? super T> filter, @Nonnull final Supplier<C> supplier) {
        return supplier.get();
    }
    
    @Override
    public <A, R> R collect(final Collector<? super T, A, R> collector) {
        return collector.finisher().apply(collector.supplier().get());
    }
    
    @Override
    public <R> R collect(final Supplier<R> supplier, final BiConsumer<R, ? super T> accumulator, final BiConsumer<R, R> combiner) {
        return supplier.get();
    }
    
    @Override
    public <U> U reduce(final U identity, final BiFunction<U, ? super T, U> accumulator, final BinaryOperator<U> combiner) {
        return identity;
    }
    
    @Override
    public Optional<T> reduce(final BinaryOperator<T> accumulator) {
        return Optional.empty();
    }
    
    @Override
    public T reduce(final T identity, final BinaryOperator<T> accumulator) {
        return identity;
    }
    
    @Override
    public boolean anyMatch(final Predicate<? super T> predicate) {
        return false;
    }
    
    @Override
    public boolean allMatch(final Predicate<? super T> predicate) {
        return true;
    }
    
    @Override
    public boolean noneMatch(final Predicate<? super T> predicate) {
        return true;
    }
    
    @Override
    public Optional<T> min(final Comparator<? super T> comparator) {
        return Optional.empty();
    }
    
    @Override
    public Optional<T> max(final Comparator<? super T> comparator) {
        return Optional.empty();
    }
    
    @Nonnull
    @Override
    public Set<Long> keys() {
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
