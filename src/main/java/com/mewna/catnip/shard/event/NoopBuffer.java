package com.mewna.catnip.shard.event;

import io.vertx.core.json.JsonObject;

/**
 * A no-op implementation of {@link EventBuffer}. The no-op buffer simply
 * passes all incoming events to the event bus, without any processing or
 * buffering (eg. for caching). This is mainly useful for the case of writing a
 * "stateless" (ie. cacheless) bot.
 *
 * @author amy
 * @since 9/9/18.
 */
public class NoopBuffer extends AbstractBuffer {
    @Override
    public void buffer(final JsonObject event) {
        emitter().emit(event);
    }
}
