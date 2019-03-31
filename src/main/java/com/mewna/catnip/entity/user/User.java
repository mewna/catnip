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

package com.mewna.catnip.entity.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mewna.catnip.entity.HasAvatar;
import com.mewna.catnip.entity.Mentionable;
import com.mewna.catnip.entity.RequiresCatnip;
import com.mewna.catnip.entity.channel.DMChannel;
import com.mewna.catnip.entity.guild.Guild;
import com.mewna.catnip.entity.guild.Member;
import com.mewna.catnip.util.CatnipEntity;
import org.immutables.value.Value.Modifiable;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletionStage;

/**
 * A single Discord user.
 *
 * @author amy
 * @since 9/4/18
 */
@SuppressWarnings("unused")
@Modifiable
@CatnipEntity
@JsonDeserialize(as = UserImpl.class)
public interface User extends Mentionable, HasAvatar, RequiresCatnip<UserImpl> {
    /**
     * The username of the user.
     *
     * @return User's name. Never null.
     */
    @Nonnull
    @CheckReturnValue
    String username();
    
    /**
     * The user's effective name shown in a guild.
     *
     * @return User's nickname in the guild, if set, otherwise the username.
     */
    @Nonnull
    @CheckReturnValue
    default String effectiveName(@Nonnull final Guild guild) {
        final String username = username();
        
        final Member member = guild.members().getById(idAsLong());
        
        if(member == null) {
            return username;
        }
        
        final String nick = member.nick();
        return nick != null ? nick : username;
    }
    
    /**
     * The DiscordTag of the user, which is the username, an hash, and the discriminator.
     *
     * @return User's DiscordTag. Never null.
     */
    @Nonnull
    @CheckReturnValue
    default String discordTag() {
        return username() + '#' + discriminator();
    }
    
    /**
     * User's avatar hash.
     * <br><b>This does not return their avatar URL nor image directly.</b>
     *
     * @return User's hashed avatar string. Can be null.
     *
     * @see User#avatarUrl() Getting the user's avatar
     */
    @Nullable
    @CheckReturnValue
    String avatar();
    
    /**
     * Whether the user is a bot, or webhook/fake user.
     *
     * @return True if the user is a bot, false if the user is a human.
     */
    @CheckReturnValue
    boolean bot();
    
    /**
     * @return The user's presence, or {@code null} if no presence is cached.
     */
    @Nullable
    @CheckReturnValue
    default Presence presence() {
        return catnip().cache().presence(id());
    }
    
    /**
     * Creates a DM channel with this user.
     *
     * @return Future with the result of the DM creation.
     */
    @JsonIgnore
    @CheckReturnValue
    default CompletionStage<DMChannel> createDM() {
        return catnip().rest().user().createDM(id());
    }
    
    @Nonnull
    @CheckReturnValue
    default String asMention() {
        return "<@" + id() + '>';
    }
}
