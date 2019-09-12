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

package com.mewna.catnip.extension.manager;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.extension.Extension;
import com.mewna.catnip.shard.event.MessageConsumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author amy
 * @since 9/6/18
 */
@Accessors(fluent = true)
@RequiredArgsConstructor
public class DefaultExtensionManager implements ExtensionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExtensionManager.class);
    
    @Getter
    private final Catnip catnip;
    private final Collection<Extension> loadedExtensions = ConcurrentHashMap.newKeySet();
    
    @Override
    public ExtensionManager loadExtension(@Nonnull final Extension extension) {
        if(!loadedExtensions.contains(extension)) {
            extension.catnip(catnip);
            loadedExtensions.add(extension);
            try {
                final var completable = extension.onLoaded();
                if(completable != null) {
                    final var result = completable.blockingGet();
                    if(result != null) {
                        throw result;
                    }
                }
            } catch(final Throwable e) {
                LOGGER.error("Extension " + extension + " threw an exception on loading.", e);
            }
        }
        return this;
    }
    
    @Override
    public ExtensionManager unloadExtension(@Nonnull final Extension extension) {
        if(loadedExtensions.contains(extension)) {
            loadedExtensions.remove(extension);
            try {
                extension.listeners().forEach(MessageConsumer::close);
                final var completable = extension.onUnloaded();
                if(completable != null) {
                    final var result = completable.blockingGet();
                    if(result != null) {
                        throw result;
                    }
                }
            } catch(final Throwable e) {
                LOGGER.error("Extension " + extension + " threw an exception on unloading.", e);
            }
        }
        return this;
    }
    
    @Nonnull
    @Override
    public Set<Extension> matchingExtensions(@Nonnull final String regex) {
        //small optimization
        final Pattern pattern = Pattern.compile(regex);
        return loadedExtensions.stream()
                .filter(e -> pattern.matcher(e.name()).matches())
                .collect(Collectors.toUnmodifiableSet());
    }
    
    @Nonnull
    @Override
    public <T extends Extension> Set<? extends T> matchingExtensions(@Nonnull final Class<T> extensionClass) {
        return loadedExtensions.stream()
                .filter(extensionClass::isInstance)
                .map(extensionClass::cast)
                .collect(Collectors.toUnmodifiableSet());
    }
    
    @Nonnull
    @Override
    public Set<Extension> extensions() {
        return Set.copyOf(loadedExtensions);
    }
    
    @Override
    public void shutdown() {
        loadedExtensions.forEach(this::unloadExtension);
    }
}
