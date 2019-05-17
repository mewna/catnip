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
import com.mewna.catnip.util.SafeVertxCompletableFuture;
import io.reactivex.Single;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.Collection;
import java.util.Map;

/**
 * A customizable entity cache is basically just what it sounds like - a cache
 * whose behaviour is ENTIRELY meant to be customized. By default, this cache
 * will return {@link CacheView#noop() noop caches} and futures completed with
 * {@code null}. If you want to write a custom partial cache, this is probably
 * the best place to start so that you don't have lots of empty method bodies.
 *
 * @author amy
 * @since 3/7/19.
 */
public abstract class CustomizableEntityCache implements EntityCacheWorker {
    protected Catnip catnip;
    
    @OverridingMethodsMustInvokeSuper
    @Nonnull
    @Override
    public EntityCache catnip(@Nonnull final Catnip catnip) {
        this.catnip = catnip;
        return this;
    }
    
    @Nonnull
    @Override
    public Future<Void> updateCache(@Nonnull final String eventType, final int shardId, @Nonnull final JsonObject payload) {
        return Future.succeededFuture();
    }
    
    @Override
    public void bulkCacheUsers(final int shardId, @Nonnull final Collection<User> users) {
        //noop
    }
    
    @Override
    public void bulkCacheChannels(final int shardId, @Nonnull final Collection<GuildChannel> channels) {
        //noop
    }
    
    @Override
    public void bulkCacheRoles(final int shardId, @Nonnull final Collection<Role> roles) {
        //noop
    }
    
    @Override
    public void bulkCacheMembers(final int shardId, @Nonnull final Collection<Member> members) {
        //noop
    }
    
    @Override
    public void bulkCacheEmoji(final int shardId, @Nonnull final Collection<CustomEmoji> emoji) {
        //noop
    }
    
    @Override
    public void bulkCachePresences(final int shardId, @Nonnull final Map<String, Presence> presences) {
        //noop
    }
    
    @Override
    public void bulkCacheVoiceStates(final int shardId, @Nonnull final Collection<VoiceState> voiceStates) {
        //noop
    }
    
    @Override
    public void invalidateShard(final int id) {
        //noop
    }
    
    @Nonnull
    @Override
    public Single<Guild> guildAsync(final long id) {
        return Single.fromFuture(SafeVertxCompletableFuture.completedFuture(catnip, null));
    }
    
    @Nonnull
    @Override
    public NamedCacheView<Guild> guilds() {
        return CacheView.noop();
    }
    
    @Nonnull
    @Override
    public Single<User> userAsync(final long id) {
        return Single.fromFuture(SafeVertxCompletableFuture.completedFuture(catnip, null));
    }
    
    @Nonnull
    @Override
    public NamedCacheView<User> users() {
        return CacheView.noop();
    }
    
    @Nonnull
    @Override
    public Single<Presence> presenceAsync(final long id) {
        return Single.fromFuture(SafeVertxCompletableFuture.completedFuture(catnip, null));
    }
    
    @Nonnull
    @Override
    public CacheView<Presence> presences() {
        return CacheView.noop();
    }
    
    @Nonnull
    @Override
    public Single<Member> memberAsync(final long guildId, final long id) {
        return Single.fromFuture(SafeVertxCompletableFuture.completedFuture(catnip, null));
    }
    
    @Nonnull
    @Override
    public NamedCacheView<Member> members(final long guildId) {
        return CacheView.noop();
    }
    
    @Nonnull
    @Override
    public NamedCacheView<Member> members() {
        return CacheView.noop();
    }
    
    @Nonnull
    @Override
    public Single<Role> roleAsync(final long guildId, final long id) {
        return Single.fromFuture(SafeVertxCompletableFuture.completedFuture(catnip, null));
    }
    
    @Nonnull
    @Override
    public NamedCacheView<Role> roles(final long guildId) {
        return CacheView.noop();
    }
    
    @Nonnull
    @Override
    public NamedCacheView<Role> roles() {
        return CacheView.noop();
    }
    
    @Nonnull
    @Override
    public Single<GuildChannel> channelAsync(final long guildId, final long id) {
        return Single.fromFuture(SafeVertxCompletableFuture.completedFuture(catnip, null));
    }
    
    @Nonnull
    @Override
    public NamedCacheView<GuildChannel> channels(final long guildId) {
        return CacheView.noop();
    }
    
    @Nonnull
    @Override
    public NamedCacheView<GuildChannel> channels() {
        return CacheView.noop();
    }
    
    @Nonnull
    @Override
    public Single<UserDMChannel> dmChannelAsync(final long id) {
        return Single.fromFuture(SafeVertxCompletableFuture.completedFuture(catnip, null));
    }
    
    @Nonnull
    @Override
    public CacheView<UserDMChannel> dmChannels() {
        return CacheView.noop();
    }
    
    @Nonnull
    @Override
    public Single<CustomEmoji> emojiAsync(final long guildId, final long id) {
        return Single.fromFuture(SafeVertxCompletableFuture.completedFuture(catnip, null));
    }
    
    @Nonnull
    @Override
    public NamedCacheView<CustomEmoji> emojis(final long guildId) {
        return CacheView.noop();
    }
    
    @Nonnull
    @Override
    public NamedCacheView<CustomEmoji> emojis() {
        return CacheView.noop();
    }
    
    @Nonnull
    @Override
    public Single<VoiceState> voiceStateAsync(final long guildId, final long id) {
        return Single.fromFuture(SafeVertxCompletableFuture.completedFuture(catnip, null));
    }
    
    @Nonnull
    @Override
    public CacheView<VoiceState> voiceStates(final long guildId) {
        return CacheView.noop();
    }
    
    @Nonnull
    @Override
    public CacheView<VoiceState> voiceStates() {
        return CacheView.noop();
    }
    
    @Nonnull
    @Override
    public Single<User> selfUserAsync() {
        return Single.fromFuture(SafeVertxCompletableFuture.completedFuture(catnip, null));
    }
}
