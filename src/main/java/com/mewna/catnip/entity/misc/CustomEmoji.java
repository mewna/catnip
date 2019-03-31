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
import com.mewna.catnip.entity.guild.Guild;
import com.mewna.catnip.util.CatnipEntity;
import org.immutables.value.Value;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author amy
 * @since 3/30/19.
 */
@Value.Modifiable
@CatnipEntity
@JsonDeserialize(as = CustomEmojiImpl.class)
public
interface CustomEmoji extends Emoji, RequiresCatnip<CustomEmojiImpl> {
    @Override
    @Nonnull
    @CheckReturnValue
    default String id() {
        return Long.toUnsignedString(idAsLong());
    }
    
    /**
     * Guild that owns this emoji, or {@code null} if it has no guild.
     * <p>
     * NOTE: This may be null in the case of a reaction, because the data
     * may not be available to get the id for the emoji!
     *
     * @return String representing the ID.
     */
    @Nullable
    @CheckReturnValue
    default Guild guild() {
        final long id = guildIdAsLong();
        if(id == 0) {
            return null;
        }
        return catnip().cache().guild(guildIdAsLong());
    }
    
    /**
     * ID of guild that owns this emoji, or {@code null} if it has no guild.
     * <p>
     * NOTE: This may be null in the case of a reaction, because the data
     * may not be available to get the id for the emoji!
     *
     * @return String representing the ID.
     */
    @Nullable
    @CheckReturnValue
    default String guildId() {
        final long id = guildIdAsLong();
        if(id == 0) {
            return null;
        }
        return Long.toUnsignedString(id);
    }
    
    /**
     * ID of guild that owns this emoji, or {@code 0} if it has no guild.
     * <p>
     * NOTE: This may be null in the case of a reaction, because the data
     * may not be available to get the id for the emoji!
     *
     * @return Long representing the ID.
     */
    @CheckReturnValue
    long guildIdAsLong();
    
    @Override
    @CheckReturnValue
    default boolean custom() {
        return true;
    }
    
    @Override
    @Nonnull
    @CheckReturnValue
    default String forMessage() {
        return String.format("<%s:%s:%s>", animated() ? "a" : "", name(), id());
    }
    
    @Override
    @Nonnull
    @CheckReturnValue
    default String forReaction() {
        return String.format("%s:%s", name(), id());
    }
    
    @Override
    @CheckReturnValue
    default boolean is(@Nonnull final String emoji) {
        return id().equals(emoji) || forMessage().equals(emoji) || forReaction().equals(emoji);
    }
}
