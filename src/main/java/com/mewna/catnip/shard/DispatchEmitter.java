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

package com.mewna.catnip.shard;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.Entity;
import com.mewna.catnip.entity.guild.Guild;
import com.mewna.catnip.entity.guild.PartialMember;
import com.mewna.catnip.entity.guild.Role;
import com.mewna.catnip.entity.misc.Ready;
import com.mewna.catnip.entity.misc.Resumed;
import com.mewna.catnip.entity.partials.Snowflake;
import com.mewna.catnip.entity.user.Presence.OnlineStatus;
import com.mewna.catnip.entity.user.PresenceUpdate;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.internal.CatnipImpl;
import com.mewna.catnip.util.JsonUtil;
import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

import static com.mewna.catnip.cache.EntityCacheWorker.CachedEntityState.*;
import static com.mewna.catnip.shard.DiscordEvent.Raw;

/**
 * @author amy
 * @since 9/2/18.
 */
public final class DispatchEmitter {
    private final Catnip catnip;
    
    public DispatchEmitter(@Nonnull final Catnip catnip) {
        this.catnip = catnip;
    }
    
    public void emit(@Nonnull final String type, @Nonnull final Entity payload) {
        if(!catnip.options().emitEventObjects()) {
            return;
        }
        if(catnip.options().disabledEvents().contains(type)) {
            return;
        }
        try {
            catnip.dispatchManager().dispatchEvent(type, payload);
        } catch(final Exception e) {
            catnip.logAdapter().error("Error emitting event with payload {}", payload, e);
        }
    }
    
    public void emit(@Nonnull final JsonObject payload) {
        if(!catnip.options().emitEventObjects()) {
            return;
        }
        final String type = payload.getString("t");
        if(catnip.options().disabledEvents().contains(type)) {
            return;
        }
        try {
            emit0(payload);
        } catch(final Exception e) {
            catnip.logAdapter().error("Error emitting event with payload {}", payload, e);
        }
    }
    
    @SuppressWarnings({"DuplicateBranchesInSwitch", "ResultOfMethodCallIgnored", "DuplicatedCode"})
    private void emit0(@Nonnull final JsonObject payload) {
        final String type = payload.getString("t");
        final JsonObject data = payload.getObject("d");
        
        switch(type) {
            // Lifecycle
            case Raw.READY -> {
                final JsonArray guilds = data.getArray("guilds");
                // All READY guilds are unavailable, marked available as the gateway
                // streams them to us
                guilds.stream()
                        .map(e -> (JsonObject) e)
                        .map(catnip.entityBuilder()::createUnavailableGuild)
                        .map(Snowflake::id)
                        .forEach(((CatnipImpl) catnip)::markUnavailable);
                final Ready ready = catnip.entityBuilder().createReady(data);
                catnip.dispatchManager().dispatchEvent(type, ready);
            }
            case Raw.RESUMED -> {
                final Resumed resumed = catnip.entityBuilder().createResumed(data);
                catnip.dispatchManager().dispatchEvent(type, resumed);
            }
            
            // Messages
            case Raw.MESSAGE_CREATE -> catnip.dispatchManager().dispatchEvent(type, catnip.entityBuilder().createMessage(data));
            case Raw.MESSAGE_UPDATE -> {
                if(data.getObject("author", null) == null) {
                    // Embeds update, emit the special case
                    catnip.dispatchManager().dispatchEvent(Raw.MESSAGE_EMBEDS_UPDATE,
                            catnip.entityBuilder().createMessageEmbedUpdate(data));
                } else {
                    catnip.dispatchManager().dispatchEvent(type, catnip.entityBuilder().createMessage(data));
                }
            }
            case Raw.MESSAGE_DELETE -> catnip.dispatchManager().dispatchEvent(type, catnip.entityBuilder().createDeletedMessage(data));
            case Raw.MESSAGE_DELETE_BULK -> catnip.dispatchManager().dispatchEvent(type, catnip.entityBuilder().createBulkDeletedMessages(data));
            case Raw.TYPING_START -> catnip.dispatchManager().dispatchEvent(type, catnip.entityBuilder().createTypingUser(data));
            case Raw.MESSAGE_REACTION_REMOVE_ALL -> catnip.dispatchManager().dispatchEvent(type, catnip.entityBuilder().createBulkRemovedReactions(data));
            case Raw.MESSAGE_REACTION_REMOVE -> catnip.dispatchManager().dispatchEvent(type, catnip.entityBuilder().createReactionUpdate(data));
            case Raw.MESSAGE_REACTION_ADD -> catnip.dispatchManager().dispatchEvent(type, catnip.entityBuilder().createReactionUpdate(data));
            case Raw.MESSAGE_REACTION_REMOVE_EMOJI -> catnip.dispatchManager().dispatchEvent(type, catnip.entityBuilder().createReactionUpdate(data));
            
            // Channels
            case Raw.CHANNEL_CREATE -> catnip.dispatchManager().dispatchEvent(type, catnip.entityBuilder().createChannel(data));
            case Raw.CHANNEL_UPDATE -> {
                final var channel = catnip.entityBuilder().createChannel(data);
                if(catnip.cacheWorker().canProvidePreviousState(CHANNEL) && channel.isGuild()) {
                    catnip.cache().channel(channel.asGuildChannel().guildId(), channel.id())
                            .map(Optional::of)
                            .defaultIfEmpty(Optional.empty())
                            .subscribe(old -> catnip.dispatchManager().dispatchEvent(type, ImmutablePair.of(old.orElse(null), channel)),
                                    e -> cacheErrorLog(type, e));
                } else {
                    // We don't cache DM channels
                    catnip.dispatchManager().dispatchEvent(type, ImmutablePair.of(null, channel));
                }
            }
            case Raw.CHANNEL_DELETE -> catnip.dispatchManager().dispatchEvent(type, catnip.entityBuilder().createChannel(data));
            case Raw.CHANNEL_PINS_UPDATE -> catnip.dispatchManager().dispatchEvent(type, catnip.entityBuilder().createChannelPinsUpdate(data));
            case Raw.WEBHOOKS_UPDATE -> catnip.dispatchManager().dispatchEvent(type, catnip.entityBuilder().createWebhooksUpdate(data));
            case Raw.INVITE_CREATE -> catnip.dispatchManager().dispatchEvent(type, catnip.entityBuilder().createInvite(data));
            case Raw.INVITE_DELETE -> catnip.dispatchManager().dispatchEvent(type, catnip.entityBuilder().createDeletedInvite(data));
            
            // Guilds
            case Raw.GUILD_CREATE -> {
                final String id = data.getString("id");
                final Guild guild = catnip.entityBuilder().createGuild(data);
                if(catnip.isUnavailable(id)) {
                    catnip.dispatchManager().dispatchEvent(Raw.GUILD_AVAILABLE, guild);
                    ((CatnipImpl) catnip).markAvailable(id);
                } else {
                    catnip.dispatchManager().dispatchEvent(type, guild);
                }
            }
            case Raw.GUILD_UPDATE -> {
                final Guild guild = catnip.entityBuilder().createGuild(data);
                if(catnip.cacheWorker().canProvidePreviousState(GUILD)) {
                    catnip.cache().guild(guild.idAsLong())
                            .map(Optional::of)
                            .defaultIfEmpty(Optional.empty())
                            .subscribe(old -> catnip.dispatchManager().dispatchEvent(type, ImmutablePair.of(old.orElse(null), guild)),
                                    e -> cacheErrorLog(type, e));
                } else {
                    catnip.dispatchManager().dispatchEvent(type, ImmutablePair.of(null, guild));
                }
            }
            case Raw.GUILD_DELETE -> {
                final String id = data.getString("id");
                if(data.getBoolean("unavailable", false)) {
                    ((CatnipImpl) catnip).markUnavailable(id);
                    catnip.dispatchManager().dispatchEvent(Raw.GUILD_UNAVAILABLE, catnip.entityBuilder().createUnavailableGuild(data));
                } else {
                    catnip.dispatchManager().dispatchEvent(type, catnip.cache().guild(id));
                }
            }
            case Raw.GUILD_BAN_ADD -> catnip.dispatchManager().dispatchEvent(type, catnip.entityBuilder().createGatewayGuildBan(data));
            case Raw.GUILD_BAN_REMOVE -> catnip.dispatchManager().dispatchEvent(type, catnip.entityBuilder().createGatewayGuildBan(data));
            case Raw.GUILD_INTEGRATIONS_UPDATE -> catnip.dispatchManager().dispatchEvent(type, data.getString("guild_id"));
            
            // Roles
            case Raw.GUILD_ROLE_CREATE -> catnip.dispatchManager().dispatchEvent(type, catnip.entityBuilder().createRole(data.getString("guild_id"),
                    data.getObject("role")));
            case Raw.GUILD_ROLE_UPDATE -> {
                final Role role = catnip.entityBuilder().createRole(data.getString("guild_id"), data.getObject("role"));
                if(catnip.cacheWorker().canProvidePreviousState(ROLE)) {
                    catnip.cache().role(role.guildIdAsLong(), role.idAsLong())
                            .map(Optional::of)
                            .defaultIfEmpty(Optional.empty())
                            .subscribe(old -> catnip.dispatchManager().dispatchEvent(type, ImmutablePair.of(old.orElse(null), role)),
                                    e -> cacheErrorLog(type, e));
                } else {
                    catnip.dispatchManager().dispatchEvent(type, ImmutablePair.of(null, role));
                }
            }
            case Raw.GUILD_ROLE_DELETE -> catnip.dispatchManager().dispatchEvent(type, catnip.entityBuilder().createPartialRole(data.getString("guild_id"),
                    data.getString("role_id")));
            
            // Emoji
            case Raw.GUILD_EMOJIS_UPDATE -> catnip.dispatchManager().dispatchEvent(type, catnip.entityBuilder().createGuildEmojisUpdate(data));
            
            // Members
            case Raw.GUILD_MEMBER_ADD -> catnip.dispatchManager().dispatchEvent(type, catnip.entityBuilder().createMember(data.getString("guild_id"), data));
            case Raw.GUILD_MEMBER_REMOVE -> catnip.dispatchManager().dispatchEvent(type, catnip.entityBuilder().createMember(data.getString("guild_id"), data));
            case Raw.GUILD_MEMBER_UPDATE -> {
                final String guild = data.getString("guild_id");
                final PartialMember partialMember = catnip.entityBuilder().createPartialMember(guild, data);
                // TODO: Figure out firing a user update here
                if(data.has("user")) {
                    final var user = catnip.entityBuilder().createUser(data.getObject("user"));
                    catnip.cacheWorker().bulkCacheUsers(payload.getObject("shard").getInt("id"), List.of(user));
                }
                if(catnip.cacheWorker().canProvidePreviousState(MEMBER)) {
                    catnip.cache().member(partialMember.guildIdAsLong(), partialMember.idAsLong())
                            .map(Optional::of)
                            .defaultIfEmpty(Optional.empty())
                            .subscribe(old -> catnip.dispatchManager().dispatchEvent(type, ImmutablePair.of(old.orElse(null), partialMember)),
                                    e -> cacheErrorLog(type, e));
                } else {
                    catnip.dispatchManager().dispatchEvent(type, ImmutablePair.of(null, partialMember));
                }
            }
            
            // Threads
            case Raw.THREAD_CREATE -> catnip.dispatchManager().dispatchEvent(type, catnip.entityBuilder().createThreadChannel(data.getString("guild_id"), data));
            case Raw.THREAD_UPDATE -> {
                final var channel = catnip.entityBuilder().createThreadChannel(data.getString("guild_id"), data);
                if(catnip.cacheWorker().canProvidePreviousState(CHANNEL)) {
                    catnip.cache().member(channel.guildIdAsLong(), channel.idAsLong())
                            .map(Optional::of)
                            .defaultIfEmpty(Optional.empty())
                            .subscribe(old -> catnip.dispatchManager().dispatchEvent(type, ImmutablePair.of(old.orElse(null), channel)),
                                    e -> cacheErrorLog(type, e));
                } else {
                    catnip.dispatchManager().dispatchEvent(type, ImmutablePair.of(null, channel));
                }
            }
            case Raw.THREAD_DELETE -> catnip.dispatchManager().dispatchEvent(type, catnip.entityBuilder().createDeletedThread(data));
            case Raw.THREAD_LIST_SYNC -> catnip.dispatchManager().dispatchEvent(type, catnip.entityBuilder().createThreadListSync(data));
            case Raw.THREAD_MEMBER_UPDATE -> catnip.dispatchManager().dispatchEvent(type, catnip.entityBuilder().createThreadMember(data));
            case Raw.THREAD_MEMBERS_UPDATE -> catnip.dispatchManager().dispatchEvent(type, catnip.entityBuilder().createThreadMembersUpdate(data));
            
            // Users
            case Raw.USER_UPDATE -> {
                final User user = catnip.entityBuilder().createUser(data);
                if(catnip.cacheWorker().canProvidePreviousState(SELF_USER)) {
                    catnip.cache().user(user.idAsLong())
                            .map(Optional::of)
                            .defaultIfEmpty(Optional.empty())
                            .subscribe(old -> catnip.dispatchManager().dispatchEvent(type, ImmutablePair.of(old.orElse(null), user)),
                                    e -> cacheErrorLog(type, e));
                } else {
                    catnip.dispatchManager().dispatchEvent(type, ImmutablePair.of(null, user));
                }
            }
            case Raw.PRESENCE_UPDATE -> {
                final PresenceUpdate presence = catnip.entityBuilder().createPresenceUpdate(data);
                if(presence.status() == OnlineStatus.INVISIBLE) {
                    final JsonObject clone = new JsonObject(payload);
                    // catnip-internal key
                    clone.remove("shard");
                    catnip.logAdapter().warn("Received a presence update with 'invisible' as the online status, " +
                            "but we should never get this. If you report this to Discord, include the following " +
                            "JSON in your report:\n{}", JsonUtil.encodePrettily(clone));
                }
                if(catnip.cacheWorker().canProvidePreviousState(USER)) {
                    catnip.cache().user(presence.id()).subscribe(cachedUser -> {
                        if(cachedUser != null) {
                            final var discrim = Integer.parseInt(cachedUser.discriminator());
                            if(discrim < 1 || discrim > 9999) {
                                final JsonObject clone = new JsonObject(payload);
                                // catnip-internal key
                                clone.remove("shard");
                                catnip.logAdapter().warn("Received a presence update for a user with a discriminator of '{}', " +
                                                "but we should never get this. Discriminators should be clamped to [0001, 9999]." +
                                                "If you report this to Discord, include the following JSON in your report:\n{}",
                                        discrim, JsonUtil.encodePrettily(clone));
                            }
                        }
                    });
                }
                if(catnip.cacheWorker().canProvidePreviousState(PRESENCE)) {
                    catnip.cache().presence(presence.idAsLong())
                            .map(Optional::of)
                            .defaultIfEmpty(Optional.empty())
                            .subscribe(old -> catnip.dispatchManager().dispatchEvent(type, ImmutablePair.of(old.orElse(null), presence)),
                                    e -> cacheErrorLog(type, e));
                } else {
                    catnip.dispatchManager().dispatchEvent(type, ImmutablePair.of(null, presence));
                }
            }
            
            // Voice
            case Raw.VOICE_STATE_UPDATE -> catnip.dispatchManager().dispatchEvent(type, catnip.entityBuilder().createVoiceState(data));
            case Raw.VOICE_SERVER_UPDATE -> catnip.dispatchManager().dispatchEvent(type, catnip.entityBuilder().createVoiceServerUpdate(data));
            
            // Interactions
            case Raw.INTERACTION_CREATE -> catnip.dispatchManager().dispatchEvent(type, catnip.entityBuilder().createInteraction(data));
            case Raw.APPLICATION_COMMAND_CREATE,
                    Raw.APPLICATION_COMMAND_UPDATE,
                    Raw.APPLICATION_COMMAND_DELETE -> catnip.dispatchManager().dispatchEvent(type, catnip.entityBuilder().createApplicationCommand(data));
            
            // Other
            case Raw.GUILD_MEMBERS_CHUNK -> {
                // End-users don't really have a use for an event here;
                // anyone who needs this will just be listening on raw
                // ws events anyway
            }
            case Raw.GIFT_CODE_UPDATE -> {
                // See docs on Raw#GIFT_CODE_UPDATE for why this is here.
            }
            
            default -> catnip.logAdapter().warn("Got unimplemented gateway event: {}", type);
        }
    }
    
    private void cacheErrorLog(final String eventType, final Throwable e) {
        if(catnip.options().logEntityPresenceWarningOnCustomCache()) {
            catnip.logAdapter().error("Couldn't fetch previous entity from cache for update event {}:", eventType, e);
        }
    }
}
