package com.mewna.catnip.internal.ratelimit;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author amy
 * @since 8/16/18.
 */
public final class MemoryRatelimiter implements Ratelimiter {
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    
    static ImmutablePair<Boolean, Long> checkRatelimitInternal(final Map<String, Bucket> buckets, final String id,
                                                               final long period, final long limit) {
        final long now = System.currentTimeMillis();
        if(buckets.containsKey(id)) {
            final Bucket bucket = buckets.get(id);
            if(bucket.resetAt < now) { // handle reset
                bucket.remaining = bucket.limit; //TODO(shred): assert bucket.limit == limit for safety/sanity?
                bucket.resetAt = now + period;
            } else if(bucket.remaining <= 0) { //no permits available
                return ImmutablePair.of(true, 0L);
            }
            bucket.remaining--;
            return ImmutablePair.of(false, bucket.remaining);
        } else {
            buckets.put(id, new Bucket(limit, limit - 1, now + period));
            return ImmutablePair.of(false, limit - 1);
        }
    }
    
    @Override
    public ImmutablePair<Boolean, Long> checkRatelimit(final String id, final long periodMs, final long limit) {
        return checkRatelimitInternal(buckets, id, periodMs, limit);
    }
    
    @AllArgsConstructor
    static final class Bucket {
        private long limit;
        private long remaining;
        private long resetAt;
    }
}
