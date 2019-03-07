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

package com.mewna.catnip.cache;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.cache.view.CacheView;
import com.mewna.catnip.cache.view.NamedCacheView;
import com.mewna.catnip.entity.channel.GuildChannel;
import com.mewna.catnip.entity.channel.UserDMChannel;
import com.mewna.catnip.entity.guild.Guild;
import com.mewna.catnip.entity.guild.Member;
import com.mewna.catnip.entity.guild.Role;
import com.mewna.catnip.entity.misc.Emoji.CustomEmoji;
import com.mewna.catnip.entity.user.Presence;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.entity.user.VoiceState;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * A customizable entity cache is basically just what it sounds like - a cache
 * whose behaviour is ENTIRELY meant to be customized. By default, this cache
 * will throw an {@link UnsupportedOperationException} on all methods it does
 * not implement. If you want to write a custom partial cache, this is probably
 * the best place to start so that you don't have lots of empty method bodies.
 *
 * @author amy
 * @since 3/7/19.
 */
public abstract class CustomizableEntityCache implements EntityCacheWorker {
    @Nonnull
    @Override
    public Future<Void> updateCache(@Nonnull final String eventType, final int shardId, @Nonnull final JsonObject payload) {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    @Override
    public void bulkCacheUsers(final int shardId, @Nonnull final Collection<User> users) {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    @Override
    public void bulkCacheChannels(final int shardId, @Nonnull final Collection<GuildChannel> channels) {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    @Override
    public void bulkCacheRoles(final int shardId, @Nonnull final Collection<Role> roles) {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    @Override
    public void bulkCacheMembers(final int shardId, @Nonnull final Collection<Member> members) {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    @Override
    public void bulkCacheEmoji(final int shardId, @Nonnull final Collection<CustomEmoji> emoji) {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    @Override
    public void bulkCachePresences(final int shardId, @Nonnull final Map<String, Presence> presences) {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    @Override
    public void bulkCacheVoiceStates(final int shardId, @Nonnull final Collection<VoiceState> voiceStates) {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    @Override
    public void invalidateShard(final int id) {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    @Nonnull
    @Override
    public EntityCache catnip(@Nonnull final Catnip catnip) {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    @Nonnull
    @Override
    public CompletableFuture<Guild> guildAsync(final long id) {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    @Nonnull
    @Override
    public NamedCacheView<Guild> guilds() {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    @Nonnull
    @Override
    public CompletableFuture<User> userAsync(final long id) {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    @Nonnull
    @Override
    public NamedCacheView<User> users() {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    @Nonnull
    @Override
    public CompletableFuture<Presence> presenceAsync(final long id) {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    @Nonnull
    @Override
    public CacheView<Presence> presences() {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    @Nonnull
    @Override
    public CompletableFuture<Member> memberAsync(final long guildId, final long id) {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    @Nonnull
    @Override
    public NamedCacheView<Member> members(final long guildId) {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    @Nonnull
    @Override
    public NamedCacheView<Member> members() {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    @Nonnull
    @Override
    public CompletableFuture<Role> roleAsync(final long guildId, final long id) {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    @Nonnull
    @Override
    public NamedCacheView<Role> roles(final long guildId) {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    @Nonnull
    @Override
    public NamedCacheView<Role> roles() {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    @Nonnull
    @Override
    public CompletableFuture<GuildChannel> channelAsync(final long guildId, final long id) {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    @Nonnull
    @Override
    public NamedCacheView<GuildChannel> channels(final long guildId) {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    @Nonnull
    @Override
    public NamedCacheView<GuildChannel> channels() {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    @Nonnull
    @Override
    public CompletableFuture<UserDMChannel> dmChannelAsync(final long id) {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    @Nonnull
    @Override
    public CacheView<UserDMChannel> dmChannels() {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    @Nonnull
    @Override
    public CompletableFuture<CustomEmoji> emojiAsync(final long guildId, final long id) {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    @Nonnull
    @Override
    public NamedCacheView<CustomEmoji> emojis(final long guildId) {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    @Nonnull
    @Override
    public NamedCacheView<CustomEmoji> emojis() {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    @Nonnull
    @Override
    public CompletableFuture<VoiceState> voiceStateAsync(final long guildId, final long id) {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    @Nonnull
    @Override
    public CacheView<VoiceState> voiceStates(final long guildId) {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    @Nonnull
    @Override
    public CacheView<VoiceState> voiceStates() {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    @Nonnull
    @Override
    public CompletableFuture<User> selfUserAsync() {
        throw new UnsupportedOperationException("Not implemented.");
    }
}
