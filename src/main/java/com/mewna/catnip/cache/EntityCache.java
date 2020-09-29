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
import com.mewna.catnip.entity.channel.GuildChannel;
import com.mewna.catnip.entity.guild.Guild;
import com.mewna.catnip.entity.guild.Member;
import com.mewna.catnip.entity.guild.Role;
import com.mewna.catnip.entity.misc.Emoji.CustomEmoji;
import com.mewna.catnip.entity.user.Presence;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.entity.user.VoiceState;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

import javax.annotation.Nonnull;

/**
 * If you're looking to implement your own caching system, you want
 * {@link EntityCacheWorker} instead.
 *
 * @author amy
 * @since 9/13/18.
 */
public interface EntityCache {
    /**
     * Get the guild with the specified ID. May be {@code null}.
     *
     * @param id The ID of the guild to fetch.
     *
     * @return The guild, or {@code null} if it isn't cached.
     */
    @Nonnull
    default Maybe<Guild> guild(@Nonnull final String id) {
        return guild(Long.parseUnsignedLong(id));
    }
    
    /**
     * Get the guild with the specified ID. May be {@code null}.
     *
     * @param id The ID of the guild to fetch.
     *
     * @return The guild, or {@code null} if it isn't cached.
     */
    @Nonnull
    Maybe<Guild> guild(final long id);
    
    /**
     * @return A view of the current guild cache. Updates to the cache will update this view.
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
    @Nonnull
    default Maybe<User> user(@Nonnull final String id) {
        return user(Long.parseUnsignedLong(id));
    }
    
    /**
     * Get the user with the specified ID. May be {@code null}.
     *
     * @param id The ID of the user to fetch.
     *
     * @return The user, or {@code null} if it isn't cached.
     */
    @Nonnull
    Maybe<User> user(final long id);
    
    /**
     * @return A view of the current user cache. Updates to the cache will update this view.
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
    @Nonnull
    default Maybe<Presence> presence(@Nonnull final String id) {
        return presence(Long.parseUnsignedLong(id));
    }
    
    /**
     * Get the presence for the user with the specified ID. May be
     * {@code null}.
     *
     * @param id The ID of the user whose presence is to be fetched.
     *
     * @return The user's presence, or {@code null} if it isn't cached.
     */
    @Nonnull
    Maybe<Presence> presence(final long id);
    
    /**
     * @return A view of the current presence cache. Updates to the cache will update this view.
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
    @Nonnull
    default Maybe<Member> member(@Nonnull final String guildId, @Nonnull final String id) {
        return member(Long.parseUnsignedLong(guildId), Long.parseUnsignedLong(id));
    }
    
    /**
     * Get the member with the given ID from the guild with the given ID. May
     * be {@code null}.
     *
     * @param guildId The ID of the guild the desired member is in.
     * @param id      The ID of the desired member.
     *
     * @return The member, or {@code null} if it isn't cached.
     */
    @Nonnull
    Maybe<Member> member(final long guildId, final long id);
    
    /**
     * Get all members for the guild with the given ID.
     *
     * @param guildId The ID of the guild to fetch members for.
     *
     * @return A view of the current member cache of the guild. Updates to the cache will update this view.
     */
    @Nonnull
    default NamedCacheView<Member> members(@Nonnull final String guildId) {
        return members(Long.parseUnsignedLong(guildId));
    }
    
    /**
     * Get all members for the guild with the given ID.
     *
     * @param guildId The ID of the guild to fetch members for.
     *
     * @return A view of the current member cache of the guild. Updates to the cache will update this view.
     */
    @Nonnull
    NamedCacheView<Member> members(long guildId);
    
    /**
     * Get all members cached in this entity cache.
     *
     * @return A view of all the current member caches. Updates to the caches or
     * additions/removals (of guilds) will update this view.
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
    @Nonnull
    default Maybe<Role> role(@Nonnull final String guildId, @Nonnull final String id) {
        return role(Long.parseUnsignedLong(guildId), Long.parseUnsignedLong(id));
    }
    
    /**
     * Get the role with the given ID from the guild with the given ID. May be
     * {@code null}.
     *
     * @param guildId The ID of the guild the desired role is from.
     * @param id      The ID of the desired role.
     *
     * @return The role, or {@code null} if it isn't cached.
     */
    @Nonnull
    Maybe<Role> role(final long guildId, final long id);
    
    /**
     * Get all roles for the guild with the given ID.
     *
     * @param guildId The ID of the guild to fetch roles for.
     *
     * @return A view of the current role cache of the guild. Updates to the cache will update this view.
     */
    @Nonnull
    default NamedCacheView<Role> roles(@Nonnull final String guildId) {
        return roles(Long.parseUnsignedLong(guildId));
    }
    
    /**
     * Get all roles for the guild with the given ID.
     *
     * @param guildId The ID of the guild to fetch roles for.
     *
     * @return A view of the current role cache of the guild. Updates to the cache will update this view.
     */
    @Nonnull
    NamedCacheView<Role> roles(final long guildId);
    
    /**
     * Get all roles cached in this entity cache.
     *
     * @return A view of all the current role caches. Updates to the caches or
     * additions/removals (of guilds) will update this view.
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
    @Nonnull
    default Maybe<GuildChannel> channel(@Nonnull final String guildId, @Nonnull final String id) {
        return channel(Long.parseUnsignedLong(guildId), Long.parseUnsignedLong(id));
    }
    
    /**
     * Get the channel with the given ID from the guild with the given ID. May
     * be {@code null}.
     *
     * @param guildId The ID of the guild the desired channel is from.
     * @param id      The ID of the desired channel.
     *
     * @return The channel, or {@code null} if it isn't cached.
     */
    @Nonnull
    Maybe<GuildChannel> channel(final long guildId, final long id);
    
    /**
     * Get all channels for the guild with the given ID.
     *
     * @param guildId The ID of the guild to fetch channels for.
     *
     * @return A view of the current channel cache of the guild. Updates to the cache will update this view.
     */
    @Nonnull
    default NamedCacheView<GuildChannel> channels(@Nonnull final String guildId) {
        return channels(Long.parseUnsignedLong(guildId));
    }
    
    /**
     * Get all channels for the guild with the given ID.
     *
     * @param guildId The ID of the guild to fetch channels for.
     *
     * @return A view of the current channel cache of the guild. Updates to the cache will update this view.
     */
    @Nonnull
    NamedCacheView<GuildChannel> channels(long guildId);
    
    /**
     * Get all guild channels cached in this entity cache.
     *
     * @return A view of all the current guild channel caches. Updates to the caches or
     * additions/removals (of guilds) will update this view.
     */
    @Nonnull
    NamedCacheView<GuildChannel> channels();
    
    /**
     * Get the custom emojis with the given ID from the guild with the given ID.
     * May be {@code null},
     *
     * @param guildId The ID of the guild the desired custom emojis is from.
     * @param id      The ID of the desired custom emojis.
     *
     * @return The custom emojis, or {@code null} if it isn't cached.
     */
    @Nonnull
    default Maybe<CustomEmoji> emoji(@Nonnull final String guildId, @Nonnull final String id) {
        return emoji(Long.parseUnsignedLong(guildId), Long.parseUnsignedLong(id));
    }
    
    /**
     * Get the custom emojis with the given ID from the guild with the given ID.
     * May be {@code null},
     *
     * @param guildId The ID of the guild the desired custom emojis is from.
     * @param id      The ID of the desired custom emojis.
     *
     * @return The custom emojis, or {@code null} if it isn't cached.
     */
    @Nonnull
    Maybe<CustomEmoji> emoji(final long guildId, final long id);
    
    /**
     * Get all custom emojis for the guild with the given ID.
     *
     * @param guildId The ID of the guild to fetch custom emojis for.
     *
     * @return A view of the current emoji cache of the guild. Updates to the cache will update this view.
     */
    @Nonnull
    default NamedCacheView<CustomEmoji> emojis(@Nonnull final String guildId) {
        return emojis(Long.parseUnsignedLong(guildId));
    }
    
    /**
     * Get all custom emojis for the guild with the given ID.
     *
     * @param guildId The ID of the guild to fetch custom emojis for.
     *
     * @return A view of the current emoji cache of the guild. Updates to the cache will update this view.
     */
    @Nonnull
    NamedCacheView<CustomEmoji> emojis(long guildId);
    
    /**
     * Get all emojis cached in this entity cache.
     *
     * @return A view of all the current emoji caches. Updates to the caches or
     * additions/removals (of guilds) will update this view.
     */
    @Nonnull
    NamedCacheView<CustomEmoji> emojis();
    
    /**
     * Get the voice state for the user with the given ID, possibly in the
     * guild with the given ID. May be {@code null}.
     *
     * @param guildId The ID of the guild the voice state is from. May not be
     *                {@code null}
     * @param id      The ID of the user whose voice state is desired. May not
     *                be {@code null}.
     *
     * @return The requested voice state, or {@code null} if it isn't cached.
     */
    @Nonnull
    default Maybe<VoiceState> voiceState(@Nonnull final String guildId, @Nonnull final String id) {
        return voiceState(Long.parseUnsignedLong(guildId), Long.parseUnsignedLong(id));
    }
    
    /**
     * Get the voice state for the user with the given ID, possibly in the
     * guild with the given ID. May be {@code null}.
     *
     * @param guildId The ID of the guild the voice state is from.
     * @param id      The ID of the user whose voice state is desired.
     *
     * @return The requested voice state, or {@code null} if it isn't cached.
     */
    @Nonnull
    Maybe<VoiceState> voiceState(final long guildId, final long id);
    
    /**
     * Get all voice states for the guild with the given ID.
     *
     * @param guildId The ID of the guild to fetch voice states for.
     *
     * @return A view of the current voice state cache of the guild. Updates to the cache will update this view.
     */
    @Nonnull
    default CacheView<VoiceState> voiceStates(@Nonnull final String guildId) {
        return voiceStates(Long.parseUnsignedLong(guildId));
    }
    
    /**
     * Get all voice states for the guild with the given ID.
     *
     * @param guildId The ID of the guild to fetch voice states for.
     *
     * @return A view of the current voice state cache of the guild. Updates to the cache will update this view.
     */
    @Nonnull
    CacheView<VoiceState> voiceStates(final long guildId);
    
    /**
     * Get all voice states for the entire bot.
     *
     * @return A view of all the current voice state caches. Updates to the caches or
     * additions/removals (of guilds) will update this view.
     */
    @Nonnull
    CacheView<VoiceState> voiceStates();
    
    /**
     * @return The currently-logged-in user. May be {@code null} if no shards
     * have logged in.
     */
    @Nonnull
    Maybe<User> selfUser();
}
