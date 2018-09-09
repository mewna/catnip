package com.mewna.catnip.shard.event;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.shard.DispatchEmitter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * @author amy
 * @since 9/9/18.
 */
@Accessors(fluent = true)
@SuppressWarnings("FieldCanBeLocal")
public abstract class AbstractBuffer implements EventBuffer {
    @Getter(AccessLevel.PROTECTED)
    private Catnip catnip;
    @Getter(AccessLevel.PROTECTED)
    private DispatchEmitter emitter;
    
    @Override
    public void catnip(final Catnip catnip) {
        this.catnip = catnip;
        emitter = new DispatchEmitter(catnip);
    }
}
