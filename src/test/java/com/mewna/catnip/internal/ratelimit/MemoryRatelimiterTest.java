package com.mewna.catnip.internal.ratelimit;

import com.mewna.catnip.internal.ratelimit.MemoryRatelimiter.Bucket;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author amy
 * @since 8/16/18.
 */
@SuppressWarnings("WeakerAccess")
public class MemoryRatelimiterTest {
    @Test
    public void testCheckRatelimitInternalWithBucket() {
        final Map<String, Bucket> test = new HashMap<>();
        
        final String key = "test";
        final long limit = 10;
        final long period = 500L;
        
        test.put(key, new Bucket(limit, limit, System.currentTimeMillis()));
        for(int i = 0; i < limit; i++) {
            final ImmutablePair<Boolean, Long> res = MemoryRatelimiter.checkRatelimitInternal(test, key, period, limit);
            assertEquals(false, res.left);
            assertEquals(limit - (i + 1), res.right.longValue());
        }
        try {
            Thread.sleep(period / 2);
        } catch(final InterruptedException e) {
            e.printStackTrace();
        }
        {
            final ImmutablePair<Boolean, Long> res = MemoryRatelimiter.checkRatelimitInternal(test, key, period, limit);
            assertEquals(true, res.left);
            assertEquals(0L, res.right.longValue());
        }
        try {
            Thread.sleep(period);
        } catch(final InterruptedException e) {
            e.printStackTrace();
        }
        {
            final ImmutablePair<Boolean, Long> res = MemoryRatelimiter.checkRatelimitInternal(test, key, period, limit);
            assertEquals(false, res.left);
            assertEquals(limit - 1, res.right.longValue());
        }
    }
    
    @Test
    public void testCheckRatelimitInternalWithoutBucket() {
        final Map<String, Bucket> test = new HashMap<>();
        
        final String key = "test";
        final long limit = 10;
        final long period = 500L;
        
        for(int i = 0; i < limit; i++) {
            final ImmutablePair<Boolean, Long> res = MemoryRatelimiter.checkRatelimitInternal(test, key, period, limit);
            assertEquals(false, res.left);
            assertEquals(limit - (i + 1), res.right.longValue());
        }
        try {
            Thread.sleep(period / 2);
        } catch(final InterruptedException e) {
            e.printStackTrace();
        }
        {
            final ImmutablePair<Boolean, Long> res = MemoryRatelimiter.checkRatelimitInternal(test, key, period, limit);
            assertEquals(true, res.left);
            assertEquals(0L, res.right.longValue());
        }
        try {
            Thread.sleep(period);
        } catch(final InterruptedException e) {
            e.printStackTrace();
        }
        {
            final ImmutablePair<Boolean, Long> res = MemoryRatelimiter.checkRatelimitInternal(test, key, period, limit);
            assertEquals(false, res.left);
            assertEquals(limit - 1, res.right.longValue());
        }
    }

    @Test
    public void testLateResetAt() {
        final Map<String, Bucket> test = new HashMap<>();

        final String key = "test";
        final long limit = 10;
        final long period = 500L;

        test.put(key, new Bucket(limit, limit, 1337));//NOTE(shred): old code moved from resetAt one period's worth
        for(int i = 0; i < limit; i++) {
            final ImmutablePair<Boolean, Long> res = MemoryRatelimiter.checkRatelimitInternal(test, key, period, limit);
            assertEquals(false, res.left);
        }
        final ImmutablePair<Boolean, Long> res = MemoryRatelimiter.checkRatelimitInternal(test, key, period, limit);
        assertEquals(true, res.left);
    }
}
