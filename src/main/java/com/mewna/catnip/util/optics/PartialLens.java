/*
 * Copyright (c) 2020 amy, All rights reserved.
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

package com.mewna.catnip.util.optics;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.Entity;
import com.mewna.catnip.entity.delegate.EntityDelegator;
import com.mewna.catnip.entity.impl.EntityBuilder;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * A partial lens implementation, used for read-only views of objects.
 *
 * @param <T> The type being lensed upon.
 * @param <V> The view exposed by the lens.
 *
 * @author amy
 * @since 9/29/20.
 */
@SuppressWarnings("unchecked")
@RequiredArgsConstructor
public class PartialLens<T, V> {
    private final Function<T, V> getter;
    
    @Nonnull
    public static <A, B> PartialLens<A, B> of(@Nonnull final Function<A, B> getter) {
        return new PartialLens<>(getter);
    }
    
    @Nullable
    public V get(@Nonnull final T base) {
        return getter.apply(base);
    }
    
    @Nonnull
    public Optional<V> maybeGet(@Nonnull final T base) {
        return Optional.ofNullable(get(base));
    }
    
    @Nonnull
    public <C> PartialLens<T, C> compose(@Nonnull final PartialLens<V, C> lens) {
        return new PartialLens<>(base -> lens.getter.apply(getter.apply(base)));
    }
    
    @Nonnull
    public static <K, V, M extends Map<K, V>> PartialLens<M, V> lenseMap(@Nonnull final K key) {
        return of(map -> map.get(key));
    }
    
    @Nonnull
    public static <B extends Entity, V extends Entity, D extends V> PartialLens<B, D> lenseEntity(@Nonnull final Catnip catnip,
                                                                                             @Nonnull final Function<B, V> getter) {
        return of(entity -> {
            final var view = getter.apply(entity);
            return EntityBuilder.staticDelegate(catnip, (Class<V>) view.getClass(), view);
        });
    }
}
