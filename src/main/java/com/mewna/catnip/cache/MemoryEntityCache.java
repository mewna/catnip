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

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.mewna.catnip.Catnip;
import com.mewna.catnip.cache.view.*;
import com.mewna.catnip.entity.builder.PresenceBuilder;
import com.mewna.catnip.entity.channel.Channel;
import com.mewna.catnip.entity.channel.GuildChannel;
import com.mewna.catnip.entity.channel.UserDMChannel;
import com.mewna.catnip.entity.guild.Guild;
import com.mewna.catnip.entity.guild.Member;
import com.mewna.catnip.entity.guild.Role;
import com.mewna.catnip.entity.misc.Emoji.CustomEmoji;
import com.mewna.catnip.entity.user.Presence;
import com.mewna.catnip.entity.user.Presence.OnlineStatus;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.entity.user.VoiceState;
import com.mewna.catnip.util.rx.RxHelpers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import lombok.Getter;
import lombok.experimental.Accessors;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.LongPredicate;

import static com.mewna.catnip.shard.DiscordEvent.Raw;
import static com.mewna.catnip.util.Utils.removeIf;

/**
 * @author amy
 * @since 9/18/18.
 */
@SuppressWarnings("ReactiveStreamsNullableInLambdaInTransform")
@Accessors(fluent = true, chain = true)
public abstract class MemoryEntityCache implements EntityCacheWorker {
    private static final Presence DEFAULT_PRESENCE = new PresenceBuilder().status(OnlineStatus.OFFLINE).build();
    
    @SuppressWarnings("WeakerAccess")
    protected final MutableNamedCacheView<Guild> guildCache = createGuildCacheView();
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
    protected final AtomicReference<User> selfUser = new AtomicReference<>(null);
    @Getter
    private Catnip catnip;
    
    @Override
    public boolean canProvidePreviousState(@Nonnull final CachedEntityState state) {
        return true;
    }
    
    /**
     * Function used to map members to their name, for named cache views.
     * Used by the default {@link #createMemberCacheView()} and
     * {@link #members()} implementations.
     * <p>
     * Defaults to returning a member's effective name, which is their
     * nickname, if present, or their username.
     *
     * @return Function used to map members to their name.
     */
    @SuppressWarnings("WeakerAccess")
    @Nonnull
    @CheckReturnValue
    protected Function<Member, String> memberNameFunction() {
        return m -> {
            if(m.nick() != null) {
                return m.nick();
            }
            // FIXME: THIS IS UNSAFE -- REWRITE CACHE VIEWS AS ASYNC
            final User u = user(m.idAsLong()).blockingGet();
            return u == null ? null : u.username();
        };
    }
    
    /**
     * Creates a new guild cache view. Subclasses can override this method to
     * use a different cache view implementation.
     *
     * @return A new guild cache view.
     *
     * @implNote Defaults to calling {@link #createNamedCacheView(Function)}.
     */
    @SuppressWarnings("WeakerAccess")
    @Nonnull
    @CheckReturnValue
    protected MutableNamedCacheView<Guild> createGuildCacheView() {
        return createNamedCacheView(Guild::name);
    }
    
    /**
     * Creates a new user cache view. Subclasses can override this method to
     * use a different cache view implementation.
     *
     * @return A new user cache view.
     *
     * @implNote Defaults to calling {@link #createNamedCacheView(Function)}.
     */
    @SuppressWarnings("WeakerAccess")
    @Nonnull
    @CheckReturnValue
    protected MutableNamedCacheView<User> createUserCacheView() {
        return createNamedCacheView(User::username);
    }
    
    /**
     * Creates a new DM channel cache view. Subclasses can override this method to
     * use a different cache view implementation.
     *
     * @return A new DM channel cache view.
     *
     * @implNote Defaults to calling {@link #createCacheView()}.
     */
    @SuppressWarnings("WeakerAccess")
    @Nonnull
    @CheckReturnValue
    protected MutableCacheView<UserDMChannel> createDMChannelCacheView() {
        return createCacheView();
    }
    
    /**
     * Creates a new presence cache view. Subclasses can override this method to
     * use a different cache view implementation.
     *
     * @return A new presence cache view.
     *
     * @implNote Defaults to calling {@link #createCacheView()}.
     */
    @SuppressWarnings("WeakerAccess")
    @Nonnull
    @CheckReturnValue
    protected MutableCacheView<Presence> createPresenceCacheView() {
        return createCacheView();
    }
    
    /**
     * Creates a new guild channel cache view. Subclasses can override this method to
     * use a different cache view implementation.
     *
     * @return A new guild channel cache view.
     *
     * @implNote Defaults to calling {@link #createNamedCacheView(Function)}.
     */
    @SuppressWarnings("WeakerAccess")
    @Nonnull
    @CheckReturnValue
    protected MutableNamedCacheView<GuildChannel> createGuildChannelCacheView() {
        return createNamedCacheView(GuildChannel::name);
    }
    
    /**
     * Creates a new role cache view. Subclasses can override this method to
     * use a different cache view implementation.
     *
     * @return A new role cache view.
     *
     * @implNote Defaults to calling {@link #createNamedCacheView(Function)}.
     */
    @SuppressWarnings("WeakerAccess")
    @Nonnull
    @CheckReturnValue
    protected MutableNamedCacheView<Role> createRoleCacheView() {
        return createNamedCacheView(Role::name);
    }
    
    /**
     * Creates a new member cache view. Subclasses can override this method to
     * use a different cache view implementation.
     *
     * @return A new member cache view.
     *
     * @implNote Defaults to calling {@link #createNamedCacheView(Function)}.
     */
    @SuppressWarnings("WeakerAccess")
    @Nonnull
    @CheckReturnValue
    protected MutableNamedCacheView<Member> createMemberCacheView() {
        return createNamedCacheView(memberNameFunction());
    }
    
    /**
     * Creates a new emoji cache view. Subclasses can override this method to
     * use a different cache view implementation.
     *
     * @return A new emoji cache view.
     *
     * @implNote Defaults to calling {@link #createNamedCacheView(Function)}.
     */
    @SuppressWarnings("WeakerAccess")
    @Nonnull
    @CheckReturnValue
    protected MutableNamedCacheView<CustomEmoji> createEmojiCacheView() {
        return createNamedCacheView(CustomEmoji::name);
    }
    
    /**
     * Creates a new voice state cache view. Subclasses can override this method to
     * use a different cache view implementation.
     *
     * @return A new voice state cache view.
     *
     * @implNote Defaults to calling {@link #createCacheView()}.
     */
    @SuppressWarnings("WeakerAccess")
    @Nonnull
    @CheckReturnValue
    protected MutableCacheView<VoiceState> createVoiceStateCacheView() {
        return createCacheView();
    }
    
    /**
     * Creates a new cache view. Subclasses can override this method to
     * use a different cache view implementation.
     *
     * @param <T> Type of the elements to be held by this view.
     *
     * @return A new cache view.
     */
    @SuppressWarnings("WeakerAccess")
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
    @SuppressWarnings("WeakerAccess")
    @Nonnull
    @CheckReturnValue
    protected <T> MutableNamedCacheView<T> createNamedCacheView(@Nonnull final Function<T, String> nameFunction) {
        return new DefaultNamedCacheView<>(nameFunction);
    }
    
    protected abstract MutableNamedCacheView<User> userCache(int shardId);
    
    protected abstract MutableCacheView<UserDMChannel> dmChannelCache(int shardId);
    
    protected abstract MutableCacheView<Presence> presenceCache(int shardId);
    
    @SuppressWarnings({"WeakerAccess", "unused", "RedundantSuppression"})
    protected MutableNamedCacheView<Guild> guildCache(final int shardId) {
        return guildCache;
    }
    
    @SuppressWarnings("WeakerAccess")
    protected MutableNamedCacheView<Member> memberCache(final long guildId, final boolean onlyGet) {
        return onlyGet ? memberCache.get(guildId) : memberCache.computeIfAbsent(guildId, __ -> createMemberCacheView());
    }
    
    @SuppressWarnings("WeakerAccess")
    protected void deleteMemberCache(final long guildId) {
        memberCache.remove(guildId);
    }
    
    @SuppressWarnings("WeakerAccess")
    protected MutableNamedCacheView<Role> roleCache(final long guildId, final boolean onlyGet) {
        return onlyGet ? roleCache.get(guildId) : roleCache.computeIfAbsent(guildId, __ -> createRoleCacheView());
    }
    
    @SuppressWarnings("WeakerAccess")
    protected void deleteRoleCache(final long guildId) {
        roleCache.remove(guildId);
    }
    
    @SuppressWarnings("WeakerAccess")
    protected MutableNamedCacheView<GuildChannel> channelCache(final long guildId, final boolean onlyGet) {
        return onlyGet ? guildChannelCache.get(guildId) : guildChannelCache.computeIfAbsent(guildId, __ -> createGuildChannelCacheView());
    }
    
    @SuppressWarnings("WeakerAccess")
    protected void deleteChannelCache(final long guildId) {
        guildChannelCache.remove(guildId);
    }
    
    @SuppressWarnings("WeakerAccess")
    protected MutableNamedCacheView<CustomEmoji> emojiCache(final long guildId, final boolean onlyGet) {
        return onlyGet ? emojiCache.get(guildId) : emojiCache.computeIfAbsent(guildId, __ -> createEmojiCacheView());
    }
    
    @SuppressWarnings("WeakerAccess")
    protected void deleteEmojiCache(final long guildId) {
        emojiCache.remove(guildId);
    }
    
    @SuppressWarnings("WeakerAccess")
    protected MutableCacheView<VoiceState> voiceStateCache(final long guildId, final boolean onlyGet) {
        return onlyGet ? voiceStateCache.get(guildId) : voiceStateCache.computeIfAbsent(guildId, __ -> createVoiceStateCacheView());
    }
    
    @SuppressWarnings("WeakerAccess")
    protected void deleteVoiceStateCache(final long guildId) {
        voiceStateCache.remove(guildId);
    }
    
    @SuppressWarnings("SameParameterValue")
    protected <T> Maybe<T> or(@Nullable final T data, final T def) {
        if(def == null) {
            return or(data);
        } else {
            return Maybe.just(data == null ? def : data);
        }
    }
    
    protected <T> Maybe<T> or(@Nullable final T data) {
        if(data == null) {
            return Maybe.<T>empty()
                    .observeOn(catnip.rxScheduler())
                    .subscribeOn(catnip.rxScheduler());
        } else {
            return Maybe.just(data)
                    .observeOn(catnip.rxScheduler())
                    .subscribeOn(catnip.rxScheduler());
        }
    }
    
    @Nonnull
    @Override
    public Maybe<Guild> guild(final long id) {
        return or(guildCache(shardId(id)).getById(id));
    }
    
    @Nonnull
    @Override
    public Maybe<User> user(final long id) {
        return or(users().getById(id));
    }
    
    @Nonnull
    @Override
    public Maybe<Presence> presence(final long id) {
        return or(presences().getById(id), DEFAULT_PRESENCE);
    }
    
    @Nonnull
    @Override
    public Maybe<Member> member(final long guildId, final long id) {
        return or(memberCache.get(guildId).getById(id));
    }
    
    @Nonnull
    @Override
    public Maybe<Role> role(final long guildId, final long id) {
        return or(roles(guildId).getById(id));
    }
    
    @Nonnull
    @Override
    public Maybe<GuildChannel> channel(final long guildId, final long id) {
        return or(channels(guildId).getById(id));
    }
    
    @Nonnull
    @Override
    public Maybe<CustomEmoji> emoji(final long guildId, final long id) {
        return or(emojis(guildId).getById(id));
    }
    
    @Nonnull
    @Override
    public Maybe<VoiceState> voiceState(final long guildId, final long id) {
        return or(voiceStates(guildId).getById(id));
    }
    
    @Nonnull
    @Override
    public Maybe<User> selfUser() {
        return or(selfUser.get());
    }
    
    protected int shardId(final long entityId) {
        return (int) ((entityId >> 22) % catnip.shardManager().shardCount());
    }
    
    private void cacheRole(final Role role) {
        roleCache(role.guildIdAsLong(), false).put(role.idAsLong(), role);
    }
    
    private void cacheMember(final Member member) {
        memberCache(member.guildIdAsLong(), false).put(member.idAsLong(), member);
    }
    
    private void cacheEmoji(final CustomEmoji emoji) {
        emojiCache(emoji.guildIdAsLong(), false).put(emoji.idAsLong(), emoji);
    }
    
    @SuppressWarnings("DuplicateBranchesInSwitch")
    @Nonnull
    @Override
    public Completable updateCache(@Nonnull final String eventType, @Nonnegative final int shardId, @Nonnull final JsonObject payload) {
        switch(eventType) {
            // Lifecycle
            case Raw.READY -> {
                selfUser.set(catnip.entityBuilder().createUser(payload.getObject("user")));
            }
            // Channels
            case Raw.CHANNEL_CREATE, Raw.CHANNEL_UPDATE -> {
                final Channel channel = catnip.entityBuilder().createChannel(payload);
                if(channel.isGuild()) {
                    final GuildChannel gc = (GuildChannel) channel;
                    channelCache(gc.guildIdAsLong(), false).put(gc.idAsLong(), gc);
                } else if(channel.isUserDM()) {
                    final UserDMChannel dm = (UserDMChannel) channel;
                    dmChannelCache(shardId).put(dm.idAsLong(), dm);
                } else {
                    catnip.logAdapter().warn("I don't know how to cache channel {}: isCategory={}, isDM={}, isGroupDM={}," +
                                    "isGuild={}, isText={}, isUserDM={}, isVoice={}",
                            channel.idAsLong(), channel.isCategory(), channel.isDM(), channel.isGroupDM(), channel.isGuild(),
                            channel.isText(), channel.isUserDM(), channel.isVoice());
                }
            }
            case Raw.CHANNEL_DELETE -> {
                final Channel channel = catnip.entityBuilder().createChannel(payload);
                if(channel.isGuild()) {
                    final GuildChannel gc = (GuildChannel) channel;
                    final MutableNamedCacheView<GuildChannel> channels = channelCache(gc.guildIdAsLong(), true);
                    if(channels != null) {
                        channels.remove(gc.idAsLong());
                    }
                } else if(channel.isUserDM()) {
                    final UserDMChannel dm = (UserDMChannel) channel;
                    dmChannelCache(shardId).remove(dm.userIdAsLong());
                } else {
                    catnip.logAdapter().warn("I don't know how to delete non-guild channel {}!", channel.idAsLong());
                }
            }
            // Guilds
            case Raw.GUILD_CREATE -> {
                final Guild guild = catnip.entityBuilder().createAndCacheGuild(shardId, payload);
                guildCache(shardId(guild.idAsLong())).put(guild.idAsLong(), guild);
            }
            case Raw.GUILD_UPDATE -> {
                final Guild guild = catnip.entityBuilder().createGuild(payload);
                guildCache(shardId(guild.idAsLong())).put(guild.idAsLong(), guild);
            }
            case Raw.GUILD_DELETE -> {
                final long guildId = Long.parseUnsignedLong(payload.getString("id"));
                guildCache(shardId(guildId)).remove(guildId);
                deleteMemberCache(guildId);
                deleteRoleCache(guildId);
                deleteChannelCache(guildId);
                deleteEmojiCache(guildId);
                deleteVoiceStateCache(guildId);
            }
            // Roles
            case Raw.GUILD_ROLE_CREATE -> {
                final String guild = payload.getString("guild_id");
                final JsonObject json = payload.getObject("role");
                final Role role = catnip.entityBuilder().createRole(guild, json);
                cacheRole(role);
            }
            case Raw.GUILD_ROLE_UPDATE -> {
                final String guild = payload.getString("guild_id");
                final JsonObject json = payload.getObject("role");
                final Role role = catnip.entityBuilder().createRole(guild, json);
                cacheRole(role);
            }
            case Raw.GUILD_ROLE_DELETE -> {
                final String guild = payload.getString("guild_id");
                final String role = payload.getString("role_id");
                final MutableCacheView<Role> cache = roleCache(Long.parseUnsignedLong(guild), true);
                if(cache != null) {
                    cache.remove(Long.parseUnsignedLong(role));
                }
            }
            // Members
            case Raw.GUILD_MEMBER_ADD -> {
                final Member member = catnip.entityBuilder().createMember(payload.getString("guild_id"), payload);
                final User user = catnip.entityBuilder().createUser(payload.getObject("user"));
                userCache(shardId).put(user.idAsLong(), user);
                cacheMember(member);
            }
            case Raw.GUILD_MEMBER_UPDATE -> {
                // This doesn't send an object like all the other events, so we build a fake
                // payload object and create an entity from that
                final JsonObject user = payload.getObject("user");
                final String id = user.getString("id");
                final String guild = payload.getString("guild_id");
            
                return Completable.fromMaybe(member(guild, id).map(old -> {
                    if(old != null) {
                        @SuppressWarnings("ConstantConditions")
                        final JsonObject data = JsonObject.builder()
                                .value("user", user)
                                .value("roles", payload.getArray("roles"))
                                .value("nick", payload.getString("nick"))
                                .value("deaf", old.deaf())
                                .value("mute", old.mute())
                                .value("joined_at", old.joinedAt()
                                        // If we have an old member cached, this shouldn't be an issue
                                        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                                .done();
                        final Member member = catnip.entityBuilder().createMember(guild, data);
                        cacheMember(member);
                    } else {
                        catnip.logAdapter().warn("Got GUILD_MEMBER_UPDATE for {} in {}, but we don't have them cached?!", id, guild);
                    }
                    return old;
                }));
            }
            case Raw.GUILD_MEMBER_REMOVE -> {
                final String guild = payload.getString("guild_id");
                final String user = payload.getObject("user").getString("id");
                final MutableCacheView<Member> cache = memberCache(Long.parseUnsignedLong(guild), true);
                if(cache != null) {
                    cache.remove(Long.parseUnsignedLong(user));
                }
            }
            // Member chunking
            case Raw.GUILD_MEMBERS_CHUNK -> {
                final String guild = payload.getString("guild_id");
                final JsonArray members = payload.getArray("members");
                members.stream().map(e -> catnip.entityBuilder().createMember(guild, (JsonObject) e)).forEach(this::cacheMember);
            }
            // Emojis
            case Raw.GUILD_EMOJIS_UPDATE -> {
                final String guild = payload.getString("guild_id");
                final JsonArray emojis = payload.getArray("emojis");
                emojis.stream().map(e -> catnip.entityBuilder().createCustomEmoji(guild, (JsonObject) e)).forEach(this::cacheEmoji);
            }
            // Currently-logged-in user
            case Raw.USER_UPDATE -> {
                // Inner payload is always a user object, according to the
                // docs, so we can just outright replace it.
                selfUser.set(catnip.entityBuilder().createUser(payload));
            }
            // Users
            case Raw.PRESENCE_UPDATE -> {
                final JsonObject user = payload.getObject("user");
                final String id = user.getString("id");
                return Completable.fromMaybe(user(id).map(old -> {
                    if(old == null && !catnip.options().chunkMembers() && catnip.options().logUncachedPresenceWhenNotChunking()) {
                        catnip.logAdapter().warn("Received PRESENCE_UPDATE for uncached user {}!?", id);
                    } else if(old != null) {
                        // This could potentially update:
                        // - username
                        // - discriminator
                        // - avatar
                        // so we check the existing cache for a user, and update as needed
                        final User updated = catnip.entityBuilder().createUser(JsonObject.builder()
                                .value("id", id)
                                .value("bot", old.bot())
                                .value("username", user.getString("username", old.username()))
                                .value("discriminator", user.getString("discriminator", old.discriminator()))
                                .value("avatar", user.getString("avatar", old.avatar()))
                                .done()
                        );
                        userCache(shardId).put(updated.idAsLong(), updated);
                        final Presence presence = catnip.entityBuilder().createPresence(payload);
                        presenceCache(shardId).put(updated.idAsLong(), presence);
                    } else if(catnip.options().chunkMembers()) {
                        final String guildId = payload.getString("guild_id", "No guild");
                        catnip.logAdapter().warn("Received PRESENCE_UPDATE for unknown user {} (guild: {})!? (member chunking enabled)",
                                id, guildId);
                    }
                    return old;
                }));
            }
            // Voice
            case Raw.VOICE_STATE_UPDATE -> {
                final VoiceState state = catnip.entityBuilder().createVoiceState(payload);
                cacheVoiceState(state);
            }
        }
        // Default case; most events don't need to have special future cases
        return RxHelpers.completedCompletable(catnip);
    }
    
    private void cacheVoiceState(final VoiceState state) {
        final long guild = state.guildIdAsLong();
        if(guild == 0) {
            catnip.logAdapter().warn("Not caching voice state for {} due to null guild", state.userIdAsLong());
            return;
        }
        voiceStateCache(guild, false).put(state.userIdAsLong(), state);
    }
    
    @Override
    public void bulkCacheUsers(@Nonnegative final int shardId, @Nonnull final Collection<User> users) {
        final MutableCacheView<User> cache = userCache(shardId);
        users.forEach(u -> cache.put(u.idAsLong(), u));
    }
    
    @Override
    public void bulkCacheChannels(@Nonnegative final int shardId, @Nonnull final Collection<GuildChannel> channels) {
        channels.forEach(gc -> channelCache(gc.guildIdAsLong(), false).put(gc.idAsLong(), gc));
    }
    
    @Override
    public void bulkCacheRoles(@Nonnegative final int shardId, @Nonnull final Collection<Role> roles) {
        roles.forEach(this::cacheRole);
    }
    
    @Override
    public void bulkCacheMembers(@Nonnegative final int shardId, @Nonnull final Collection<Member> members) {
        members.forEach(this::cacheMember);
    }
    
    @Override
    public void bulkCacheEmoji(@Nonnegative final int shardId, @Nonnull final Collection<CustomEmoji> emoji) {
        emoji.forEach(this::cacheEmoji);
    }
    
    @Override
    public void bulkCachePresences(@Nonnegative final int shardId, @Nonnull final Map<String, Presence> presences) {
        final MutableCacheView<Presence> cache = presenceCache(shardId);
        presences.forEach((id, presence) -> cache.put(Long.parseUnsignedLong(id), presence));
    }
    
    @Override
    public void bulkCacheVoiceStates(@Nonnegative final int shardId, @Nonnull final Collection<VoiceState> voiceStates) {
        voiceStates.forEach(this::cacheVoiceState);
    }
    
    @Override
    public void invalidateShard(final int id) {
        final int shardCount = catnip().shardManager().shardCount();
        final LongPredicate predicate = entityId -> (entityId >> 22) % shardCount == id;
        removeIf(memberCache, predicate);
        removeIf(roleCache, predicate);
        removeIf(guildChannelCache, predicate);
        removeIf(emojiCache, predicate);
        removeIf(voiceStateCache, predicate);
    }
    
    @Nonnull
    @Override
    public NamedCacheView<Guild> guilds() {
        return guildCache;
    }
    
    @Nonnull
    @Override
    public NamedCacheView<Member> members(final long guildId) {
        final MutableNamedCacheView<Member> cache = memberCache(guildId, true);
        return cache == null ? CacheView.noop() : cache;
    }
    
    @Nonnull
    @Override
    public NamedCacheView<Member> members() {
        return new CompositeNamedCacheView<>(memberCache.values(), memberNameFunction());
    }
    
    @Nonnull
    @Override
    public NamedCacheView<Role> roles(final long guildId) {
        final MutableNamedCacheView<Role> cache = roleCache(guildId, true);
        return cache == null ? CacheView.noop() : cache;
    }
    
    @Nonnull
    @Override
    public NamedCacheView<Role> roles() {
        return new CompositeNamedCacheView<>(roleCache.values(), Role::name);
    }
    
    @Nonnull
    @Override
    public NamedCacheView<GuildChannel> channels(final long guildId) {
        final MutableNamedCacheView<GuildChannel> cache = channelCache(guildId, true);
        return cache == null ? CacheView.noop() : cache;
    }
    
    @Nonnull
    @Override
    public NamedCacheView<GuildChannel> channels() {
        return new CompositeNamedCacheView<>(guildChannelCache.values(), GuildChannel::name);
    }
    
    @Nonnull
    @Override
    public NamedCacheView<CustomEmoji> emojis(final long guildId) {
        final MutableNamedCacheView<CustomEmoji> cache = emojiCache(guildId, true);
        return cache == null ? CacheView.noop() : cache;
    }
    
    @Nonnull
    @Override
    public NamedCacheView<CustomEmoji> emojis() {
        return new CompositeNamedCacheView<>(emojiCache.values(), CustomEmoji::name);
    }
    
    @Nonnull
    @Override
    public CacheView<VoiceState> voiceStates(final long guildId) {
        final MutableCacheView<VoiceState> cache = voiceStateCache(guildId, true);
        return cache == null ? CacheView.noop() : cache;
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
        return this;
    }
}
