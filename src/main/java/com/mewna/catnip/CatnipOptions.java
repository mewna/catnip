package com.mewna.catnip;

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
    
}
