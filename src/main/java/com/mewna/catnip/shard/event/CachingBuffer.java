package com.mewna.catnip.shard.event;

import io.vertx.core.json.JsonObject;

/**
 * An implementation of {@link EventBuffer} used for the case of caching all
 * guilds sent in the {@code READY} payload. This doc needs to be filled out
 * way more than this eventually.
 *
 * @author amy
 * @since 9/9/18.
 */
public class CachingBuffer extends AbstractBuffer {
    @Override
    public void buffer(final JsonObject event) {
    
    }
}
