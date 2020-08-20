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

package com.mewna.catnip.entity;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.serialization.EntitySerializer;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * A single entity in catnip. An entity is any data received from Discord that
 * can be serialized or deserialized, as well as a class that must have a
 * catnip instance attached to it.
 *
 * @author natanbc
 * @since 5/9/18.
 */
@SuppressWarnings("InterfaceMayBeAnnotatedFunctional")
public interface Entity {
    /**
     * Deserialize the given data into an entity of the given type, with the
     * provided catnip instanced attached to it.
     *
     * @param catnip The catnip instance to attach.
     * @param type   The type of entity to deserialize to.
     * @param data   The data to be deserialized.
     * @param <T>    The type of data being deserialized.
     * @param <E>    The type of entity being deserialized.
     *
     * @return The deserialized entity.
     */
    @Nonnull
    @CheckReturnValue
    @SuppressWarnings("unchecked")
    static <T, E extends Entity> E deserialize(@Nonnull final Catnip catnip, @Nonnull final Class<E> type,
                                               @Nonnull final T data) {
        return ((EntitySerializer<T>) catnip.entitySerializer()).deserialize(data, type);
    }
    
    /**
     * Returns the catnip instance associated with this entity.
     *
     * @return The catnip instance of this entity.
     */
    Catnip catnip();
    
    /**
     * Serialize this entity. Returns whatever type of data the configured
     * serializer returns -- not necessarily JSON!
     *
     * @param <T> The type of serialized data.
     *
     * @return The serialized data.
     */
    @Nonnull
    @CheckReturnValue
    @SuppressWarnings("unchecked")
    default <T> T serialize() {
        return (T) catnip().entitySerializer().serialize(this);
    }
}
