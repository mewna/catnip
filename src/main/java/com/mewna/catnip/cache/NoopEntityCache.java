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

import com.google.common.collect.ImmutableList;
import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.channel.Channel;
import com.mewna.catnip.entity.channel.GuildChannel;
import com.mewna.catnip.entity.guild.Guild;
import com.mewna.catnip.entity.guild.Member;
import com.mewna.catnip.entity.guild.Role;
import com.mewna.catnip.entity.misc.Emoji.CustomEmoji;
import com.mewna.catnip.entity.user.Presence;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.entity.user.VoiceState;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author amy
 * @since 9/13/18.
 */
@Accessors(fluent = true, chain = true)
@SuppressWarnings("unused")
public class NoopEntityCache implements EntityCacheWorker {
    @Nonnull
    @Override
    public Future<Void> updateCache(@Nonnull final String eventType, @Nonnull final JsonObject payload) {
        return Future.succeededFuture(null);
    }
    
    @Override
    public void bulkCacheUsers(@Nonnull final Collection<User> users) {
    }
    
    @Override
    public void bulkCacheChannels(@Nonnull final Collection<GuildChannel> channels) {
    }
    
    @Override
    public void bulkCacheRoles(@Nonnull final Collection<Role> roles) {
    }
    
    @Override
    public void bulkCacheMembers(@Nonnull final Collection<Member> members) {
    }
    
    @Override
    public void bulkCacheEmoji(@Nonnull final Collection<CustomEmoji> emoji) {
    }
    
    @Override
    public void bulkCachePresences(@Nonnull final Map<String, Presence> presences) {
    }
    
    @Override
    public void bulkCacheVoiceStates(@Nonnull final Collection<VoiceState> voiceStates) {
    }
    
    @Nullable
    @Override
    public Guild guild(@Nonnull final String id) {
        return null;
    }
    
    @Nonnull
    @Override
    public List<Guild> guilds() {
        return ImmutableList.of();
    }
    
    @Nullable
    @Override
    public User user(@Nonnull final String id) {
        return null;
    }
    
    @Nullable
    @Override
    public Presence presence(@Nonnull final String id) {
        return null;
    }
    
    @Nonnull
    @Override
    public List<Presence> presences() {
        return ImmutableList.of();
    }
    
    @Nullable
    @Override
    public Member member(@Nonnull final String guildId, @Nonnull final String id) {
        return null;
    }
    
    @Nonnull
    @Override
    public List<Member> members(@Nonnull final String guildId) {
        return ImmutableList.of();
    }
    
    @Nonnull
    @Override
    public List<Member> members() {
        return ImmutableList.of();
    }
    
    @Nonnull
    @Override
    public List<User> users() {
        return ImmutableList.of();
    }
    
    @Nullable
    @Override
    public Role role(@Nonnull final String guildId, @Nonnull final String id) {
        return null;
    }
    
    @Nonnull
    @Override
    public List<Role> roles(@Nonnull final String guildId) {
        return ImmutableList.of();
    }
    
    @Nonnull
    @Override
    public List<Role> roles() {
        return ImmutableList.of();
    }
    
    @Nullable
    @Override
    public Channel channel(@Nonnull final String guildId, @Nonnull final String id) {
        return null;
    }
    
    @Nonnull
    @Override
    public List<Channel> channels(@Nonnull final String guildId) {
        return ImmutableList.of();
    }
    
    @Nonnull
    @Override
    public List<Channel> channels() {
        return ImmutableList.of();
    }
    
    @Nullable
    @Override
    public CustomEmoji emoji(@Nonnull final String guildId, @Nonnull final String id) {
        return null;
    }
    
    @Nonnull
    @Override
    public List<CustomEmoji> emojis(@Nonnull final String guildId) {
        return ImmutableList.of();
    }
    
    @Nonnull
    @Override
    public List<CustomEmoji> emojis() {
        return ImmutableList.of();
    }
    
    @Nullable
    @Override
    public VoiceState voiceState(@Nullable final String guildId, @Nonnull final String id) {
        return null;
    }
    
    @Nonnull
    @Override
    public List<VoiceState> voiceStates(@Nonnull final String guildId) {
        return ImmutableList.of();
    }
    
    @Nonnull
    @Override
    public List<VoiceState> voiceState() {
        return ImmutableList.of();
    }
    
    @Nonnull
    @Override
    public EntityCache catnip(@Nonnull final Catnip catnip) {
        return this;
    }
}
