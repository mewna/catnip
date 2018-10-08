package com.mewna.catnip.rest.bucket;

/**
 * @author amy
 * @since 10/7/18.
 */
public interface BucketBackend {
    BucketBackend limit(String route, long value);
    long limit(String route);
    
    BucketBackend remaining(String route, long value);
    long remaining(String route);
    
    BucketBackend reset(String route, long value);
    long reset(String route);
}
