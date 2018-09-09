package com.mewna.catnip.shard.event;

import com.mewna.catnip.Catnip;
import io.vertx.core.json.JsonObject;

/**
 * @author amy
 * @since 9/9/18.
 */
@SuppressWarnings("InterfaceMayBeAnnotatedFunctional")
public interface EventBuffer {
    void buffer(JsonObject event);
    
    void catnip(Catnip catnip);
}
