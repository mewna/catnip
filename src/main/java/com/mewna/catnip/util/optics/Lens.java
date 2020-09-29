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

import lombok.Getter;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A full lens, allowing for both views and mutations on the target object.
 *
 * @author amy
 * @since 9/29/20.
 */
public class Lens<T, V> extends PartialLens<T, V> {
    @Getter
    private final BiFunction<T, V, T> setter;
    
    public Lens(@Nonnull final Function<T, V> getter, @Nonnull final BiFunction<T, V, T> setter) {
        super(getter);
        this.setter = setter;
    }
    
    @Nonnull
    public Function<T, T> modify(@Nonnull final Function<V, V> modifier) {
        return old -> {
            final var view = get(old);
            final var transformed = modifier.apply(view);
            return setter.apply(old, transformed);
        };
    }
    
    @Nonnull
    public Function<Function<V, V>, T> modify(@Nonnull final T old) {
        return modifier -> {
            final var view = get(old);
            final var transformed = modifier.apply(view);
            return setter.apply(old, transformed);
        };
    }
    
    @Nonnull
    public <C> Lens<T, C> compose(@Nonnull final Lens<V, C> lens) {
        return new Lens<>(base -> lens.get(get(base)), (base, patch) -> {
            final var modifiedView = lens.modify(__ -> patch).apply(get(base));
            return setter.apply(base, modifiedView);
        });
    }
    
    @Nonnull
    public static <A, B> Lens<A, B> of(@Nonnull final Function<A, B> getter, @Nonnull final BiFunction<A, B, A> setter) {
        return new Lens<>(getter, setter);
    }
    
    @Nonnull
    public T set(@Nonnull final T target, @Nonnull final V value) {
        return modify(__ -> value).apply(target);
    }
    
    @Nonnull
    public Function<V, T> set(@Nonnull final T target) {
        return patch -> modify(__ -> patch).apply(target);
    }
    
    @Nonnull
    public T setFromSelf(@Nonnull final T target) {
        return set(target, Objects.requireNonNull(get(target)));
    }
}
