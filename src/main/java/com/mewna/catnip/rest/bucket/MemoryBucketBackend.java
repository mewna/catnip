package com.mewna.catnip.rest.bucket;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author amy
 * @since 10/7/18.
 */
public class MemoryBucketBackend implements BucketBackend {
    private final Map<String, Container> buckets = new ConcurrentHashMap<>();
    
    @Nonnull
    @Override
    public BucketBackend limit(@Nonnull final String route, @Nonnegative final long value) {
        final Container container = buckets.computeIfAbsent(route, __ -> new Container());
        container.limit(value);
        return this;
    }
    
    @Nonnegative
    @Override
    public long limit(@Nonnull final String route) {
        return buckets.computeIfAbsent(route, __ -> new Container()).limit();
    }
    
    @Nonnull
    @Override
    public BucketBackend remaining(@Nonnull final String route, @Nonnegative final long value) {
        final Container container = buckets.computeIfAbsent(route, __ -> new Container());
        container.remaining(value);
        return this;
    }
    
    @Nonnegative
    @Override
    public long remaining(@Nonnull final String route) {
        return buckets.computeIfAbsent(route, __ -> new Container()).remaining();
    }
    
    @Nonnull
    @Override
    public BucketBackend reset(@Nonnull final String route, @Nonnegative final long value) {
        final Container container = buckets.computeIfAbsent(route, __ -> new Container());
        container.reset(value);
        return this;
    }
    
    @Nonnegative
    @Override
    public long reset(@Nonnull final String route) {
        return buckets.computeIfAbsent(route, __ -> new Container()).reset();
    }
}
