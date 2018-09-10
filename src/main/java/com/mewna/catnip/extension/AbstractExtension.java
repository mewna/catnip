package com.mewna.catnip.extension;

import com.mewna.catnip.Catnip;
import io.vertx.core.AbstractVerticle;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author amy
 * @since 9/6/18
 */
@Accessors(fluent = true, chain = true)
@SuppressWarnings("WeakerAccess")
@RequiredArgsConstructor
public abstract class AbstractExtension extends AbstractVerticle implements Extension {
    @Getter
    @Setter
    private Catnip catnip;
    
    @Getter
    private final String name;
}
