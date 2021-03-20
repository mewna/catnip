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

package com.mewna.catnip.extension;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.extension.hook.CatnipHook;
import com.mewna.catnip.shard.event.DoubleEventType;
import com.mewna.catnip.shard.event.EventType;
import com.mewna.catnip.shard.event.MessageConsumer;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author amy
 * @since 9/6/18
 */
@Accessors(fluent = true, chain = true)
@SuppressWarnings("WeakerAccess")
@RequiredArgsConstructor
public abstract class AbstractExtension implements Extension {
    @Getter
    private final String name;
    private final Collection<CatnipHook> hooks = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<MessageConsumer<?>> listeners = new CopyOnWriteArraySet<>();
    @Getter
    @Setter
    private Catnip catnip;
    
    @Override
    public Extension registerHook(@Nonnull final CatnipHook hook) {
        hooks.add(hook);
        return this;
    }
    
    @Override
    public Extension unregisterHook(@Nonnull final CatnipHook hook) {
        hooks.remove(hook);
        return this;
    }
    
    @Override
    public Set<CatnipHook> hooks() {
        return Set.copyOf(hooks);
    }
    
    private <T> MessageConsumer<T> on(@Nonnull final EventType<T> type) {
        final MessageConsumer<T> consumer = catnip().dispatchManager().createConsumer(type.key());
        listeners.add(consumer);
        return consumer;
    }
    
    private <T> MessageConsumer<T> on(@Nonnull final EventType<T> type, @Nonnull final Consumer<T> handler) {
        return on(type).handler(handler);
    }
    
    @Override
    public <T> Observable<T> observable(@Nonnull final EventType<T> type) {
        return on(type).asObservable().subscribeOn(catnip().rxScheduler()).observeOn(catnip().rxScheduler());
    }
    
    @Override
    public <T> Flowable<T> flowable(@Nonnull final EventType<T> type) {
        return on(type).asFlowable().subscribeOn(catnip().rxScheduler()).observeOn(catnip().rxScheduler());
    }
    
    private <T, E> MessageConsumer<Pair<T, E>> on(@Nonnull final DoubleEventType<T, E> type) {
        final MessageConsumer<Pair<T, E>> consumer = catnip().dispatchManager().createConsumer(type.key());
        listeners.add(consumer);
        return consumer;
    }
    
    private <T, E> MessageConsumer<Pair<T, E>> on(@Nonnull final DoubleEventType<T, E> type,
                                                 @Nonnull final BiConsumer<T, E> handler) {
        return on(type).handler(m -> handler.accept(m.getLeft(), m.getRight()));
    }
    
    @Override
    public <T, E> Observable<Pair<T, E>> observable(@Nonnull final DoubleEventType<T, E> type) {
        return on(type).asObservable().subscribeOn(catnip().rxScheduler()).observeOn(catnip().rxScheduler());
    }
    
    @Override
    public <T, E> Flowable<Pair<T, E>> flowable(@Nonnull final DoubleEventType<T, E> type) {
        return on(type).asFlowable().subscribeOn(catnip().rxScheduler()).observeOn(catnip().rxScheduler());
    }
}
