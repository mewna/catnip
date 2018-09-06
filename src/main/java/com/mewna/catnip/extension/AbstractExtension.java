package com.mewna.catnip.extension;

import io.vertx.core.AbstractVerticle;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author amy
 * @since 9/6/18
 */
@Accessors(fluent = true)
@SuppressWarnings("WeakerAccess")
@RequiredArgsConstructor
public abstract class AbstractExtension extends AbstractVerticle implements Extension {
    @Getter
    private final String name;
}
