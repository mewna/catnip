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

package com.mewna.catnip.rest.bucket;

import com.mewna.catnip.util.SafeVertxCompletableFuture;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author amy
 * @since 10/7/18.
 */
public class MemoryBucketBackend implements BucketBackend {
    private final Map<String, BucketContainer> buckets = new ConcurrentHashMap<>();
    
    @Nonnull
    @Override
    public CompletableFuture<BucketBackend> limit(@Nonnull final String route, @Nonnegative final long value) {
        final BucketContainer container = buckets.computeIfAbsent(route, __ -> new BucketContainer());
        container.limit(value);
        return SafeVertxCompletableFuture.completedFuture(this);
    }
    
    @Nonnegative
    @Override
    public CompletableFuture<Long> limit(@Nonnull final String route) {
        return SafeVertxCompletableFuture.completedFuture(buckets.computeIfAbsent(route, __ -> new BucketContainer()).limit());
    }
    
    @Nonnull
    @Override
    public CompletableFuture<BucketBackend> remaining(@Nonnull final String route, @Nonnegative final long value) {
        final BucketContainer container = buckets.computeIfAbsent(route, __ -> new BucketContainer());
        container.remaining(value);
        return SafeVertxCompletableFuture.completedFuture(this);
    }
    
    @Nonnegative
    @Override
    public CompletableFuture<Long> remaining(@Nonnull final String route) {
        return SafeVertxCompletableFuture.completedFuture(buckets.computeIfAbsent(route, __ -> new BucketContainer()).remaining());
    }
    
    @Nonnull
    @Override
    public CompletableFuture<BucketBackend> reset(@Nonnull final String route, @Nonnegative final long value) {
        final BucketContainer container = buckets.computeIfAbsent(route, __ -> new BucketContainer());
        container.reset(value);
        return SafeVertxCompletableFuture.completedFuture(this);
    }
    
    @Nonnegative
    @Override
    public CompletableFuture<Long> reset(@Nonnull final String route) {
        return SafeVertxCompletableFuture.completedFuture(buckets.computeIfAbsent(route, __ -> new BucketContainer()).reset());
    }
    
    @Nonnull
    @Override
    public CompletableFuture<BucketBackend> latency(@Nonnull final String route, final long value) {
        final BucketContainer container = buckets.computeIfAbsent(route, __ -> new BucketContainer());
        container.latency(value);
        return SafeVertxCompletableFuture.completedFuture(this);
    }
    
    @Override
    public CompletableFuture<Long> latency(@Nonnull final String route) {
        return SafeVertxCompletableFuture.completedFuture(buckets.computeIfAbsent(route, __ -> new BucketContainer()).latency());
    }
    
    @Nonnull
    @Override
    public CompletableFuture<BucketBackend> lastRequest(@Nonnull final String route, final long time) {
        final BucketContainer container = buckets.computeIfAbsent(route, __ -> new BucketContainer());
        container.lastRequest(time);
        return SafeVertxCompletableFuture.completedFuture(this);
    }
    
    @Override
    public CompletableFuture<Long> lastRequest(@Nonnull final String route) {
        return SafeVertxCompletableFuture.completedFuture(buckets.computeIfAbsent(route, __ -> new BucketContainer()).lastRequest());
    }
}
