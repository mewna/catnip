package com.mewna.catnip.rest.guild;

import com.google.common.collect.ImmutableSet;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

@RequiredArgsConstructor
@Accessors(fluent = true, chain = true)
@Getter
@SuppressWarnings("unused")
public class PositionUpdater {
    private final String guildId;
    private final boolean reverseOrder;
    
    @Getter(AccessLevel.NONE)
    private final Map<String, Integer> positions = new HashMap<>(); // autoboxing >:(
    
    private String entityId;
    
    @Nonnull
    @CheckReturnValue
    public PositionUpdater select(@Nonnull final String entityId) {
        this.entityId = entityId;
        return this;
    }
    
    @Nonnull
    @CheckReturnValue
    public PositionUpdater position(@Nonnegative final int position) {
        if (entityId == null) {
            throw new IllegalStateException("No entity selected!");
        }
        positions.put(entityId, position);
        return this;
    }
    
    @Nonnull
    @CheckReturnValue
    public PositionUpdater increment() {
        if (entityId == null) {
            throw new IllegalStateException("No entity selected");
        }
        // not using computeIfAbsent because extra write needed otherwise
        final Integer old = positions.get(entityId);
        if (old == null) {
            positions.put(entityId, positions.size() - 1);
            return this;
        }
        positions.put(entityId, reverseOrder ? old - 1 : old + 1);
        return this;
    }
    
    @Nonnull
    @CheckReturnValue
    public PositionUpdater decrement() {
        if (entityId == null) {
            throw new IllegalStateException("No entity selected!");
        }
        final Integer old = positions.get(entityId);
        if (old == null) {
            positions.put(entityId, positions.size() - 1);
            return this;
        }
        positions.put(entityId, reverseOrder ? old + 1 : old - 1);
        return this;
    }
    
    @Nonnull
    @CheckReturnValue
    public Collection<String> channelIds() {
        return ImmutableSet.copyOf(positions.keySet());
    }
    
    @Nonnull
    @CheckReturnValue
    public Collection<Integer> positions() {
        return ImmutableSet.copyOf(positions.values());
    }
    
    @Nonnull
    @CheckReturnValue
    public Collection<Entry<String, Integer>> entries() {
        return ImmutableSet.copyOf(positions.entrySet());
    }
}
