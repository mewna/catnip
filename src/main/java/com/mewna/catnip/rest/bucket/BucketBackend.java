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

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

/**
 * @author amy
 * @since 10/7/18.
 */
@SuppressWarnings("UnusedReturnValue")
public interface BucketBackend {
    @Nonnull
    CompletableFuture<BucketBackend> limit(@Nonnull String route, @Nonnegative long value);
    
    @Nonnegative
    CompletableFuture<Long> limit(@Nonnull String route);
    
    @Nonnull
    CompletableFuture<BucketBackend> remaining(@Nonnull String route, @Nonnegative long value);
    
    @Nonnegative
    CompletableFuture<Long> remaining(@Nonnull String route);
    
    @Nonnull
    CompletableFuture<BucketBackend> reset(@Nonnull String route, @Nonnegative long value);
    
    @Nonnegative
    CompletableFuture<Long> reset(@Nonnull String route);
    
    @Nonnull
    CompletableFuture<BucketBackend> latency(@Nonnull String route, @Nonnegative long value);
    
    @Nonnegative
    CompletableFuture<Long> latency(@Nonnull String route);
    
    @Nonnull
    CompletableFuture<BucketBackend> lastRequest(@Nonnull String route, @Nonnegative long time);
    
    @Nonnegative
    CompletableFuture<Long> lastRequest(@Nonnull String route);
    
    @Getter
    @Setter
    @Accessors(fluent = true)
    @SuppressWarnings("FieldMayBeFinal")
    @NoArgsConstructor
    final class BucketContainer {
        // By default, we pretend we have 1 request left in a 5-limit bucket.
        // This is done so that it'll immediately update from the headers on
        // the next request
        private long limit = 5;
        private long remaining = 1;
        private long reset = System.currentTimeMillis() - 1L;
        private long latency;
        private long lastRequest;
    }
}
