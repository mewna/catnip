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

package com.mewna.catnip.entity.misc;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mewna.catnip.entity.RequiresCatnip;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.util.CatnipEntity;
import org.immutables.value.Value;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * @author amy
 * @since 3/30/19.
 */
@Value.Modifiable
@CatnipEntity
@JsonDeserialize(as = UnicodeEmojiImpl.class)
public
interface UnicodeEmoji extends Emoji, RequiresCatnip<UnicodeEmojiImpl> {
    @Override
    default String id() {
        throw new IllegalStateException("Unicode emojis have no IDs!");
    }
    
    @Override
    default long idAsLong() {
        throw new IllegalStateException("Unicode emojis have no IDs!");
    }
    
    @Override
    @Nonnull
    @CheckReturnValue
    default List<String> roles() {
        return Collections.emptyList();
    }
    
    @Override
    @Nullable
    @CheckReturnValue
    default User user() {
        return null;
    }
    
    @Override
    @CheckReturnValue
    default boolean managed() {
        return false;
    }
    
    @Override
    @CheckReturnValue
    default boolean animated() {
        return false;
    }
    
    @Override
    @CheckReturnValue
    default boolean custom() {
        return false;
    }
    
    @Override
    @Nonnull
    @CheckReturnValue
    default String forMessage() {
        return name();
    }
    
    @Override
    @Nonnull
    @CheckReturnValue
    default String forReaction() {
        return name();
    }
    
    @Override
    @CheckReturnValue
    default boolean is(@Nonnull final String emoji) {
        return name().equals(emoji);
    }
}
