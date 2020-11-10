/*
 * Copyright (c) 2018 amy, All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.mewna.catnip.rest.guild;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * An updater for positions of things like roles and channels.
 * <p/>
 * Consider a set of channels like the following:
 * <pre>
 * {@code
 * #channel (id 123, pos 0)
 * #channel (id 125, pos 1)
 * #channel (id 193, pos 2)
 * }
 * </pre>
 * Suppose you wanted to change the order from {@code (123, 125, 193)} to
 * {@code (193, 123, 125)}. You could accomplish this with something like:
 * <pre>
 * {@code
 * updater
 *     .select("123").position(1)
 *     .select("125").position(2)
 *     .select("193").position(0);
 * }
 * </pre>
 * which, in action, looks something like:
 * <pre>
 * {@code
 * 0.    123 ----|
 * 1. |- 125     |
 * 2. |  193 -   |
 *    |      |   |
 * 0. |  193 <   |
 * 1. |  123 <---|
 * 2. |> 125
 * }
 * </pre>
 *
 *
 * Or alternatively:
 * <pre>
 * {@code
 * // Add all channels
 * updater
 *     .select("123").position(0)
 *     .select("125").position(0)
 *     .select("193").position(0);
 *
 * // Relative movements
 * updater
 *     .select("123").increment()
 *     .select("125").increment()
 *     .select("125").increment();
 * }
 * </pre>
 * When objects have the same position, Discord will sort them by their
 * snowflake. So the second example looks something like:
 * <pre>
 * {@code
 * // Initial state
 * 0.  123
 * 1.  125
 * 2.  193
 *
 * // All channels added to updater
 * // Remember that they're sorted by snowflake
 * 0.  123
 * 0.  125
 * 0.  193
 *
 * // .select("123").increment()
 * 0.  123 --|
 * 0.  125   |
 * 0.  193   |
 *           |
 * 0.  125   |
 * 0.  193   |
 * 1.  123 <-|
 *
 * // .select("125").increment()
 * 0.  125 --|
 * 0.  193   |
 * 1.  123   |
 *           |
 * 0.  193   |
 * 1.  123   |
 * 1.  125 <-|
 *
 * // .select("125").increment()
 * 0.  193
 * 1.  123
 * 1.  125 --|
 *           |
 * 0.  193   |
 * 1.  123   |
 * 2.  125 <-|
 * }
 * </pre>
 * While in this example, and in many real-world examples, we could just leave
 * the positions the same and let snowflake-sorting figure it out, it's most
 * likely better overall to have proper positions specified, so that the
 * snowflake-sorting doesn't surprise you.
 */
@Getter
@RequiredArgsConstructor
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
        if(entityId == null) {
            throw new IllegalStateException("No entity selected!");
        }
        positions.put(entityId, position);
        return this;
    }
    
    @Nonnull
    @CheckReturnValue
    public PositionUpdater increment() {
        if(entityId == null) {
            throw new IllegalStateException("No entity selected");
        }
        // not using computeIfAbsent because extra write needed otherwise
        final Integer old = positions.get(entityId);
        if(old == null) {
            positions.put(entityId, positions.size() - 1);
            return this;
        }
        positions.put(entityId, reverseOrder ? old - 1 : old + 1);
        return this;
    }
    
    @Nonnull
    @CheckReturnValue
    public PositionUpdater decrement() {
        if(entityId == null) {
            throw new IllegalStateException("No entity selected!");
        }
        final Integer old = positions.get(entityId);
        if(old == null) {
            positions.put(entityId, positions.size() - 1);
            return this;
        }
        positions.put(entityId, reverseOrder ? old + 1 : old - 1);
        return this;
    }
    
    @Nonnull
    @CheckReturnValue
    public Collection<String> entityIds() {
        return Set.copyOf(positions.keySet());
    }
    
    @Nonnull
    @CheckReturnValue
    public Collection<Integer> positions() {
        return Set.copyOf(positions.values());
    }
    
    @Nonnull
    @CheckReturnValue
    public Collection<Entry<String, Integer>> entries() {
        return Set.copyOf(positions.entrySet());
    }
}
