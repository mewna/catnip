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

package com.mewna.catnip.util.scheduler;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * @author amy
 * @since 7/29/19.
 */
@RequiredArgsConstructor
public class RxTaskScheduler extends AbstractTaskScheduler {
    private final AtomicLong idCounter = new AtomicLong(0);
    private final Map<Long, Disposable> tasks = new ConcurrentHashMap<>();
    
    @Override
    public long setTimer(final long ms, @Nonnull final Consumer<Long> task) {
        final var id = idCounter.getAndIncrement();
        final var disposable = Observable.timer(ms, TimeUnit.MILLISECONDS)
                .observeOn(catnip().rxScheduler())
                .subscribeOn(catnip().rxScheduler())
                .forEach(l -> {
                    // Once the task is running, cancellation shouldn't(?) have any effect
                    tasks.remove(id);
                    task.accept(l);
                });
        tasks.put(id, disposable);
        return id;
    }
    
    @Override
    public long setInterval(final long ms, @Nonnull final Consumer<Long> task) {
        final var id = idCounter.getAndIncrement();
        final var disposable = Observable.interval(ms, TimeUnit.MILLISECONDS)
                .observeOn(catnip().rxScheduler())
                .subscribeOn(catnip().rxScheduler())
                .forEach(task::accept);
        tasks.put(id, disposable);
        return id;
    }
    
    @Override
    public boolean cancel(final long id) {
        if (tasks.containsKey(id)) {
            final var disposable = tasks.get(id);
            tasks.remove(id);
            disposable.dispose();
            return true;
        } else {
            return false;
        }
    }
}
