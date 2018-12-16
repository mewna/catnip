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

import com.mewna.catnip.util.Utils;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.function.Function;

/**
 * Default {@link NamedCacheView NamedCacheView} implementation.
 *
 * @param <T> Type of the entity held by this cache.
 *
 * @author natanbc
 * @since 12/15/18
 */
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
