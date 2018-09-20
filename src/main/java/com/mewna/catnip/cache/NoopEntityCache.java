package com.mewna.catnip.cache;

import com.google.common.collect.ImmutableList;
import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.*;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

/**
 * @author amy
 * @since 9/13/18.
 */
@Accessors(fluent = true, chain = true)
@SuppressWarnings("unused")
public class NoopEntityCache implements EntityCacheWorker {
    @Getter
    @Setter
    private Catnip catnip;
    
    @Nonnull
    @Override
    public EntityCache updateCache(@Nonnull final String eventType, @Nonnull final JsonObject payload) {
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
    
    @Nullable
    @Override
    public Guild guild(@Nonnull final String id) {
        return null;
    }
    
    @Nullable
    @Override
    public User user(@Nonnull final String id) {
        return null;
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
}
