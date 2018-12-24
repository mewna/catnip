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
import com.mewna.catnip.cache.view.*;
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
import io.vertx.core.Future;
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

import static com.mewna.catnip.shard.DiscordEvent.Raw;

/**
 * @author amy
 * @since 9/18/18.
 */
@Accessors(fluent = true, chain = true)
@SuppressWarnings({"unused", "MismatchedQueryAndUpdateOfCollection"})
public class MemoryEntityCache implements EntityCacheWorker {
    @SuppressWarnings("WeakerAccess")
    protected final MutableNamedCacheView<Guild> guildCache = createGuildCacheView();
    @SuppressWarnings("WeakerAccess")
    protected final MutableNamedCacheView<User> userCache = createUserCacheView();
    @SuppressWarnings("WeakerAccess")
    protected final MutableCacheView<UserDMChannel> dmChannelCache = createDMChannelCacheView();
    @SuppressWarnings("WeakerAccess")
    protected final Map<Long, MutableNamedCacheView<Member>> memberCache = new ConcurrentHashMap<>();
    @SuppressWarnings("WeakerAccess")
    protected final Map<Long, MutableNamedCacheView<Role>> roleCache = new ConcurrentHashMap<>();
    @SuppressWarnings("WeakerAccess")
    protected final Map<Long, MutableNamedCacheView<GuildChannel>> guildChannelCache = new ConcurrentHashMap<>();
    @SuppressWarnings("WeakerAccess")
    protected final Map<Long, MutableNamedCacheView<CustomEmoji>> emojiCache = new ConcurrentHashMap<>();
    @SuppressWarnings("WeakerAccess")
    protected final Map<Long, MutableCacheView<VoiceState>> voiceStateCache = new ConcurrentHashMap<>();
    @SuppressWarnings("WeakerAccess")
    protected final MutableCacheView<Presence> presenceCache = createPresenceCacheView();
    @SuppressWarnings("WeakerAccess")
    protected final AtomicReference<User> selfUser = new AtomicReference<>(null);
    @Getter
    private Catnip catnip;
    private EntityBuilder entityBuilder;
    
    @SuppressWarnings("WeakerAccess")
    @Nonnull
    protected Function<Member, String> memberNameFunction() {
        return m -> {
            if(m.nick() != null) {
                return m.nick();
            }
            final User u = user(m.id());
            return u == null ? null : u.username();
        };
    }
    
    /**
     * Creates a new guild cache view. Subclasses can override this method to
     * use a different cache view implementation.
     *
     * @return A new guild cache view.
     *
     * @implNote Default to calling {@link #createNamedCacheView(Function)}
     */
    @SuppressWarnings("WeakerAccess")
    @Nonnull
    protected MutableNamedCacheView<Guild> createGuildCacheView() {
        return createNamedCacheView(Guild::name);
    }
    
    /**
     * Creates a new user cache view. Subclasses can override this method to
     * use a different cache view implementation.
     *
     * @return A new user cache view.
     *
     * @implNote Default to calling {@link #createNamedCacheView(Function)}
     */
    @SuppressWarnings("WeakerAccess")
    @Nonnull
    protected MutableNamedCacheView<User> createUserCacheView() {
        return createNamedCacheView(User::username);
    }
    
    /**
     * Creates a new DM channel cache view. Subclasses can override this method to
     * use a different cache view implementation.
     *
     * @return A new DM channel cache view.
     *
     * @implNote Default to calling {@link #createCacheView()}
     */
    @SuppressWarnings("WeakerAccess")
    @Nonnull
    protected MutableCacheView<UserDMChannel> createDMChannelCacheView() {
        return createCacheView();
    }
    
    /**
     * Creates a new presence cache view. Subclasses can override this method to
     * use a different cache view implementation.
     *
     * @return A new presence cache view.
     *
     * @implNote Default to calling {@link #createCacheView()}
     */
    @SuppressWarnings("WeakerAccess")
    @Nonnull
    protected MutableCacheView<Presence> createPresenceCacheView() {
        return createCacheView();
    }
    
    /**
     * Creates a new guild channel cache view. Subclasses can override this method to
     * use a different cache view implementation.
     *
     * @return A new guild channel cache view.
     *
     * @implNote Default to calling {@link #createNamedCacheView(Function)}
     */
    @SuppressWarnings("WeakerAccess")
    @Nonnull
    protected MutableNamedCacheView<GuildChannel> createGuildChannelCacheView() {
        return createNamedCacheView(GuildChannel::name);
    }
    
    /**
     * Creates a new role cache view. Subclasses can override this method to
     * use a different cache view implementation.
     *
     * @return A new role cache view.
     *
     * @implNote Default to calling {@link #createNamedCacheView(Function)}
     */
    @SuppressWarnings("WeakerAccess")
    @Nonnull
    protected MutableNamedCacheView<Role> createRoleCacheView() {
        return createNamedCacheView(Role::name);
    }
    
    /**
     * Creates a new member cache view. Subclasses can override this method to
     * use a different cache view implementation.
     *
     * @return A new member cache view.
     *
     * @implNote Default to calling {@link #createNamedCacheView(Function)}
     */
    @SuppressWarnings("WeakerAccess")
    @Nonnull
    protected MutableNamedCacheView<Member> createMemberCacheView() {
        return createNamedCacheView(memberNameFunction());
    }
    
    /**
     * Creates a new emoji cache view. Subclasses can override this method to
     * use a different cache view implementation.
     *
     * @return A new emoji cache view.
     *
     * @implNote Default to calling {@link #createNamedCacheView(Function)}
     */
    @SuppressWarnings("WeakerAccess")
    @Nonnull
    protected MutableNamedCacheView<CustomEmoji> createEmojiCacheView() {
        return createNamedCacheView(CustomEmoji::name);
    }
    
    /**
     * Creates a new cache view. Subclasses can override this method to
     * use a different cache view implementation.
     *
     * @param <T> Type of the elements to be held by this view.
     *
     * @return A new cache view.
     */
    @Nonnull
    @CheckReturnValue
    protected <T> MutableCacheView<T> createCacheView() {
        return new DefaultCacheView<>();
    }
    
    /**
     * Creates a new named cache view. Subclasses can override this method to
     * use a different cache view implementation.
     *
     * @param <T> Type of the elements to be held by this view.
     *
     * @return A new named cache view.
     */
    @Nonnull
    @CheckReturnValue
    protected <T> MutableNamedCacheView<T> createNamedCacheView(@Nonnull final Function<T, String> nameFunction) {
        return new DefaultNamedCacheView<>(nameFunction);
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
    
    private void cacheChannel(final Channel channel) {
        if(channel.isGuild()) {
            final GuildChannel gc = (GuildChannel) channel;
            guildChannelCache.computeIfAbsent(Long.parseUnsignedLong(gc.guildId()),
                    __ -> createGuildChannelCacheView())
                    .put(gc.id(), gc);
        } else if(channel.isUserDM()) {
            final UserDMChannel dm = (UserDMChannel) channel;
            dmChannelCache.put(dm.userId(), dm);
        } else {
            catnip.logAdapter().warn("I don't know how to cache channel {}: isCategory={}, isDM={}, isGroupDM={}," +
                            "isGuild={}, isText={}, isUserDM={}, isVoice={}",
                    channel.id(), channel.isCategory(), channel.isDM(), channel.isGroupDM(), channel.isGuild(),
                    channel.isText(), channel.isUserDM(), channel.isVoice());
        }
    }
    
    private void cacheRole(final Role role) {
        roleCache.computeIfAbsent(Long.parseUnsignedLong(role.guildId()), __ -> createRoleCacheView())
                .put(role.id(), role);
    }
    
    private void cacheUser(final User user) {
        userCache.put(user.id(), user);
    }
    
    private void cacheMember(final Member member) {
        memberCache.computeIfAbsent(Long.parseUnsignedLong(member.guildId()), __ -> createMemberCacheView())
                .put(member.id(), member);
    }
    
    private void cacheEmoji(final CustomEmoji emoji) {
        emojiCache.computeIfAbsent(Long.parseUnsignedLong(Objects.requireNonNull(emoji.guildId(), "Cannot cache emoji with null guild id!")),
                __ -> createEmojiCacheView())
                .put(emoji.id(), emoji);
    }
    
    private void cachePresence(final String id, final Presence presence) {
        presenceCache.put(id, presence);
    }
    
    @Nonnull
    @Override
    public Future<Void> updateCache(@Nonnull final String eventType, @Nonnull final JsonObject payload) {
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
                    final MutableNamedCacheView<GuildChannel> channels = guildChannelCache.get(Long.parseLong(gc.guildId()));
                    if(channels != null) {
                        channels.remove(gc.id());
                    }
                } else if(channel.isUserDM()) {
                    final UserDMChannel dm = (UserDMChannel) channel;
                    dmChannelCache.remove(dm.userId());
                } else {
                    catnip.logAdapter().warn("I don't know how to delete non-guild channel {}!", channel.id());
                }
                break;
            }
            // Guilds
            case Raw.GUILD_CREATE: {
                // This is wrapped in a blocking executor because there could
                // be cases of massive guilds that end blocking for a
                // significant amount of time while the guild is being cached.
                final Future<Void> future = Future.future();
                catnip().vertx().executeBlocking(f -> {
                    final Guild guild = entityBuilder.createGuild(payload);
                    guildCache.put(guild.id(), guild);
                    f.complete(null);
                }, __ -> future.complete(null));
                return future;
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
                Optional.ofNullable(roleCache.get(Long.parseLong(guild))).ifPresent(e -> e.remove(role));
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
                Optional.ofNullable(memberCache.get(Long.parseLong(guild))).ifPresent(e -> e.remove(user));
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
        // Default case; most events don't need to have special future cases
        return Future.succeededFuture();
    }
    
    private void cacheVoiceState(final VoiceState state) {
        final String guild = state.guildId();
        if(guild == null) {
            catnip.logAdapter().warn("Not caching voice state for {} due to null guild", state.userId());
            return;
        }
        voiceStateCache.computeIfAbsent(Long.parseUnsignedLong(guild), __ -> createCacheView())
                .put(state.userId(), state);
    }
    
    @Override
    public void bulkCacheUsers(@Nonnull final Collection<User> users) {
        users.forEach(this::cacheUser);
    }
    
    @Override
    public void bulkCacheChannels(@Nonnull final Collection<GuildChannel> channels) {
        channels.forEach(this::cacheChannel);
    }
    
    @Override
    public void bulkCacheRoles(@Nonnull final Collection<Role> roles) {
        roles.forEach(this::cacheRole);
    }
    
    @Override
    public void bulkCacheMembers(@Nonnull final Collection<Member> members) {
        members.forEach(this::cacheMember);
    }
    
    @Override
    public void bulkCacheEmoji(@Nonnull final Collection<CustomEmoji> emoji) {
        emoji.forEach(this::cacheEmoji);
    }
    
    @Override
    public void bulkCachePresences(@Nonnull final Map<String, Presence> presences) {
        presences.forEach(this::cachePresence);
    }
    
    @Override
    public void bulkCacheVoiceStates(@Nonnull final Collection<VoiceState> voiceStates) {
        voiceStates.forEach(this::cacheVoiceState);
    }
    
    @Nullable
    @Override
    public Guild guild(final long id) {
        return guildCache.getById(id);
    }
    
    @Nonnull
    @Override
    public NamedCacheView<Guild> guilds() {
        return guildCache;
    }
    
    @Nullable
    @Override
    public User user(final long id) {
        return userCache.getById(id);
    }
    
    @Nullable
    @Override
    public Presence presence(final long id) {
        return presenceCache.getById(id);
    }
    
    @Nonnull
    @Override
    public CacheView<Presence> presences() {
        return presenceCache;
    }
    
    @Nullable
    @Override
    public Member member(final long guildId, final long id) {
        final MutableNamedCacheView<Member> cache = memberCache.get(guildId);
        return cache == null ? null : cache.getById(id);
    }
    
    @Nonnull
    @Override
    public NamedCacheView<Member> members(final long guildId) {
        final MutableNamedCacheView<Member> cache = memberCache.get(guildId);
        return cache == null ? NamedCacheView.empty() : cache;
    }
    
    @Nonnull
    @Override
    public NamedCacheView<Member> members() {
        return new CompositeNamedCacheView<>(memberCache.values(), memberNameFunction());
    }
    
    @Nonnull
    @Override
    public NamedCacheView<User> users() {
        return userCache;
    }
    
    @Nullable
    @Override
    public Role role(final long guildId, final long id) {
        final MutableNamedCacheView<Role> cache = roleCache.get(guildId);
        return cache == null ? null : cache.getById(id);
    }
    
    @Nonnull
    @Override
    public NamedCacheView<Role> roles(final long guildId) {
        final MutableNamedCacheView<Role> cache = roleCache.get(guildId);
        return cache == null ? NamedCacheView.empty() : cache;
    }
    
    @Nonnull
    @Override
    public NamedCacheView<Role> roles() {
        return new CompositeNamedCacheView<>(roleCache.values(), Role::name);
    }
    
    @Nullable
    @Override
    public GuildChannel channel(final long guildId, final long id) {
        final MutableNamedCacheView<GuildChannel> cache = guildChannelCache.get(guildId);
        return cache == null ? null : cache.getById(id);
    }
    
    @Nonnull
    @Override
    public NamedCacheView<GuildChannel> channels(final long guildId) {
        final MutableNamedCacheView<GuildChannel> cache = guildChannelCache.get(guildId);
        return cache == null ? NamedCacheView.empty() : cache;
    }
    
    @Nonnull
    @Override
    public NamedCacheView<GuildChannel> channels() {
        return new CompositeNamedCacheView<>(guildChannelCache.values(), GuildChannel::name);
    }
    
    @Nullable
    @Override
    public UserDMChannel dmChannel(final long id) {
        return dmChannelCache.getById(id);
    }
    
    @Nonnull
    @Override
    public CacheView<UserDMChannel> dmChannels() {
        return dmChannelCache;
    }
    
    @Nullable
    @Override
    public CustomEmoji emoji(final long guildId, final long id) {
        final MutableNamedCacheView<CustomEmoji> cache = emojiCache.get(guildId);
        return cache == null ? null : cache.getById(id);
    }
    
    @Nonnull
    @Override
    public NamedCacheView<CustomEmoji> emojis(final long guildId) {
        final MutableNamedCacheView<CustomEmoji> cache = emojiCache.get(guildId);
        return cache == null ? NamedCacheView.empty() : cache;
    }
    
    @Nonnull
    @Override
    public NamedCacheView<CustomEmoji> emojis() {
        return new CompositeNamedCacheView<>(emojiCache.values(), CustomEmoji::name);
    }
    
    @Nullable
    @Override
    public VoiceState voiceState(final long guildId, final long id) {
        final MutableCacheView<VoiceState> cache = voiceStateCache.get(guildId);
        return cache == null ? null : cache.getById(id);
    }
    
    @Nonnull
    @Override
    public CacheView<VoiceState> voiceStates(final long guildId) {
        final MutableCacheView<VoiceState> cache = voiceStateCache.get(guildId);
        return cache == null ? CacheView.empty() : cache;
    }
    
    @Nonnull
    @Override
    public CacheView<VoiceState> voiceStates() {
        return new CompositeCacheView<>(voiceStateCache.values());
    }
    
    @Nonnull
    @Override
    public EntityCache catnip(@Nonnull final Catnip catnip) {
        this.catnip = catnip;
        entityBuilder = new EntityBuilder(catnip);
        return this;
    }
}
