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
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static com.mewna.catnip.shard.DiscordEvent.*;

/**
 * @author amy
 * @since 9/18/18.
 */
@Accessors(fluent = true, chain = true)
@SuppressWarnings({"unused", "MismatchedQueryAndUpdateOfCollection"})
public class MemoryEntityCache implements EntityCacheWorker {
    // TODO: What even is efficiency
    
    private final Map<String, Guild> guildCache = new ConcurrentHashMap<>();
    private final Map<String, User> userCache = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Member>> memberCache = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Role>> roleCache = new ConcurrentHashMap<>();
    // TODO: How to handle DM channels?
    private final Map<String, Map<String, Channel>> channelCache = new ConcurrentHashMap<>();
    @Getter
    private Catnip catnip;
    private EntityBuilder entityBuilder;
    
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
    
    private void cacheChannel(final Channel channel) {
        if(channel.isGuild()) {
            final GuildChannel gc = (GuildChannel) channel;
            Map<String, Channel> channels = channelCache.get(gc.guildId());
            if(channels == null) {
                channels = new ConcurrentHashMap<>();
                channelCache.put(gc.guildId(), channels);
            }
            channels.put(gc.id(), gc);
            catnip.logAdapter().debug("Cached channel {} for guild {}", gc.id(), gc.guildId());
        } else {
            catnip.logAdapter().warn("I don't know how to cache non-guild channel {}!", channel.id());
        }
    }
    
    private void cacheRole(final Role role) {
        Map<String, Role> roles = roleCache.get(role.guildId());
        if(roles == null) {
            roles = new ConcurrentHashMap<>();
            roleCache.put(role.guildId(), roles);
        }
        roles.put(role.id(), role);
        catnip.logAdapter().debug("Cached role {} for guild {}", role.id(), role.guildId());
    }
    
    private void cacheUser(final User user) {
        userCache.put(user.id(), user);
        catnip.logAdapter().debug("Cached user {}", user.id());
    }
    
    private void cacheMember(final Member member) {
        Map<String, Member> members = memberCache.get(member.guildId());
        if(members == null) {
            members = new ConcurrentHashMap<>();
            memberCache.put(member.guildId(), members);
        }
        members.put(member.id(), member);
        catnip.logAdapter().debug("Cached member {} for guild {}", member.id(), member.guildId());
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
                    catnip.logAdapter().debug("Deleted channel {} for guild {}", gc.id(), gc.guildId());
                } else {
                    catnip.logAdapter().warn("I don't know how to delete non-guild channel {}!", channel.id());
                }
                break;
            }
            // Guilds
            case GUILD_CREATE: {
                final Guild guild = entityBuilder.createCachedGuild(payload);
                guildCache.put(guild.id(), guild);
                break;
            }
            case GUILD_UPDATE: {
                final Guild guild = entityBuilder.createCachedGuild(payload);
                guildCache.put(guild.id(), guild);
                break;
            }
            case GUILD_DELETE: {
                final Guild guild = entityBuilder.createCachedGuild(payload);
                guildCache.remove(guild.id());
                break;
            }
            // Roles
            case GUILD_ROLE_CREATE: {
                final String guild = payload.getString("guild_id");
                final JsonObject json = payload.getJsonObject("role");
                final Role role = entityBuilder.createRole(guild, json);
                cacheRole(role);
                break;
            }
            case GUILD_ROLE_UPDATE: {
                final String guild = payload.getString("guild_id");
                final JsonObject json = payload.getJsonObject("role");
                final Role role = entityBuilder.createRole(guild, json);
                cacheRole(role);
                break;
            }
            case GUILD_ROLE_DELETE: {
                final String guild = payload.getString("guild_id");
                final String role = payload.getString("role_id");
                Optional.ofNullable(roleCache.get(guild)).ifPresent(e -> e.remove(role));
                catnip.logAdapter().debug("Deleted role {} for guild {}", role, guild);
                break;
            }
            // Members
            case GUILD_MEMBER_ADD: {
                final Member member = entityBuilder.createMember(payload.getString("guild_id"), payload);
                final User user = entityBuilder.createUser(payload.getJsonObject("user"));
                cacheUser(user);
                cacheMember(member);
                break;
            }
            case GUILD_MEMBER_UPDATE: {
                // This doesn't send an object like all the other events, so we build a fake
                // payload object and create an entity from that
                final JsonObject user = payload.getJsonObject("user");
                final String id = user.getString("id");
                final String guild = payload.getString("guild_id");
                
                final Map<String, Member> members = memberCache.get(guild);
                if(members != null) {
                    final Member old = members.get(id);
                    if(old != null) {
                        final JsonObject data = new JsonObject()
                                .put("user", user)
                                .put("roles", payload.getJsonArray("roles"))
                                .put("nick", payload.getString("nick"))
                                .put("deaf", old.deaf())
                                .put("mute", old.mute())
                                .put("joined_at", old.joinedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                        final Member member = entityBuilder.createMember(guild, data);
                        cacheMember(member);
                    } else {
                        catnip.logAdapter().warn("Got GUILD_MEMBER_UPDATE for {} in {}, but we don't have them cached?!", id, guild);
                    }
                } else {
                    catnip.logAdapter().warn("Got GUILD_MEMBER_UPDATE for {} in {}, but we have no members cached?!", id, guild);
                }
                
                break;
            }
            case GUILD_MEMBER_REMOVE: {
                final String guild = payload.getString("guild_id");
                final String user = payload.getJsonObject("user").getString("id");
                Optional.ofNullable(memberCache.get(guild)).ifPresent(e -> e.remove(user));
                break;
            }
            // Member chunking
            case GUILD_MEMBERS_CHUNK: {
                final String guild = payload.getString("guild_id");
                final JsonArray members = payload.getJsonArray("members");
                members.stream().map(e -> entityBuilder.createMember(guild, (JsonObject) e)).forEach(this::cacheMember);
                catnip.logAdapter().debug("Processed chunk of {} members for guild {}", members.size(), guild);
                break;
            }
            // Users
            case USER_UPDATE: {
                // TODO: This is self, do we care?
                break;
            }
            case PRESENCE_UPDATE: {
                break;
            }
            // Voice
            case VOICE_STATE_UPDATE: {
                // TODO: How to cache this?
                // Bots can have a voice state in multiple guilds - how will we denote this?
                break;
            }
        }
        return this;
    }
    
    @Nonnull
    @Override
    public EntityCache bulkCacheChannels(@Nonnull final Collection<GuildChannel> channels) {
        channels.forEach(this::cacheChannel);
        return this;
    }
    
    @Nonnull
    @Override
    public EntityCache bulkCacheRoles(@Nonnull final Collection<Role> roles) {
        roles.forEach(this::cacheRole);
        return this;
    }
    
    @Nonnull
    @Override
    public EntityCache bulkCacheMembers(@Nonnull final Collection<Member> members) {
        members.forEach(this::cacheMember);
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
    
    @Nonnull
    @Override
    public List<Member> members(@Nonnull final String guildId) {
        if(memberCache.containsKey(guildId)) {
            return ImmutableList.copyOf(memberCache.get(guildId).values());
        } else {
            return ImmutableList.of();
        }
    }
    
    @Nullable
    @Override
    public Role role(@Nonnull final String guildId, @Nonnull final String id) {
        return null;
    }
    
    @Nonnull
    @Override
    public List<Role> roles(@Nonnull final String guildId) {
        if(memberCache.containsKey(guildId)) {
            return ImmutableList.copyOf(roleCache.get(guildId).values());
        } else {
            return ImmutableList.of();
        }
    }
    
    @Nullable
    @Override
    public Channel channel(@Nonnull final String guildId, @Nonnull final String id) {
        return null;
    }
    
    @Nonnull
    @Override
    public List<Channel> channels(@Nonnull final String guildId) {
        if(memberCache.containsKey(guildId)) {
            return ImmutableList.copyOf(channelCache.get(guildId).values());
        } else {
            return ImmutableList.of();
        }
    }
    
    @Nonnull
    @Override
    public EntityCache catnip(@Nonnull final Catnip catnip) {
        this.catnip = catnip;
        entityBuilder = new EntityBuilder(catnip);
        return this;
    }
}
