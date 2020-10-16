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

import com.mewna.catnip.entity.partials.Snowflake;
import com.mewna.catnip.entity.channel.Channel;
import com.mewna.catnip.entity.channel.MessageChannel;
import com.mewna.catnip.entity.guild.Guild;
import io.reactivex.rxjava3.core.Maybe;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * When a message's embeds are updated, Discord sends a {@code MESSAGE_UPDATE}
 * event that only has id/channel_id/guild_id/embeds in the inner payload.
 * Because of this, we can't just use {@link Message} to represent this event.
 *
 * @author amy
 * @since 10/9/18.
 */
public interface MessageEmbedUpdate extends Snowflake {
    @Nullable
    @CheckReturnValue
    default String guildId() {
        final long id = guildIdAsLong();
        if(id == 0) {
            return null;
        }
        return Long.toUnsignedString(id);
    }
    
    @CheckReturnValue
    long guildIdAsLong();
    
    @Nonnull
    @CheckReturnValue
    default Maybe<Guild> guild() {
        if(guildId() == null) {
            return Maybe.empty();
        } else {
            //noinspection ConstantConditions
            return catnip().cache().guild(guildId());
        }
    }
    
    @Nonnull
    @CheckReturnValue
    default String channelId() {
        return Long.toUnsignedString(channelIdAsLong());
    }
    
    @CheckReturnValue
    long channelIdAsLong();
    
    @Nonnull
    @CheckReturnValue
    default Maybe<MessageChannel> channel() {
        final long guild = guildIdAsLong();
        if(guild != 0) {
            return catnip().cache().channel(guild, channelIdAsLong()).map(Channel::asMessageChannel);
        } else {
            return catnip().rest().channel().getChannelById(channelId()).map(Channel::asMessageChannel).toMaybe();
        }
    }
    
    @Nonnull
    @CheckReturnValue
    List<Embed> embeds();
}
