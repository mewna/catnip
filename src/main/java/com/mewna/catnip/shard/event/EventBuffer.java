package com.mewna.catnip.shard.event;

import com.mewna.catnip.Catnip;
import io.vertx.core.json.JsonObject;

/**
 * Used for buffering events for things like caching.
 *
 * @author amy
 * @since 9/9/18.
 */
@SuppressWarnings("InterfaceMayBeAnnotatedFunctional")
public interface EventBuffer {
    /**
     * Buffers a single event.
     *
     * @param event The event to buffer.
     */
    void buffer(JsonObject event);
    
    void catnip(Catnip catnip);
}
