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

import com.mewna.catnip.cache.CacheFlag;
import com.mewna.catnip.cache.EntityCacheWorker;
import com.mewna.catnip.cache.SplitMemoryEntityCache;
import com.mewna.catnip.entity.guild.Guild;
import com.mewna.catnip.entity.lifecycle.HighWebsocketLatency;
import com.mewna.catnip.entity.lifecycle.MemberChunkRerequest;
import com.mewna.catnip.entity.serialization.DefaultEntitySerializer;
import com.mewna.catnip.entity.serialization.EntitySerializer;
import com.mewna.catnip.entity.user.Presence;
import com.mewna.catnip.extension.Extension;
import com.mewna.catnip.rest.ratelimit.DefaultRateLimiter;
import com.mewna.catnip.rest.requester.Requester;
import com.mewna.catnip.rest.requester.SerialRequester;
import com.mewna.catnip.shard.CompressionMode;
import com.mewna.catnip.shard.DiscordEvent.Raw;
import com.mewna.catnip.shard.buffer.CachingBuffer;
import com.mewna.catnip.shard.buffer.EventBuffer;
import com.mewna.catnip.shard.buffer.NoopBuffer;
import com.mewna.catnip.shard.event.DefaultDispatchManager;
import com.mewna.catnip.shard.event.DispatchManager;
import com.mewna.catnip.shard.manager.DefaultShardManager;
import com.mewna.catnip.shard.manager.ShardManager;
import com.mewna.catnip.shard.ratelimit.MemoryRatelimiter;
import com.mewna.catnip.shard.ratelimit.Ratelimiter;
import com.mewna.catnip.shard.session.DefaultSessionManager;
import com.mewna.catnip.shard.session.SessionManager;
import com.mewna.catnip.util.CatnipOptionsView;
import com.mewna.catnip.util.logging.DefaultLogAdapter;
import com.mewna.catnip.util.logging.LogAdapter;
import com.mewna.catnip.util.rx.RxHelpers;
import com.mewna.catnip.util.scheduler.RxTaskScheduler;
import com.mewna.catnip.util.scheduler.TaskScheduler;
import io.reactivex.Scheduler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.http.HttpClient;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author amy
 * @since 9/25/18.
 */
@Getter
@Setter
@Accessors(fluent = true, chain = true)
@RequiredArgsConstructor
@SuppressWarnings("OverlyCoupledClass")
public final class CatnipOptions implements CatnipOptionsView, Cloneable {
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
    private EntityCacheWorker cacheWorker = new SplitMemoryEntityCache();
    @Nonnull
    private Set<CacheFlag> cacheFlags = EnumSet.noneOf(CacheFlag.class);
    @Nonnull
    private DispatchManager dispatchManager = new DefaultDispatchManager();
    private boolean chunkMembers = true;
    private boolean emitEventObjects = true;
    private boolean enforcePermissions = true;
    @Nullable
    private Presence initialPresence;
    @Nonnull
    private Set<String> disabledEvents = Set.of();
    @Nonnull
    private Requester requester = new SerialRequester(new DefaultRateLimiter(), HttpClient.newBuilder());
    private boolean logExtensionOverrides = true;
    private boolean validateToken = true;
    private boolean captureRestStacktraces = true;
    private boolean logUncachedPresenceWhenNotChunking = true;
    private boolean enableGuildSubscriptions = true;
    private long memberChunkTimeout = TimeUnit.SECONDS.toMillis(10);
    private Scheduler rxScheduler = RxHelpers.FORK_JOIN_SCHEDULER;
    private boolean logLifecycleEvents = true;
    private boolean manualChunkRerequesting;
    private int largeThreshold = 250;
    private TaskScheduler taskScheduler = new RxTaskScheduler();
    private HttpClient httpClient = HttpClient.newBuilder()
            .executor(RxHelpers.FORK_JOIN_POOL)
            .build();
    private CompressionMode compressionMode = CompressionMode.ZLIB;
    private boolean restRatelimitsWithoutClockSync;
    private long highLatencyThreshold = TimeUnit.SECONDS.toNanos(10);
    private EntitySerializer<?> entitySerializer = new DefaultEntitySerializer();
    
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch(final CloneNotSupportedException e) {
            throw new IllegalStateException("Couldn't clone!", e);
        }
    }
}
