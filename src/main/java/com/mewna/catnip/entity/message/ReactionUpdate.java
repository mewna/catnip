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

package com.mewna.catnip.entity.message;

import com.mewna.catnip.entity.Entity;
import com.mewna.catnip.entity.channel.Channel;
import com.mewna.catnip.entity.channel.MessageChannel;
import com.mewna.catnip.entity.guild.Guild;
import com.mewna.catnip.entity.misc.Emoji;
import com.mewna.catnip.entity.user.User;
import io.reactivex.rxjava3.core.Maybe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Fired when a reaction is updated.
 *
 * @author amy
 * @since 10/6/18.
 */
public interface ReactionUpdate extends Entity {
    /**
     * @return The id of the user whose reaction was updated, if applicable.
     */
    @Nullable
    String userId();
    
    @Nonnull
    default Maybe<User> user() {
        if (userId() != null) {
            //noinspection ConstantConditions
            return catnip().cache().user(userId());
        } else {
            return Maybe.empty();
        }
    }
    
    /**
     * @return The id of the channel the update is from.
     */
    @Nonnull
    String channelId();
    
    @Nonnull
    @SuppressWarnings("ConstantConditions")
    default Maybe<MessageChannel> channel() {
        if (guildId() == null) {
            return catnip().rest().channel().getChannelById(channelId()).map(Channel::asMessageChannel).toMaybe();
        } else {
            return catnip().cache().channel(guildId(), channelId()).map(Channel::asMessageChannel);
        }
    }
    
    /**
     * @return The id of the message the update is from.
     */
    @Nonnull
    String messageId();
    
    /**
     * @return The id of the guild the update is from, if applicable.
     */
    @Nullable
    String guildId();
    
    @Nonnull
    default Maybe<Guild> guild() {
        if (guildId() == null) {
            return Maybe.empty();
        } else {
            //noinspection ConstantConditions
            return catnip().cache().guild(guildId());
        }
    }
    
    /**
     * @return The emoji from the updated reaction.
     */
    @Nonnull
    Emoji emoji();
}
