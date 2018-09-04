package com.mewna.catnip.internal.logging;

import lombok.experimental.Accessors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author amy
 * @since 9/3/18.
 */
@Accessors(fluent = true)
public class DefaultLogAdapter implements LogAdapter {
    @SuppressWarnings("StaticVariableOfConcreteClass")
    private static final MySecurityManager mySecurityManager = new MySecurityManager();
    
    @Override
    public void log(@Nonnull final Level level, @Nonnull final String message, @Nullable final Object... objects) {
        // TODO: Switch this to use StackWalker when eventually moving to J9+
        final Class<?> caller = mySecurityManager.getCallerClassName(3);
        final Logger logger = LoggerFactory.getLogger(caller);
        final FormattingTuple tuple = MessageFormatter.arrayFormat(message, objects);
        final String formatted = tuple.getMessage();
        if(logger != null) {
            switch(level) {
                case TRACE: {
                    logger.trace(formatted);
                    if(tuple.getThrowable() != null) {
                        logger.trace("Stacktrace: ", tuple.getThrowable());
                    }
                    break;
                }
                case DEBUG: {
                    logger.debug(formatted);
                    if(tuple.getThrowable() != null) {
                        logger.debug("Stacktrace: ", tuple.getThrowable());
                    }
                    break;
                }
                case INFO: {
                    logger.info(formatted);
                    if(tuple.getThrowable() != null) {
                        logger.info("Stacktrace: ", tuple.getThrowable());
                    }
                    break;
                }
                case WARN: {
                    logger.warn(formatted);
                    if(tuple.getThrowable() != null) {
                        logger.warn("Stacktrace: ", tuple.getThrowable());
                    }
                    break;
                }
                case ERROR: {
                    logger.error(formatted);
                    if(tuple.getThrowable() != null) {
                        logger.error("Stacktrace: ", tuple.getThrowable());
                    }
                    break;
                }
            }
        }
    }

    /**
     * A custom security manager that exposes the getClassContext() information.
     * This is an ugly hack and needs to be replaced with the StackWalker API
     * when we eventually move to J9+. See this StackOverflow question:
     * https://stackoverflow.com/a/2924426/
     */
    static class MySecurityManager extends SecurityManager {
        Class<?> getCallerClassName(@SuppressWarnings("SameParameterValue") final int callStackDepth) {
            return getClassContext()[callStackDepth];
        }
    }
}
