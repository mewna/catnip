/*
 * Copyright (c) 2019 amy, All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *  3. Neither the name of the copyright holder nor the names of its contributors
 *     may be used to endorse or promote products derived from this software without
 *     specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 *  FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 *  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *  CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.mewna.catnip.shard.event;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.shard.GatewayIntent;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler.Worker;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

public class DefaultDispatchManager extends AbstractDispatchManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDispatchManager.class);
    private final Map<String, Set<DefaultMessageConsumer<?>>> consumers = new ConcurrentHashMap<>();
    private Worker worker;
    
    @Override
    public void catnip(final Catnip catnip) {
        super.catnip(catnip);
        worker = catnip().rxScheduler().createWorker();
    }
    
    @Override
    public void dispatchEvent(final String address, final Object event) {
        final var addressConsumers = consumers.get(address);
        
        if(addressConsumers != null) {
            if(worker == null) {
                LOGGER.warn("Please configure the Catnip parent instance before dispatching events.");
                throw new IllegalStateException("dispatchEvent was called before DefaultDispatchManager was configured.");
            }
            worker.schedule(() -> addressConsumers.forEach(c -> c.handle(event)));
        }
    }
    
    @Override
    public <T> MessageConsumer<T> createConsumer(final String address) {
        // Nullability concerns are not met during tests
        //noinspection ConstantConditions
        if(catnip() != null && catnip().options() != null) {
            if(catnip().options().logEventNotInIntentsWarning() && (catnip().options().apiVersion() > 6
                    || !catnip().options().intents().isEmpty())) {
                final var intents = catnip().options().intents();
                if(GatewayIntent.ALL_INTENT_EVENTS.contains(address)
                        && intents.stream()
                        .map(GatewayIntent::events)
                        .flatMap(Collection::stream)
                        .noneMatch(address::equals)) {
                    catnip().logAdapter().warn("Listening for event `{}`, but current intents disallow this!",
                            address);
                    catnip().logAdapter().warn("If you know what you're doing, you can suppress this message with the `logEventNotInIntentsWarning` option.");
                }
            }
        }
        final var consumer = new DefaultMessageConsumer<T>(address);
        consumers.computeIfAbsent(address, __ -> new CopyOnWriteArraySet<>()).add(consumer);
        return consumer;
    }
    
    @Override
    public void close() {
        worker.dispose();
        worker = null;
    }
    
    @Getter
    @Accessors(fluent = true)
    @RequiredArgsConstructor
    class DefaultMessageConsumer<T> implements MessageConsumer<T> {
        private final String address;
        
        private Consumer<T> internalHandler;
        
        @SuppressWarnings("unchecked")
        void handle(final Object event) {
            if(internalHandler != null) {
                internalHandler.accept((T) event);
            }
        }
        
        // This method is specific to Catnip#on and can be safely removed if wanted.
        @Override
        public MessageConsumer<T> handler(final Consumer<T> handler) {
            internalHandler = event -> worker.schedule(() -> {
                try {
                    handler.accept(event);
                } catch(final Exception e) {
                    LOGGER.error("Exception handling event on address " + address, e);
                }
            });
            return this;
        }
        
        @Override
        public Observable<T> asObservable() {
            return Observable.create(emitter -> {
                internalHandler = emitter::onNext;
                emitter.setCancellable(this::close);
            });
        }
        
        @Override
        public Flowable<T> asFlowable(final BackpressureStrategy backpressureStrategy) {
            return Flowable.create(emitter -> {
                internalHandler = emitter::onNext;
                emitter.setCancellable(this::close);
            }, backpressureStrategy);
        }
        
        @Override
        public void close() {
            final var addressConsumers = consumers.get(address);
            
            if(addressConsumers != null) {
                addressConsumers.remove(this);
            }
        }
    }
}
