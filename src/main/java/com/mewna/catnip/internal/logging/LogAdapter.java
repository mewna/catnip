package com.mewna.catnip.internal.logging;

import org.slf4j.event.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.slf4j.event.Level.*;

/**
 * If, for some reason, you want to plug in your own logging framework that
 * ISN'T logback, SLF4J, ..., then you can implement this interface. Check out
 * {@link DefaultLogAdapter} for an example.
 *
 * @author amy
 * @since 9/3/18.
 */
@SuppressWarnings("InterfaceMayBeAnnotatedFunctional")
public interface LogAdapter {
    void log(@Nonnull Level level, @Nonnull String message, @Nullable Object... objects);
    
    default void trace(@Nonnull final String message, @Nullable final Object... objects) {
        log(TRACE, message, objects);
    }
    
    default void debug(@Nonnull final String message, @Nullable final Object... objects) {
        log(DEBUG, message, objects);
    }
    
    default void info(@Nonnull final String message, @Nullable final Object... objects) {
        log(INFO, message, objects);
    }
    
    default void warn(@Nonnull final String message, @Nullable final Object... objects) {
        log(WARN, message, objects);
    }
    
    default void error(@Nonnull final String message, @Nullable final Object... objects) {
        log(ERROR, message, objects);
    }
}
