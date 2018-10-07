package com.mewna.catnip.extension;

import com.google.common.collect.ImmutableSet;
import com.mewna.catnip.Catnip;
import com.mewna.catnip.extension.hook.CatnipHook;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.impl.ConcurrentHashSet;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Set;

/**
 * @author amy
 * @since 9/6/18
 */
@Accessors(fluent = true, chain = true)
@SuppressWarnings("WeakerAccess")
@RequiredArgsConstructor
public abstract class AbstractExtension extends AbstractVerticle implements Extension {
    @Getter
    private final String name;
    private final Collection<CatnipHook> hooks = new ConcurrentHashSet<>();
    @Getter
    @Setter
    private Catnip catnip;
    
    @Override
    public Extension registerHook(@Nonnull final CatnipHook hook) {
        hooks.add(hook);
        return this;
    }
    
    @Override
    public Extension unregisterHook(@Nonnull final CatnipHook hook) {
        hooks.remove(hook);
        return this;
    }
    
    @Override
    public Set<CatnipHook> hooks() {
        return ImmutableSet.copyOf(hooks);
    }
}
