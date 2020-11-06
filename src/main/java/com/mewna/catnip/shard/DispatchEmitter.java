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
import com.mewna.catnip.entity.partials.Snowflake;
import com.mewna.catnip.entity.guild.Guild;
import com.mewna.catnip.entity.guild.PartialMember;
import com.mewna.catnip.entity.guild.Role;
import com.mewna.catnip.entity.impl.EntityBuilder;
import com.mewna.catnip.entity.misc.Ready;
import com.mewna.catnip.entity.misc.Resumed;
import com.mewna.catnip.entity.user.Presence.OnlineStatus;
import com.mewna.catnip.entity.user.PresenceUpdate;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.internal.CatnipImpl;
import com.mewna.catnip.util.JsonUtil;
import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.annotation.Nonnull;

import static com.mewna.catnip.shard.DiscordEvent.Raw;

/**
 * @author amy
 * @since 9/2/18.
 */
public final class DispatchEmitter {
    private final Catnip catnip;
    private final EntityBuilder entityBuilder;
    
    public DispatchEmitter(@Nonnull final Catnip catnip) {
        this.catnip = catnip;
        entityBuilder = new EntityBuilder(catnip);
    }
    
    public void emit(@Nonnull final String type, @Nonnull final Entity payload) {
        if (!catnip.options().emitEventObjects()) {
            return;
        }
        if (catnip.options().disabledEvents().contains(type)) {
            return;
        }
        try {
            catnip.dispatchManager().dispatchEvent(type, payload);
        } catch(final Exception e) {
            catnip.logAdapter().error("Error emitting event with payload {}", payload, e);
        }
    }
    
    public void emit(@Nonnull final JsonObject payload) {
        if (!catnip.options().emitEventObjects()) {
            return;
        }
        final String type = payload.getString("t");
        if (catnip.options().disabledEvents().contains(type)) {
            return;
        }
        try {
            emit0(payload);
        } catch(final Exception e) {
            catnip.logAdapter().error("Error emitting event with payload {}", payload, e);
        }
    }
    
    @SuppressWarnings({"DuplicateBranchesInSwitch", "ResultOfMethodCallIgnored"})
    private void emit0(@Nonnull final JsonObject payload) {
        final String type = payload.getString("t");
        final JsonObject data = payload.getObject("d");
        
        switch(type) {
            // Lifecycle
            case Raw.READY: {
                final JsonArray guilds = data.getArray("guilds");
                // All READY guilds are unavailable, marked available as the gateway
                // streams them to us
                guilds.stream()
                        .map(e -> (JsonObject) e)
                        .map(entityBuilder::createUnavailableGuild)
                        .map(Snowflake::id)
                        .forEach(((CatnipImpl) catnip)::markUnavailable);
                final Ready ready = entityBuilder.createReady(data);
                catnip.dispatchManager().dispatchEvent(type, ready);
                break;
            }
            case Raw.RESUMED: {
                final Resumed resumed = entityBuilder.createResumed(data);
                catnip.dispatchManager().dispatchEvent(type, resumed);
                break;
            }
            
            // Messages
            case Raw.MESSAGE_CREATE: {
                catnip.dispatchManager().dispatchEvent(type, entityBuilder.createMessage(data));
                break;
            }
            case Raw.MESSAGE_UPDATE: {
                if (data.getObject("author", null) == null) {
                    // Embeds update, emit the special case
                    catnip.dispatchManager().dispatchEvent(Raw.MESSAGE_EMBEDS_UPDATE,
                            entityBuilder.createMessageEmbedUpdate(data));
                } else {
                    catnip.dispatchManager().dispatchEvent(type, entityBuilder.createMessage(data));
                }
                break;
            }
            case Raw.MESSAGE_DELETE: {
                catnip.dispatchManager().dispatchEvent(type, entityBuilder.createDeletedMessage(data));
                break;
            }
            case Raw.MESSAGE_DELETE_BULK: {
                catnip.dispatchManager().dispatchEvent(type, entityBuilder.createBulkDeletedMessages(data));
                break;
            }
            case Raw.TYPING_START: {
                catnip.dispatchManager().dispatchEvent(type, entityBuilder.createTypingUser(data));
                break;
            }
            case Raw.MESSAGE_REACTION_REMOVE_ALL: {
                catnip.dispatchManager().dispatchEvent(type, entityBuilder.createBulkRemovedReactions(data));
                break;
            }
            case Raw.MESSAGE_REACTION_REMOVE: {
                catnip.dispatchManager().dispatchEvent(type, entityBuilder.createReactionUpdate(data));
                break;
            }
            case Raw.MESSAGE_REACTION_ADD: {
                catnip.dispatchManager().dispatchEvent(type, entityBuilder.createReactionUpdate(data));
                break;
            }
            case Raw.MESSAGE_REACTION_REMOVE_EMOJI: {
                catnip.dispatchManager().dispatchEvent(type, entityBuilder.createReactionUpdate(data));
                break;
            }
            
            // Channels
            case Raw.CHANNEL_CREATE: {
                catnip.dispatchManager().dispatchEvent(type, entityBuilder.createChannel(data));
                break;
            }
            case Raw.CHANNEL_UPDATE: {
                catnip.dispatchManager().dispatchEvent(type, entityBuilder.createChannel(data));
                break;
            }
            case Raw.CHANNEL_DELETE: {
                catnip.dispatchManager().dispatchEvent(type, entityBuilder.createChannel(data));
                break;
            }
            case Raw.CHANNEL_PINS_UPDATE: {
                catnip.dispatchManager().dispatchEvent(type, entityBuilder.createChannelPinsUpdate(data));
                break;
            }
            case Raw.WEBHOOKS_UPDATE: {
                catnip.dispatchManager().dispatchEvent(type, entityBuilder.createWebhooksUpdate(data));
                break;
            }
            case Raw.INVITE_CREATE: {
                catnip.dispatchManager().dispatchEvent(type, entityBuilder.createInvite(data));
                break;
            }
            case Raw.INVITE_DELETE: {
                catnip.dispatchManager().dispatchEvent(type, entityBuilder.createDeletedInvite(data));
            }
            
            // Guilds
            case Raw.GUILD_CREATE: {
                final String id = data.getString("id");
                final Guild guild = entityBuilder.createGuild(data);
                if (catnip.isUnavailable(id)) {
                    catnip.dispatchManager().dispatchEvent(Raw.GUILD_AVAILABLE, guild);
                    ((CatnipImpl) catnip).markAvailable(id);
                } else {
                    catnip.dispatchManager().dispatchEvent(type, guild);
                }
                break;
            }
            case Raw.GUILD_UPDATE: {
                final Guild guild = entityBuilder.createGuild(data);
                catnip.cache().guild(guild.idAsLong())
                        .subscribe(old -> catnip.dispatchManager().dispatchEvent(type, ImmutablePair.of(old, guild)), e -> cacheErrorLog(type, e));
                break;
            }
            case Raw.GUILD_DELETE: {
                final String id = data.getString("id");
                if (data.getBoolean("unavailable", false)) {
                    ((CatnipImpl) catnip).markUnavailable(id);
                    catnip.dispatchManager().dispatchEvent(Raw.GUILD_UNAVAILABLE, entityBuilder.createUnavailableGuild(data));
                } else {
                    catnip.dispatchManager().dispatchEvent(type, catnip.cache().guild(id));
                }
                break;
            }
            case Raw.GUILD_BAN_ADD: {
                catnip.dispatchManager().dispatchEvent(type, entityBuilder.createGatewayGuildBan(data));
                break;
            }
            case Raw.GUILD_BAN_REMOVE: {
                catnip.dispatchManager().dispatchEvent(type, entityBuilder.createGatewayGuildBan(data));
                break;
            }
            case Raw.GUILD_INTEGRATIONS_UPDATE: {
                catnip.dispatchManager().dispatchEvent(type, data.getString("guild_id"));
                break;
            }
            
            // Roles
            case Raw.GUILD_ROLE_CREATE: {
                catnip.dispatchManager().dispatchEvent(type, entityBuilder.createRole(data.getString("guild_id"),
                        data.getObject("role")));
                break;
            }
            case Raw.GUILD_ROLE_UPDATE: {
                final Role role = entityBuilder.createRole(data.getString("guild_id"), data.getObject("role"));
                catnip.cache().role(role.guildIdAsLong(), role.idAsLong())
                        .subscribe(old -> catnip.dispatchManager().dispatchEvent(type, ImmutablePair.of(old, role)), e -> cacheErrorLog(type, e));
                break;
            }
            case Raw.GUILD_ROLE_DELETE: {
                catnip.dispatchManager().dispatchEvent(type, entityBuilder.createPartialRole(data.getString("guild_id"),
                        data.getString("role_id")));
                break;
            }
            
            // Emoji
            case Raw.GUILD_EMOJIS_UPDATE: {
                catnip.dispatchManager().dispatchEvent(type, entityBuilder.createGuildEmojisUpdate(data));
                break;
            }
            
            // Members
            case Raw.GUILD_MEMBER_ADD: {
                catnip.dispatchManager().dispatchEvent(type, entityBuilder.createMember(data.getString("guild_id"), data));
                break;
            }
            case Raw.GUILD_MEMBER_REMOVE: {
                catnip.dispatchManager().dispatchEvent(type, entityBuilder.createMember(data.getString("guild_id"), data));
                break;
            }
            case Raw.GUILD_MEMBER_UPDATE: {
                final String guild = data.getString("guild_id");
                final PartialMember partialMember = entityBuilder.createPartialMember(guild, data);
                catnip.cache().member(partialMember.guildIdAsLong(), partialMember.idAsLong())
                        .subscribe(old -> catnip.dispatchManager().dispatchEvent(type, ImmutablePair.of(old, partialMember)), e -> cacheErrorLog(type, e));
                break;
            }
            
            // Users
            case Raw.USER_UPDATE: {
                final User user = entityBuilder.createUser(data);
                catnip.cache().selfUser()
                        .subscribe(old -> catnip.dispatchManager().dispatchEvent(type, ImmutablePair.of(old, user)), e -> cacheErrorLog(type, e));
                break;
            }
            case Raw.PRESENCE_UPDATE: {
                final PresenceUpdate presence = entityBuilder.createPresenceUpdate(data);
                if (presence.status() == OnlineStatus.INVISIBLE) {
                    final JsonObject clone = new JsonObject(payload);
                    // catnip-internal key
                    clone.remove("shard");
                    catnip.logAdapter().warn("Received a presence update with 'invisible' as the online status, " +
                            "but we should never get this. If you report this to Discord, include the following " +
                            "JSON in your report:\n{}", JsonUtil.encodePrettily(clone));
                }
                catnip.cache().user(presence.id()).subscribe(cachedUser -> {
                    if (cachedUser != null) {
                        final var discrim = Integer.parseInt(cachedUser.discriminator());
                        if (discrim < 1 || discrim > 9999) {
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
                catnip.cache().presence(presence.idAsLong())
                        .subscribe(old -> catnip.dispatchManager().dispatchEvent(type, ImmutablePair.of(old, presence)), e -> cacheErrorLog(type, e));
                break;
            }
            
            // Voice
            case Raw.VOICE_STATE_UPDATE: {
                catnip.dispatchManager().dispatchEvent(type, entityBuilder.createVoiceState(data));
                break;
            }
            case Raw.VOICE_SERVER_UPDATE: {
                catnip.dispatchManager().dispatchEvent(type, entityBuilder.createVoiceServerUpdate(data));
                break;
            }
            
            // Other
            case Raw.GUILD_MEMBERS_CHUNK: {
                // End-users don't really have a use for an event here;
                // anyone who needs this will just be listening on raw
                // ws events anyway
                break;
            }
            case Raw.GIFT_CODE_UPDATE: {
                // See docs on Raw#GIFT_CODE_UPDATE for why this is here.
                break;
            }
            
            default: {
                catnip.logAdapter().warn("Got unimplemented gateway event: {}", type);
                break;
            }
        }
    }
    
    private void cacheErrorLog(final String eventType, final Throwable e) {
        if (catnip.options().logEntityPresenceWarningOnCustomCache()) {
            catnip.logAdapter().error("Couldn't fetch previous entity from cache for update event {}:", eventType, e);
        }
    }
}
