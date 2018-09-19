package com.mewna.catnip.cache;

import com.google.common.collect.ImmutableList;
import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.*;
import com.mewna.catnip.entity.impl.EntityBuilder;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.experimental.Accessors;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static com.mewna.catnip.shard.DiscordEvent.*;

/**
 * @author amy
 * @since 9/18/18.
 */
@Accessors(fluent = true, chain = true)
@SuppressWarnings("unused")
public class MemoryEntityCache implements EntityCache {
    // TODO: What even is efficiency amirite
    
    private final Map<String, Guild> guildCache = new ConcurrentHashMap<>();
    private final Map<String, User> userCache = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Member>> memberCache = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Role>> roleCache = new ConcurrentHashMap<>();
    // TODO: How to handle DM channels?
    private final Map<String, Map<String, Channel>> channelCache = new ConcurrentHashMap<>();
    @Getter
    private Catnip catnip;
    private EntityBuilder entityBuilder;
    
    private void cacheChannel(final Channel channel) {
        if(channel.isGuild()) {
            final GuildChannel gc = (GuildChannel) channel;
            Map<String, Channel> channels = channelCache.get(gc.guildId());
            if(channels == null) {
                channels = new ConcurrentHashMap<>();
                channelCache.put(gc.guildId(), channels);
            }
            channels.put(gc.id(), gc);
            // TODO: Add to guild
            catnip.logAdapter().debug("Cached channel {} for guild {}", gc.id(), gc.guildId());
        } else {
            catnip.logAdapter().warn("I don't know how to cache non-guild channel {}!", channel.id());
        }
    }
    
    @Nonnull
    @Override
    public EntityCache updateCache(@Nonnull final String eventType, @Nonnull final JsonObject payload) {
        switch(eventType) {
            // Channels
            case CHANNEL_CREATE: {
                final Channel channel = entityBuilder.createChannel(payload);
                cacheChannel(channel);
                break;
            }
            case CHANNEL_UPDATE: {
                final Channel channel = entityBuilder.createChannel(payload);
                cacheChannel(channel);
                break;
            }
            case CHANNEL_DELETE: {
                final Channel channel = entityBuilder.createChannel(payload);
                if(channel.isGuild()) {
                    final GuildChannel gc = (GuildChannel) channel;
                    Map<String, Channel> channels = channelCache.get(gc.guildId());
                    if(channels == null) {
                        channels = new ConcurrentHashMap<>();
                        channelCache.put(gc.guildId(), channels);
                    }
                    channels.remove(gc.id());
                    // TODO: Remove from guild
                    catnip.logAdapter().debug("Deleted channel {} for guild {}", gc.id(), gc.guildId());
                } else {
                    catnip.logAdapter().warn("I don't know how to delete non-guild channel {}!", channel.id());
                }
                break;
            }
            // Guilds
            case GUILD_CREATE: {
                final Guild guild = entityBuilder.createGuild(payload);
                guildCache.put(guild.id(), guild);
                guild.channels().forEach(this::cacheChannel);
                break;
            }
            case GUILD_UPDATE: {
                final Guild guild = entityBuilder.createGuild(payload);
                guildCache.put(guild.id(), guild);
                guild.channels().forEach(this::cacheChannel);
                // TODO: Cache roles
                // TODO: Cache members
                // TODO: Cache users?
                break;
            }
            case GUILD_DELETE: {
                final Guild guild = entityBuilder.createGuild(payload);
                guildCache.remove(guild.id());
                break;
            }
            // Roles
            case GUILD_ROLE_CREATE: {
                break;
            }
            case GUILD_ROLE_UPDATE: {
                break;
            }
            case GUILD_ROLE_DELETE: {
                break;
            }
            // Members
            case GUILD_MEMBER_ADD: {
                break;
            }
            case GUILD_MEMBER_REMOVE: {
                break;
            }
            case GUILD_MEMBER_UPDATE: {
                break;
            }
            // Member chunking
            case GUILD_MEMBERS_CHUNK: {
                break;
            }
            // Users
            case USER_UPDATE: {
                // TODO: This is self, do we care
                break;
            }
            case PRESENCE_UPDATE: {
                break;
            }
            // Voice
            case VOICE_STATE_UPDATE: {
                // TODO
                break;
            }
        }
        return this;
    }
    
    @Nullable
    @Override
    public Guild guild(@Nonnull final String id) {
        return guildCache.get(id);
    }
    
    @Nullable
    @Override
    public User user(@Nonnull final String id) {
        return userCache.get(id);
    }
    
    @Nullable
    @Override
    public Member member(@Nonnull final String guildId, @Nonnull final String id) {
        return null;
    }
    
    @Nullable
    @Override
    public Role role(@Nonnull final String guildId, @Nonnull final String id) {
        return null;
    }
    
    @Nullable
    @Override
    public Channel channel(@Nonnull final String guildId, @Nonnull final String id) {
        return null;
    }
    
    @Nonnull
    @Override
    public EntityCache catnip(@Nonnull final Catnip catnip) {
        this.catnip = catnip;
        entityBuilder = new EntityBuilder(catnip);
        return this;
    }
    
    @Nonnull
    @CheckReturnValue
    private static <T> Function<JsonArray, List<T>> mapObjectContents(@Nonnull final Function<JsonObject, T> builder) {
        return array -> {
            final Collection<T> result = new ArrayList<>(array.size());
            for(final Object object : array) {
                if(!(object instanceof JsonObject)) {
                    throw new IllegalArgumentException("Expected array to contain only objects, but found " +
                            (object == null ? "null" : object.getClass())
                    );
                }
                result.add(builder.apply((JsonObject) object));
            }
            return ImmutableList.copyOf(result);
        };
    }
}
