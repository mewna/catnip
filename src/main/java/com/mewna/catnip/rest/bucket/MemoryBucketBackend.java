package com.mewna.catnip.rest.bucket;

import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author amy
 * @since 10/7/18.
 */
public class MemoryBucketBackend implements BucketBackend {
    private final Map<String, Container> buckets = new ConcurrentHashMap<>();
    
    @Override
    public BucketBackend limit(final String route, final long value) {
        final Container container = buckets.computeIfAbsent(route, __ -> new Container());
        container.limit = value;
        return this;
    }
    
    @Override
    public long limit(final String route) {
        return buckets.computeIfAbsent(route, __ -> new Container()).limit;
    }
    
    @Override
    public BucketBackend remaining(final String route, final long value) {
        final Container container = buckets.computeIfAbsent(route, __ -> new Container());
        container.remaining = value;
        return this;
    }
    
    @Override
    public long remaining(final String route) {
        return buckets.computeIfAbsent(route, __ -> new Container()).remaining;
    }
    
    @Override
    public BucketBackend reset(final String route, final long value) {
        final Container container = buckets.computeIfAbsent(route, __ -> new Container());
        container.reset = value;
        return this;
    }
    
    @Override
    public long reset(final String route) {
        return buckets.computeIfAbsent(route, __ -> new Container()).reset;
    }
    
    @NoArgsConstructor
    private static final class Container {
        private long limit;
        private long remaining;
        private long reset;
    }
}
