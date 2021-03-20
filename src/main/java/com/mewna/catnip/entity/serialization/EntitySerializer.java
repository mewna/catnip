/*
 * Copyright (c) 2019 amy, All rights reserved.
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

package com.mewna.catnip.entity.serialization;

import com.mewna.catnip.entity.Entity;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * A generic behaviour for de/serializing catnip entities. The default
 * implementation is {@link DefaultEntitySerializer}, <strong> which does
 * nothing but throw on method calls.</strong> You must provide a serializer
 * that properly handles de/serialization requests.
 *
 * @param <T> The type of the serialized data.
 *
 * @author amy
 * @since 9/12/19.
 */
public interface EntitySerializer<T> {
    /**
     * Serializes the given entity. This method should handle things like
     * converting snowflakes into strings, if the serialization method in use
     * (ex. JSON) doesn't support bigints.<br/>
     * <p>
     * It is <strong>strongly</strong> recommended that you add in
     * functionality to determine the catnip version that an entity was
     * serialized with, so as to avoid potential version mismatch errors.
     *
     * @param entity The entity to serialize.
     *
     * @return The serialized entity data.
     */
    @Nonnull
    @CheckReturnValue
    T serialize(@Nonnull final Entity entity);
    
    /**
     * Deserializes the given data into an entity of the right type. This
     * method should handle things like converting strings into snowflakes, if
     * the serialization method in use (ex. JSON) doesn't support bigints; that
     * is, it should effectively undo any data transformations done in
     * {@link #serialize(Entity)}.<br/>
     * <p>
     * It is <strong>strongly</strong> recommended that you add in
     * functionality to determine the catnip version that an entity was
     * serialized with, so as to avoid potential version mismatch errors.
     *
     * @param data The data to deserialize.
     * @param as   The type of entity to deserialize data into.
     * @param <E>  The type of the target entity.
     *
     * @return The entity deserialized from the given data.
     */
    @Nonnull
    @CheckReturnValue
    <E extends Entity> E deserialize(@Nonnull final T data, @Nonnull final Class<E> as);
}
