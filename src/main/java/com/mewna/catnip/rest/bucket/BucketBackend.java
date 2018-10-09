package com.mewna.catnip.rest.bucket;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * @author amy
 * @since 10/7/18.
 */
@SuppressWarnings("UnusedReturnValue")
public interface BucketBackend {
    @Nonnull
    BucketBackend limit(@Nonnull String route, @Nonnegative long value);
    
    @Nonnegative
    long limit(@Nonnull String route);
    
    @Nonnull
    BucketBackend remaining(@Nonnull String route, @Nonnegative long value);
    
    @Nonnegative
    long remaining(@Nonnull String route);
    
    @Nonnull
    BucketBackend reset(@Nonnull String route, @Nonnegative long value);
    
    @Nonnegative
    long reset(@Nonnull String route);
    
    @Getter
    @Setter
    @Accessors(fluent = true)
    @SuppressWarnings("FieldMayBeFinal")
    @NoArgsConstructor
    final class Container {
        // By default, we pretend we have 1 request left in a 5-limit bucket.
        // This is done so that it'll immediately update from the headers on
        // the next request
        private long limit = 5;
        private long remaining = 1;
        private long reset = System.currentTimeMillis() - 1L;
    }
}
