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

package com.mewna.catnip.entity.message;

import com.mewna.catnip.entity.Entity;
import com.mewna.catnip.entity.guild.Guild;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author amy
 * @since 8/19/19.
 */
public interface MessageReference extends Entity {
    /**
     * The id of the message this message reference is from. May be null.
     *
     * @return String representing the message ID.
     */
    @Nullable
    String messageId();
    
    /**
     * The id of the channel this message reference is from. May not be null.
     *
     * @return String representing the channel ID.
     */
    @Nonnull
    String channelId();
    
    /**
     * The id of the guild this message reference is from. May be null.
     *
     * @return String representing the guild ID.
     */
    @Nullable
    @CheckReturnValue
    String guildId();
    
    /**
     * The guild this entity is from.
     *
     * @return Guild represented by the guild ID.
     */
    @Nullable
    @CheckReturnValue
    default Guild guild() {
        if(guildId() == null) {
            return null;
        } else {
            //noinspection ConstantConditions
            return catnip().cache().guild(guildId());
        }
    }
}
