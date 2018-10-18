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
    public EntityCache updateCache(@Nonnull final String eventType, @Nonnull final JsonObject payload) {
        return this;
    }
    
    @Nonnull
    @Override
    public EntityCache bulkCacheUsers(@Nonnull final Collection<User> users) {
        return this;
    }
    
    @Nonnull
    @Override
    public EntityCache bulkCacheChannels(@Nonnull final Collection<GuildChannel> channels) {
        return this;
    }
    
    @Nonnull
    @Override
    public EntityCache bulkCacheRoles(@Nonnull final Collection<Role> roles) {
        return this;
    }
    
    @Nonnull
    @Override
    public EntityCache bulkCacheMembers(@Nonnull final Collection<Member> members) {
        return this;
    }
    
    @Nonnull
    @Override
    public EntityCache bulkCacheEmoji(@Nonnull final Collection<CustomEmoji> emoji) {
        return this;
    }
    
    @Nonnull
    @Override
    public EntityCache bulkCachePresences(@Nonnull final Map<String, Presence> presences) {
        return this;
    }
    
    @Nonnull
    @Override
    public EntityCache bulkCacheVoiceStates(@Nonnull final Collection<VoiceState> voiceStates) {
        return this;
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
