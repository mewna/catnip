package com.mewna.catnip.shard;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Marker for statically validating event types on handlers.
 *
 * @param <T> Type of the event fired.
 *
 * @author natanbc
 * @since 10/6/18.
 */
public interface EventType<T> {
    /**
     * Key used in the event bus.
     *
     * @return Key where this event is fired in the bus.
     */
    @Nonnull
    @CheckReturnValue
    String key();
    
    /**
     * Class of the event payload.
     *
     * @return Class of the payload fired for this event.
     */
    @Nonnull
    @CheckReturnValue
    Class<T> payloadClass();
}
