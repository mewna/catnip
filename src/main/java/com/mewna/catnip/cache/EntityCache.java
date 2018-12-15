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

package com.mewna.catnip.cache;

import com.mewna.catnip.cache.view.CacheView;
import com.mewna.catnip.cache.view.NamedCacheView;
import com.mewna.catnip.entity.channel.Channel;
import com.mewna.catnip.entity.guild.Guild;
import com.mewna.catnip.entity.guild.Member;
import com.mewna.catnip.entity.guild.Role;
import com.mewna.catnip.entity.misc.Emoji.CustomEmoji;
import com.mewna.catnip.entity.user.Presence;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.entity.user.VoiceState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * If you're looking to implement your own caching system, you want
 * {@link EntityCacheWorker} instead.
 *
 * @author amy
 * @since 9/13/18.
 */
@SuppressWarnings("unused")
public interface EntityCache {
    /**
     * Get the guild with the specified ID. May be {@code null}.
     *
     * @param id The ID of the guild to fetch.
     *
     * @return The guild, or {@code null} if it isn't cached.
     */
    @Nullable
    Guild guild(@Nonnull String id);
    
    /**
     * Get all guilds cached in this entity cache.
     *
     * @return A non-{@code null}, possibly-empty list of guilds
     */
    @Nonnull
    NamedCacheView<Guild> guilds();
    
    /**
     * Get the user with the specified ID. May be {@code null}.
     *
     * @param id The ID of the user to fetch.
     *
     * @return The user, or {@code null} if it isn't cached.
     */
    @Nullable
    User user(@Nonnull String id);
    
    /**
     * Get all users cached in this cache instance.
     *
     * @return A non-{@code null}, possibly-empty list of users.
     */
    @Nonnull
    NamedCacheView<User> users();
    
    /**
     * Get the presence for the user with the specified ID. May be
     * {@code null}.
     *
     * @param id The ID of the user whose presence is to be fetched.
     *
     * @return The user's presence, or {@code null} if it isn't cached.
     */
    @Nullable
    Presence presence(@Nonnull String id);
    
    /**
     * Get all presences cached in this entity cache.
     *
     * @return A non-{@code null}, possibly-empty list of presences
     */
    @Nonnull
    CacheView<Presence> presences();
    
    /**
     * Get the member with the given ID from the guild with the given ID. May
     * be {@code null}.
     *
     * @param guildId The ID of the guild the desired member is in.
     * @param id      The ID of the desired member.
     *
     * @return The member, or {@code null} if it isn't cached.
     */
    @Nullable
    Member member(@Nonnull String guildId, @Nonnull String id);
    
    /**
     * Get all members for the guild with the given ID. The list returned by
     * this method will never be {@code null}, but may be empty.
     *
     * @param guildId The ID of the guild to fetch members for.
     *
     * @return A non-{@code null}, possibly-empty list of guild members.
     */
    @Nonnull
    NamedCacheView<Member> members(@Nonnull String guildId);
    
    /**
     * Get all members cached in this entity cache.
     *
     * @return A non-{@code null}, possibly-empty list of members
     */
    @Nonnull
    NamedCacheView<Member> members();
    
    /**
     * Get the role with the given ID from the guild with the given ID. May be
     * {@code null}.
     *
     * @param guildId The ID of the guild the desired role is from.
     * @param id      The ID of the desired role.
     *
     * @return The role, or {@code null} if it isn't cached.
     */
    @Nullable
    Role role(@Nonnull String guildId, @Nonnull String id);
    
    /**
     * Get all roles for the guild with the given ID. The list returned by this
     * method will never be {@code null}, but may be empty.
     *
     * @param guildId The ID of the guild to fetch roles for.
     *
     * @return A non-{@code null}, possibly-empty list of guild roles.
     */
    @Nonnull
    NamedCacheView<Role> roles(@Nonnull String guildId);
    
    /**
     * Get all roles cached in this entity cache.
     *
     * @return A non-{@code null}, possibly-empty list of roles
     */
    @Nonnull
    NamedCacheView<Role> roles();
    
    /**
     * Get the channel with the given ID from the guild with the given ID. May
     * be {@code null}.
     *
     * @param guildId The ID of the guild the desired channel is from.
     * @param id      The ID of the desired channel.
     *
     * @return The channel, or {@code null} if it isn't cached.
     */
    @Nullable
    Channel channel(@Nonnull String guildId, @Nonnull String id);
    
    /**
     * Get all channels for the guild with the given ID. The list returned by
     * this method will never be {@code null}, but may be empty.
     *
     * @param guildId The ID of the guild to fetch channels for.
     *
     * @return A non-{@code null}, possibly-empty list of guild channels.
     */
    @Nonnull
    CacheView<Channel> channels(@Nonnull String guildId);
    
    /**
     * Get all channels cached in this entity cache.
     *
     * @return A non-{@code null}, possibly-empty list of channels
     */
    @Nonnull
    CacheView<Channel> channels();
    
    /**
     * Get the custom emojis with the given ID from the guild with the given ID.
     * May be {@code null},
     *
     * @param guildId The ID of the guild the desired custom emojis is from.
     * @param id      The ID of the desired custom emojis.
     *
     * @return The custom emojis, or {@code null} if it isn't cached.
     */
    @Nullable
    CustomEmoji emoji(@Nonnull String guildId, @Nonnull String id);
    
    /**
     * Get all custom emojis for the guild with the given ID. The list returned
     * by this method will never be {@code null}, but may be empty.
     *
     * @param guildId The ID of the guild to fetch custom emojis for.
     *
     * @return A non-{@code null}, possibly-empty list of custom emojis.
     */
    @Nonnull
    NamedCacheView<CustomEmoji> emojis(@Nonnull String guildId);
    
    /**
     * Get all emojis cached in this entity cache.
     *
     * @return A non-{@code null}, possibly-empty list of emojis
     */
    @Nonnull
    NamedCacheView<CustomEmoji> emojis();
    
    /**
     * Get the voice state for the user with the given ID, possibly in the
     * guild with the given ID. May be {@code null}.
     *
     * @param guildId The ID of the guild the voice state is from. May be
     *                {@code null}
     * @param id      The ID of the user whose voice state is desired. May not
     *                be {@code null}.
     *
     * @return The requested voice state, or {@code null} if it isn't cached.
     */
    @Nullable
    VoiceState voiceState(@Nullable String guildId, @Nonnull String id);
    
    /**
     * Get all voice states for the guild with the given ID. The list returned
     * by this method will never be {@code null}, but may be empty.
     *
     * @param guildId The ID of the guild to fetch voice states for.
     *
     * @return A non-{@code null}, possibly-empty list of voice states.
     */
    @Nonnull
    CacheView<VoiceState> voiceStates(@Nonnull String guildId);
    
    /**
     * Get all voice states for the entire bot. The list returned by this
     * method will never be {@code null}, but may be empty.
     *
     * @return A non-{@code null}, possibly-empty list of voice states.
     */
    @Nonnull
    CacheView<VoiceState> voiceStates();
}
