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

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.Snowflake;
import com.mewna.catnip.entity.guild.Guild;
import com.mewna.catnip.entity.impl.EntityBuilder;
import com.mewna.catnip.entity.misc.Ready;
import com.mewna.catnip.entity.misc.Resumed;
import com.mewna.catnip.entity.user.Presence.OnlineStatus;
import com.mewna.catnip.entity.user.PresenceUpdate;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.internal.CatnipImpl;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

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
    
    public void emit(@Nonnull final JsonObject payload) {
        if(!catnip.emitEventObjects()) {
            return;
        }
        final String type = payload.getString("t");
        if(catnip.disabledEvents().contains(type)) {
            return;
        }
        try {
            emit0(payload);
        } catch(final Exception e) {
            catnip.logAdapter().error("Error emitting event with payload {}", payload, e);
        }
    }
    
    private void emit0(@Nonnull final JsonObject payload) {
        final String type = payload.getString("t");
        final JsonObject data = payload.getJsonObject("d");
        
        switch(type) {
            // Lifecycle
            case Raw.READY: {
                final JsonArray guilds = data.getJsonArray("guilds");
                // All READY guilds are unavailable, marked available as the gateway
                // streams them to us
                guilds.stream()
                        .map(e -> (JsonObject) e)
                        .map(entityBuilder::createUnavailableGuild)
                        .map(Snowflake::id)
                        .forEach(((CatnipImpl) catnip)::markUnavailable);
                final Ready ready = entityBuilder.createReady(data);
                ((CatnipImpl) catnip).selfUser(ready.user());
                catnip.eventBus().publish(type, ready);
                break;
            }
            case Raw.RESUMED: {
                final Resumed resumed = entityBuilder.createResumed(data);
                catnip.eventBus().publish(type, resumed);
                break;
            }
            
            // Messages
            case Raw.MESSAGE_CREATE: {
                catnip.eventBus().publish(type, entityBuilder.createMessage(data));
                break;
            }
            case Raw.MESSAGE_UPDATE: {
                if(data.getJsonObject("author", null) == null) {
                    // Embeds update, emit the special case
                    catnip.eventBus().publish(Raw.MESSAGE_EMBEDS_UPDATE, entityBuilder.createMessageEmbedUpdate(data));
                } else {
                    catnip.eventBus().publish(type, entityBuilder.createMessage(data));
                }
                break;
            }
            case Raw.MESSAGE_DELETE: {
                catnip.eventBus().publish(type, entityBuilder.createDeletedMessage(data));
                break;
            }
            case Raw.MESSAGE_DELETE_BULK: {
                catnip.eventBus().publish(type, entityBuilder.createBulkDeletedMessages(data));
                break;
            }
            case Raw.TYPING_START: {
                catnip.eventBus().publish(type, entityBuilder.createTypingUser(data));
                break;
            }
            case Raw.MESSAGE_REACTION_REMOVE_ALL: {
                catnip.eventBus().publish(type, entityBuilder.createBulkRemovedReactions(data));
                break;
            }
            case Raw.MESSAGE_REACTION_REMOVE: {
                catnip.eventBus().publish(type, entityBuilder.createReactionUpdate(data));
                break;
            }
            case Raw.MESSAGE_REACTION_ADD: {
                catnip.eventBus().publish(type, entityBuilder.createReactionUpdate(data));
                break;
            }
            
            // Channels
            case Raw.CHANNEL_CREATE: {
                catnip.eventBus().publish(type, entityBuilder.createChannel(data));
                break;
            }
            case Raw.CHANNEL_UPDATE: {
                catnip.eventBus().publish(type, entityBuilder.createChannel(data));
                break;
            }
            case Raw.CHANNEL_DELETE: {
                catnip.eventBus().publish(type, entityBuilder.createChannel(data));
                break;
            }
            case Raw.CHANNEL_PINS_UPDATE: {
                catnip.eventBus().publish(type, entityBuilder.createChannelPinsUpdate(data));
                break;
            }
            case Raw.WEBHOOKS_UPDATE: {
                catnip.eventBus().publish(type, entityBuilder.createWebhooksUpdate(data));
                break;
            }
            
            // Guilds
            case Raw.GUILD_CREATE: {
                final String id = data.getString("id");
                final Guild guild = entityBuilder.createGuild(data);
                if(catnip.isUnavailable(id)) {
                    catnip.eventBus().publish(Raw.GUILD_AVAILABLE, guild);
                    ((CatnipImpl) catnip).markAvailable(id);
                } else {
                    catnip.eventBus().publish(type, guild);
                }
                break;
            }
            case Raw.GUILD_UPDATE: {
                catnip.eventBus().publish(type, entityBuilder.createGuild(data));
                break;
            }
            case Raw.GUILD_DELETE: {
                final String id = data.getString("id");
                if(data.getBoolean("unavailable", false)) {
                    ((CatnipImpl) catnip).markUnavailable(id);
                    catnip.eventBus().publish(Raw.GUILD_UNAVAILABLE, entityBuilder.createUnavailableGuild(data));
                } else {
                    catnip.eventBus().publish(type, entityBuilder.createGuild(data));
                }
                break;
            }
            case Raw.GUILD_BAN_ADD: {
                catnip.eventBus().publish(type, entityBuilder.createGatewayGuildBan(data));
                break;
            }
            case Raw.GUILD_BAN_REMOVE: {
                catnip.eventBus().publish(type, entityBuilder.createGatewayGuildBan(data));
                break;
            }
            case Raw.GUILD_INTEGRATIONS_UPDATE: {
                catnip.eventBus().publish(type, data.getString("guild_id"));
                break;
            }
            
            // Roles
            case Raw.GUILD_ROLE_CREATE: {
                catnip.eventBus().publish(type, entityBuilder.createRole(data.getString("guild_id"), data.getJsonObject("role")));
                break;
            }
            case Raw.GUILD_ROLE_UPDATE: {
                catnip.eventBus().publish(type, entityBuilder.createRole(data.getString("guild_id"), data.getJsonObject("role")));
                break;
            }
            case Raw.GUILD_ROLE_DELETE: {
                catnip.eventBus().publish(type, entityBuilder.createPartialRole(data.getString("guild_id"), data.getString("role_id")));
                break;
            }
            
            // Emoji
            case Raw.GUILD_EMOJIS_UPDATE: {
                catnip.eventBus().publish(type, entityBuilder.createGuildEmojisUpdate(data));
                break;
            }
            
            // Members
            case Raw.GUILD_MEMBER_ADD: {
                catnip.eventBus().publish(type, entityBuilder.createMember(data.getString("guild_id"), data));
                break;
            }
            case Raw.GUILD_MEMBER_REMOVE: {
                catnip.eventBus().publish(type, entityBuilder.createMember(data.getString("guild_id"), data));
                break;
            }
            case Raw.GUILD_MEMBER_UPDATE: {
                final String guild = data.getString("guild_id");
                catnip.eventBus().publish(type, entityBuilder.createPartialMember(guild, data));
                break;
            }
            
            // Users
            case Raw.USER_UPDATE: {
                final User user = entityBuilder.createUser(data);
                ((CatnipImpl) user).selfUser(user);
                catnip.eventBus().publish(type, user);
                break;
            }
            case Raw.PRESENCE_UPDATE: {
                final PresenceUpdate presence = entityBuilder.createPresenceUpdate(data);
                if(presence.status() == OnlineStatus.INVISIBLE) {
                    catnip.logAdapter().warn("Received a presence update with 'invisible' as the online status, " +
                            "but we should never get this. If you report this to Discord, include the following " +
                            "JSON in your report: {}", payload.encodePrettily());
                }
                catnip.eventBus().publish(type, presence);
                break;
            }
            
            // Voice
            case Raw.VOICE_STATE_UPDATE: {
                catnip.eventBus().publish(type, entityBuilder.createVoiceState(data));
                break;
            }
            case Raw.VOICE_SERVER_UPDATE: {
                catnip.eventBus().publish(type, entityBuilder.createVoiceServerUpdate(data));
                break;
            }
            
            // Other
            
            case Raw.GUILD_MEMBERS_CHUNK: {
                // End-users don't really have a use for an event here;
                // anyone who needs this will just be listening on raw
                // ws events anyway
                break;
            }
            
            default: {
                catnip.logAdapter().warn("Got unimplemented gateway event: {}", type);
                break;
            }
        }
    }
}
