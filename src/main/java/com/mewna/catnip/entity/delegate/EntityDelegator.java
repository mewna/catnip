/*
 * Copyright (c) 2020 amy, All rights reserved.
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

package com.mewna.catnip.entity.delegate;

import javax.annotation.Nonnull;

/**
 * An entity delegator allows for delegating entities to a custom class. A
 * delegated entity is created such that the delegated type {@code R} is a
 * subclass of the original entity type {@code T}.<br />
 * Delegated entities allow for all sorts of useful things! Consider the
 * following:
 * <code>
 *     final CustomUser user = (CustomUser) catnip.cache().user("snowflake");
 * </code>
 * By implementing an entity delegator, you can use your custom entity class
 * ANYWHERE, as long as you implement this interface! The intent of an entity
 * delegator is that all methods you aren't implementing yourself can be passed
 * through to the original entity, or "delegated" entity.
 *
 * @author amy
 * @since 2/13/20.
 */
public interface EntityDelegator {
    /**
     * Delegates the provided entity.
     * @param type The type of the entity being delegated.
     * @param data The entity to delegate.
     * @param <T> The type of entity being delegated.
     * @param <R> The entity delegate. Must be a subclass of {@code T}.
     * @return The delegated entity.
     */
    <T, R extends T> R delegate(@Nonnull Class<T> type, @Nonnull T data);
}
