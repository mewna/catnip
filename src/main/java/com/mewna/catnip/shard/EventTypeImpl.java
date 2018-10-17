package com.mewna.catnip.shard;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * @param <T> Type of the event.
 *
 * @author natanbc
 * @since 10/6/18.
 */
@Getter
@Accessors(fluent = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
class EventTypeImpl<T> implements EventType<T> {
    private final String key;
    private final Class<T> payloadClass;
    
    static <T> EventType<T> event(@Nonnull final String key, @Nonnull final Class<T> payloadClass) {
        return new EventTypeImpl<>(key, payloadClass);
    }
    
    static EventType<Void> notFired(@Nonnull final String key) {
        return new EventTypeImpl<Void>(key, Void.class) {
            @Nonnull
            @CheckReturnValue
            @Override
            public String key() {
                throw new UnsupportedOperationException("Event " + key + " is not implemented");
            }
        };
    }
}
