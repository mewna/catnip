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

package com.mewna.catnip;

import com.google.common.collect.ImmutableSet;
import com.mewna.catnip.cache.CacheFlag;
import com.mewna.catnip.cache.EntityCacheWorker;
import com.mewna.catnip.cache.MemoryEntityCache;
import com.mewna.catnip.entity.user.Presence;
import com.mewna.catnip.internal.logging.DefaultLogAdapter;
import com.mewna.catnip.internal.logging.LogAdapter;
import com.mewna.catnip.internal.ratelimit.MemoryRatelimiter;
import com.mewna.catnip.internal.ratelimit.Ratelimiter;
import com.mewna.catnip.rest.bucket.BucketBackend;
import com.mewna.catnip.rest.bucket.MemoryBucketBackend;
import com.mewna.catnip.shard.event.CachingBuffer;
import com.mewna.catnip.shard.event.EventBuffer;
import com.mewna.catnip.shard.manager.DefaultShardManager;
import com.mewna.catnip.shard.manager.ShardManager;
import com.mewna.catnip.shard.session.DefaultSessionManager;
import com.mewna.catnip.shard.session.SessionManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import okhttp3.OkHttpClient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Set;

/**
 * @author amy
 * @since 9/25/18.
 */
@Getter
@Setter
@Accessors(fluent = true, chain = true)
@RequiredArgsConstructor
@SuppressWarnings("OverlyCoupledClass")
public final class CatnipOptions {
    @Nonnull
    private final String token;
    @Nonnull
    private ShardManager shardManager = new DefaultShardManager();
    @Nonnull
    private SessionManager sessionManager = new DefaultSessionManager();
    @Nonnull
    private Ratelimiter gatewayRatelimiter = new MemoryRatelimiter();
    @Nonnull
    private LogAdapter logAdapter = new DefaultLogAdapter();
    @Nonnull
    private EventBuffer eventBuffer = new CachingBuffer();
    @Nonnull
    private EntityCacheWorker cacheWorker = new MemoryEntityCache();
    @Nonnull
    private Set<CacheFlag> cacheFlags = EnumSet.noneOf(CacheFlag.class);
    @Nonnull
    private BucketBackend restBucketBackend = new MemoryBucketBackend();
    private boolean chunkMembers = true;
    private boolean emitEventObjects = true;
    @Nullable
    private Presence presence;
    @Nonnull
    private Set<String> disabledEvents = ImmutableSet.of();
    @Nonnull
    private OkHttpClient restHttpClient = new OkHttpClient();
}
