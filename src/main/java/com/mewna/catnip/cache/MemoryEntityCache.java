package com.mewna.catnip.cache;

import com.google.common.collect.ImmutableList;
import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.channel.Channel;
import com.mewna.catnip.entity.channel.GuildChannel;
import com.mewna.catnip.entity.channel.UserDMChannel;
import com.mewna.catnip.entity.guild.Guild;
import com.mewna.catnip.entity.guild.Member;
import com.mewna.catnip.entity.guild.Role;
import com.mewna.catnip.entity.impl.EntityBuilder;
import com.mewna.catnip.entity.misc.Emoji.CustomEmoji;
import com.mewna.catnip.entity.user.Presence;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.entity.user.VoiceState;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.mewna.catnip.shard.DiscordEvent.Raw;

/**
 * @author amy
 * @since 9/18/18.
 */
@Accessors(fluent = true, chain = true)
@SuppressWarnings({"unused", "MismatchedQueryAndUpdateOfCollection"})
public class MemoryEntityCache implements EntityCacheWorker {
    // TODO: What even is efficiency
    
    private static final String DM_CHANNEL_KEY = "DMS";
    
    @SuppressWarnings("WeakerAccess")
    protected final Map<String, Guild> guildCache = new ConcurrentHashMap<>();
    @SuppressWarnings("WeakerAccess")
    protected final Map<String, User> userCache = new ConcurrentHashMap<>();
    @SuppressWarnings("WeakerAccess")
    protected final Map<String, Map<String, Member>> memberCache = new ConcurrentHashMap<>();
    @SuppressWarnings("WeakerAccess")
    protected final Map<String, Map<String, Role>> roleCache = new ConcurrentHashMap<>();
    @SuppressWarnings("WeakerAccess")
    protected final Map<String, Map<String, Channel>> channelCache = new ConcurrentHashMap<>();
    @SuppressWarnings("WeakerAccess")
    protected final Map<String, Map<String, CustomEmoji>> emojiCache = new ConcurrentHashMap<>();
    @SuppressWarnings("WeakerAccess")
    protected final Map<String, Map<String, VoiceState>> voiceStateCache = new ConcurrentHashMap<>();
    @SuppressWarnings("WeakerAccess")
    protected final Map<String, Presence> presenceCache = new ConcurrentHashMap<>();
    @SuppressWarnings("WeakerAccess")
    protected final AtomicReference<User> selfUser = new AtomicReference<>(null);
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
            final Map<String, Channel> channels = channelCache.computeIfAbsent(gc.guildId(), __ -> new ConcurrentHashMap<>());
            channels.put(gc.id(), gc);
        } else if(channel.isUserDM()) {
            final UserDMChannel dm = (UserDMChannel) channel;
            final Map<String, Channel> channels = channelCache.computeIfAbsent(DM_CHANNEL_KEY, __ -> new ConcurrentHashMap<>());
            // In this case in particular, this is safe because this method
            // will call into this cache and try to fetch the user that way.
            // The only possible way this could fail is if Discord sends us a
            // DM for a user we don't have cached, which is unlikely
            // TODO: Re-evaluate safety at some point
            //noinspection ConstantConditions
            channels.put(dm.recipient().id(), channel);
        } else {
            catnip.logAdapter().warn("I don't know how to cache channel {}: isCategory={}, isDM={}, isGroupDM={}," +
                            "isGuild={}, isText={}, isUserDM={}, isVoice={}",
                    channel.id(), channel.isCategory(), channel.isDM(), channel.isGroupDM(), channel.isGuild(),
                    channel.isText(), channel.isUserDM(), channel.isVoice());
        }
    }
    
    private void cacheRole(final Role role) {
        final Map<String, Role> roles = roleCache.computeIfAbsent(role.guildId(), __ -> new ConcurrentHashMap<>());
        roles.put(role.id(), role);
    }
    
    private void cacheUser(final User user) {
        userCache.put(user.id(), user);
    }
    
    private void cacheMember(final Member member) {
        final Map<String, Member> members = memberCache.computeIfAbsent(member.guildId(), __ -> new ConcurrentHashMap<>());
        members.put(member.id(), member);
    }
    
    private void cacheEmoji(final CustomEmoji emoji) {
        final Map<String, CustomEmoji> emojiMap = emojiCache.computeIfAbsent(emoji.guildId(), __ -> new ConcurrentHashMap<>());
        emojiMap.put(emoji.id(), emoji);
    }
    
    private void cachePresence(final String id, final Presence presence) {
        presenceCache.put(id, presence);
    }
    
    private void cacheVoiceState(final VoiceState state) {
        if(state.guildId() == null) {
            catnip.logAdapter().warn("Not caching voice state for {} due to null guild", state.userId());
            return;
        }
        final Map<String, VoiceState> states = voiceStateCache.computeIfAbsent(state.guildId(), __ -> new ConcurrentHashMap<>());
        states.put(state.userId(), state);
    }
    
    @Nonnull
    @Override
    public EntityCache updateCache(@Nonnull final String eventType, @Nonnull final JsonObject payload) {
        switch(eventType) {
            // Lifecycle
            case Raw.READY: {
                selfUser.set(entityBuilder.createUser(payload.getJsonObject("user")));
                break;
            }
            // Channels
            case Raw.CHANNEL_CREATE: {
                final Channel channel = entityBuilder.createChannel(payload);
                cacheChannel(channel);
                break;
            }
            case Raw.CHANNEL_UPDATE: {
                final Channel channel = entityBuilder.createChannel(payload);
                cacheChannel(channel);
                break;
            }
            case Raw.CHANNEL_DELETE: {
                final Channel channel = entityBuilder.createChannel(payload);
                if(channel.isGuild()) {
                    final GuildChannel gc = (GuildChannel) channel;
                    Map<String, Channel> channels = channelCache.get(gc.guildId());
                    if(channels == null) {
                        channels = new ConcurrentHashMap<>();
                        channelCache.put(gc.guildId(), channels);
                    }
                    channels.remove(gc.id());
                } else {
                    catnip.logAdapter().warn("I don't know how to delete non-guild channel {}!", channel.id());
                }
                break;
            }
            // Guilds
            case Raw.GUILD_CREATE: {
                final Guild guild = entityBuilder.createGuild(payload);
                guildCache.put(guild.id(), guild);
                break;
            }
            case Raw.GUILD_UPDATE: {
                final Guild guild = entityBuilder.createGuild(payload);
                guildCache.put(guild.id(), guild);
                break;
            }
            case Raw.GUILD_DELETE: {
                final Guild guild = entityBuilder.createGuild(payload);
                guildCache.remove(guild.id());
                break;
            }
            // Roles
            case Raw.GUILD_ROLE_CREATE: {
                final String guild = payload.getString("guild_id");
                final JsonObject json = payload.getJsonObject("role");
                final Role role = entityBuilder.createRole(guild, json);
                cacheRole(role);
                break;
            }
            case Raw.GUILD_ROLE_UPDATE: {
                final String guild = payload.getString("guild_id");
                final JsonObject json = payload.getJsonObject("role");
                final Role role = entityBuilder.createRole(guild, json);
                cacheRole(role);
                break;
            }
            case Raw.GUILD_ROLE_DELETE: {
                final String guild = payload.getString("guild_id");
                final String role = payload.getString("role_id");
                Optional.ofNullable(roleCache.get(guild)).ifPresent(e -> e.remove(role));
                break;
            }
            // Members
            case Raw.GUILD_MEMBER_ADD: {
                final Member member = entityBuilder.createMember(payload.getString("guild_id"), payload);
                final User user = entityBuilder.createUser(payload.getJsonObject("user"));
                cacheUser(user);
                cacheMember(member);
                break;
            }
            case Raw.GUILD_MEMBER_UPDATE: {
                // This doesn't send an object like all the other events, so we build a fake
                // payload object and create an entity from that
                final JsonObject user = payload.getJsonObject("user");
                final String id = user.getString("id");
                final String guild = payload.getString("guild_id");
                final Member old = member(guild, id);
                if(old != null) {
                    @SuppressWarnings("ConstantConditions")
                    final JsonObject data = new JsonObject()
                            .put("user", user)
                            .put("roles", payload.getJsonArray("roles"))
                            .put("nick", payload.getString("nick"))
                            .put("deaf", old.deaf())
                            .put("mute", old.mute())
                            .put("joined_at", old.joinedAt()
                                    // If we have an old member cached, this shouldn't be an issue
                                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                    final Member member = entityBuilder.createMember(guild, data);
                    cacheMember(member);
                } else {
                    catnip.logAdapter().warn("Got GUILD_MEMBER_UPDATE for {} in {}, but we don't have them cached?!", id, guild);
                }
                break;
            }
            case Raw.GUILD_MEMBER_REMOVE: {
                final String guild = payload.getString("guild_id");
                final String user = payload.getJsonObject("user").getString("id");
                Optional.ofNullable(memberCache.get(guild)).ifPresent(e -> e.remove(user));
                break;
            }
            // Member chunking
            case Raw.GUILD_MEMBERS_CHUNK: {
                final String guild = payload.getString("guild_id");
                final JsonArray members = payload.getJsonArray("members");
                members.stream().map(e -> entityBuilder.createMember(guild, (JsonObject) e)).forEach(this::cacheMember);
                break;
            }
            // Emojis
            case Raw.GUILD_EMOJIS_UPDATE: {
                if(!catnip.cacheFlags().contains(CacheFlag.DROP_EMOJI)) {
                    final String guild = payload.getString("guild_id");
                    final JsonArray emojis = payload.getJsonArray("emojis");
                    emojis.stream().map(e -> entityBuilder.createCustomEmoji(guild, (JsonObject) e)).forEach(this::cacheEmoji);
                }
                break;
            }
            // Currently-logged-in user
            case Raw.USER_UPDATE: {
                // Inner payload is always a user object, according to the
                // docs, so we can just outright replace it.
                selfUser.set(entityBuilder.createUser(payload));
                break;
            }
            // Users
            case Raw.PRESENCE_UPDATE: {
                final JsonObject user = payload.getJsonObject("user");
                final String id = user.getString("id");
                final User old = user(id);
                if(old == null && !catnip.chunkMembers()) {
                    catnip.logAdapter().warn("Received PRESENCE_UPDATE for uncached user {}!?", id);
                } else if(old != null) {
                    // This could potentially update:
                    // - username
                    // - discriminator
                    // - avatar
                    // so we check the existing cache for a user, and update as needed
                    final User updated = entityBuilder.createUser(new JsonObject()
                            .put("id", id)
                            .put("bot", old.bot())
                            .put("username", user.getString("username", old.username()))
                            .put("discriminator", user.getString("discriminator", old.discriminator()))
                            .put("avatar", user.getString("avatar", old.avatar()))
                    );
                    cacheUser(updated);
                    if(!catnip.cacheFlags().contains(CacheFlag.DROP_GAME_STATUSES)) {
                        final Presence presence = entityBuilder.createPresence(payload);
                        cachePresence(id, presence);
                    }
                } else if(catnip.chunkMembers()) {
                    catnip.logAdapter().warn("Received PRESENCE_UPDATE for unknown user {}!? (member chunking enabled)", id);
                }
                break;
            }
            // Voice
            case Raw.VOICE_STATE_UPDATE: {
                if(!catnip.cacheFlags().contains(CacheFlag.DROP_VOICE_STATES)) {
                    final VoiceState state = entityBuilder.createVoiceState(payload);
                    cacheVoiceState(state);
                }
                break;
            }
        }
        return this;
    }
    
    @Nonnull
    @Override
    public EntityCache bulkCacheUsers(@Nonnull final Collection<User> users) {
        users.forEach(this::cacheUser);
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
    
    @Nonnull
    @Override
    public EntityCache bulkCacheEmoji(@Nonnull final Collection<CustomEmoji> emoji) {
        emoji.forEach(this::cacheEmoji);
        return this;
    }
    
    @Nonnull
    @Override
    public EntityCache bulkCachePresences(@Nonnull final Map<String, Presence> presences) {
        presences.forEach(this::cachePresence);
        return this;
    }
    
    @Nonnull
    @Override
    public EntityCache bulkCacheVoiceStates(@Nonnull final Collection<VoiceState> voiceStates) {
        voiceStates.forEach(this::cacheVoiceState);
        return this;
    }
    
    @Nullable
    @Override
    public Guild guild(@Nonnull final String id) {
        return guildCache.get(id);
    }
    
    @Nonnull
    @Override
    public List<Guild> guilds() {
        return ImmutableList.copyOf(guildCache.values());
    }
    
    @Nullable
    @Override
    public User user(@Nonnull final String id) {
        return userCache.get(id);
    }
    
    @Nullable
    @Override
    public Presence presence(@Nonnull final String id) {
        return presenceCache.get(id);
    }
    
    @Nonnull
    @Override
    public List<Presence> presences() {
        return ImmutableList.copyOf(presenceCache.values());
    }
    
    @Nullable
    @Override
    public Member member(@Nonnull final String guildId, @Nonnull final String id) {
        if(memberCache.containsKey(guildId)) {
            return memberCache.get(guildId).get(id);
        } else {
            return null;
        }
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
    
    @Nonnull
    @Override
    public List<Member> members() {
        return ImmutableList.copyOf(memberCache.values().stream()
                .map(Map::values)
                .flatMap(Collection::stream)
                .collect(Collectors.toList()));
    }
    
    @Nonnull
    @Override
    public List<User> users() {
        return ImmutableList.copyOf(userCache.values());
    }
    
    @Nullable
    @Override
    public Role role(@Nonnull final String guildId, @Nonnull final String id) {
        if(roleCache.containsKey(guildId)) {
            return roleCache.get(guildId).get(id);
        } else {
            return null;
        }
    }
    
    @Nonnull
    @Override
    public List<Role> roles(@Nonnull final String guildId) {
        if(roleCache.containsKey(guildId)) {
            return ImmutableList.copyOf(roleCache.get(guildId).values());
        } else {
            return ImmutableList.of();
        }
    }
    
    @Nonnull
    @Override
    public List<Role> roles() {
        return ImmutableList.copyOf(roleCache.values().stream()
                .map(Map::values)
                .flatMap(Collection::stream)
                .collect(Collectors.toList()));
    }
    
    @Nullable
    @Override
    public Channel channel(@Nonnull final String guildId, @Nonnull final String id) {
        if(channelCache.containsKey(guildId)) {
            return channelCache.get(guildId).get(id);
        } else {
            return null;
        }
    }
    
    @Nonnull
    @Override
    public List<Channel> channels(@Nonnull final String guildId) {
        if(channelCache.containsKey(guildId)) {
            return ImmutableList.copyOf(channelCache.get(guildId).values());
        } else {
            return ImmutableList.of();
        }
    }
    
    @Nonnull
    @Override
    public List<Channel> channels() {
        return ImmutableList.copyOf(channelCache.values().stream().map(Map::values).flatMap(Collection::stream).collect(Collectors.toList()));
    }
    
    @Nullable
    @Override
    public CustomEmoji emoji(@Nonnull final String guildId, @Nonnull final String id) {
        if(emojiCache.containsKey(guildId)) {
            return emojiCache.get(guildId).get(id);
        } else {
            return null;
        }
    }
    
    @Nonnull
    @Override
    public List<CustomEmoji> emojis(@Nonnull final String guildId) {
        if(emojiCache.containsKey(guildId)) {
            return ImmutableList.copyOf(emojiCache.get(guildId).values());
        } else {
            return ImmutableList.of();
        }
    }
    
    @Nonnull
    @Override
    public List<CustomEmoji> emojis() {
        return ImmutableList.copyOf(emojiCache.values().stream()
                .map(Map::values)
                .flatMap(Collection::stream)
                .collect(Collectors.toList()));
    }
    
    @Nullable
    @Override
    public VoiceState voiceState(@Nullable final String guildId, @Nonnull final String id) {
        if(voiceStateCache.containsKey(guildId)) {
            return voiceStateCache.get(guildId).get(id);
        } else {
            return null;
        }
    }
    
    @Nonnull
    @Override
    public List<VoiceState> voiceStates(@Nonnull final String guildId) {
        if(voiceStateCache.containsKey(guildId)) {
            return ImmutableList.copyOf(voiceStateCache.get(guildId).values());
        } else {
            return ImmutableList.of();
        }
    }
    
    @Nonnull
    @Override
    public List<VoiceState> voiceState() {
        return ImmutableList.copyOf(voiceStateCache.values().stream()
                .map(Map::values)
                .flatMap(Collection::stream)
                .collect(Collectors.toList()));
    }
    
    @Nonnull
    @Override
    public EntityCache catnip(@Nonnull final Catnip catnip) {
        this.catnip = catnip;
        entityBuilder = new EntityBuilder(catnip);
        return this;
    }
}
