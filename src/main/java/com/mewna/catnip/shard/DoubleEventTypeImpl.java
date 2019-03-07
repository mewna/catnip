/*
 * Copyright (c) 2019 amy, All rights reserved.
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

package com.mewna.catnip.shard;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * @author amy
 * @since 3/7/19.
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public class DoubleEventTypeImpl<T, E> implements DoubleEventType<T, E> {
    private final String key;
    private final Class<T> left;
    private final Class<E> right;
    
    @Nonnull
    @Override
    public Pair<Class<T>, Class<E>> payloadClasses() {
        return ImmutablePair.of(left, right);
    }
    
    static <T, E> DoubleEventType<T, E> doubleEvent(@Nonnull final String key, @Nonnull final Class<T> left,
                                              @Nonnull final Class<E> right) {
        return new DoubleEventTypeImpl<>(key, left, right);
    }
    
    static DoubleEventType<Void, Void> doubleNotFired(@Nonnull final String key) {
        return new DoubleEventTypeImpl<Void, Void>(key, Void.class, Void.class) {
            @Nonnull
            @CheckReturnValue
            @Override
            public String key() {
                throw new UnsupportedOperationException("Event " + key + " is not implemented");
            }
        };
    }
}
