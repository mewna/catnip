package com.mewna.catnip.shard;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.impl.EntityBuilder;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;

import static com.mewna.catnip.shard.DiscordEvent.*;

/**
 * TODO: This should be cache-aware so that we don't pay the cost of deserializing twice
 *
 * @author amy
 * @since 9/2/18.
 */
public class DispatchEmitter {
    private final Catnip catnip;
    private final EntityBuilder entityBuilder;
    
    public DispatchEmitter(@Nonnull final Catnip catnip) {
        this.catnip = catnip;
        entityBuilder = new EntityBuilder(catnip);
    }
    
    public void emit(@Nonnull final JsonObject payload) {
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
                catnip.eventBus().send(type, entityBuilder.createReady(data));
                break;
            }
            
            // Messages
            case Raw.MESSAGE_CREATE: {
                catnip.eventBus().send(type, entityBuilder.createMessage(data));
                break;
            }
            case Raw.MESSAGE_UPDATE: {
                catnip.eventBus().send(type, entityBuilder.createMessage(data));
                break;
            }
            case Raw.MESSAGE_DELETE: {
                catnip.eventBus().send(type, entityBuilder.createDeletedMessage(data));
                break;
            }
            case Raw.MESSAGE_DELETE_BULK: {
                catnip.eventBus().send(type, entityBuilder.createBulkDeletedMessages(data));
                break;
            }
            case Raw.TYPING_START: {
                catnip.eventBus().send(type, entityBuilder.createTypingUser(data));
                break;
            }
            case Raw.MESSAGE_REACTION_REMOVE_ALL: {
                catnip.eventBus().send(type, entityBuilder.createBulkRemovedReactions(data));
                break;
            }
            case Raw.MESSAGE_REACTION_REMOVE: {
                catnip.eventBus().send(type, entityBuilder.createReactionUpdate(data));
                break;
            }
            case Raw.MESSAGE_REACTION_ADD: {
                catnip.eventBus().send(type, entityBuilder.createReactionUpdate(data));
                break;
            }
            
            // Channels
            case Raw.CHANNEL_CREATE: {
                catnip.eventBus().send(type, entityBuilder.createChannel(data));
                break;
            }
            case Raw.CHANNEL_UPDATE: {
                catnip.eventBus().send(type, entityBuilder.createChannel(data));
                break;
            }
            case Raw.CHANNEL_DELETE: {
                catnip.eventBus().send(type, entityBuilder.createChannel(data));
                break;
            }
            
            // Guilds
            case Raw.GUILD_CREATE: {
                catnip.eventBus().send(type, entityBuilder.createGuild(data));
                break;
            }
            case Raw.GUILD_UPDATE: {
                catnip.eventBus().send(type, entityBuilder.createGuild(data));
                break;
            }
            case Raw.GUILD_DELETE: {
                catnip.eventBus().send(type, entityBuilder.createUnavailableGuild(data));
                break;
            }
            case Raw.GUILD_BAN_ADD: {
                catnip.eventBus().send(type, entityBuilder.createGatewayGuildBan(data));
                break;
            }
            case Raw.GUILD_BAN_REMOVE: {
                catnip.eventBus().send(type, entityBuilder.createGatewayGuildBan(data));
                break;
            }
            
            // Roles
            case Raw.GUILD_ROLE_CREATE: {
                catnip.eventBus().send(type, entityBuilder.createRole(data.getString("guild_id"), data.getJsonObject("role")));
                break;
            }
            case Raw.GUILD_ROLE_UPDATE: {
                catnip.eventBus().send(type, entityBuilder.createRole(data.getString("guild_id"), data.getJsonObject("role")));
                break;
            }
            case Raw.GUILD_ROLE_DELETE: {
                catnip.eventBus().send(type, entityBuilder.createPartialRole(data.getString("guild_id"), data.getString("role_id")));
                break;
            }
            
            // Emoji
            case Raw.GUILD_EMOJIS_UPDATE: {
                // TODO: ???
                catnip.logAdapter().warn("Got GUILD_EMOJIS_UPDATE, but this isn't implemented!");
                break;
            }
            
            // Members
            case Raw.GUILD_MEMBER_ADD: {
                catnip.eventBus().send(type, entityBuilder.createMember(data.getString("guild_id"), data));
                break;
            }
            case Raw.GUILD_MEMBER_REMOVE: {
                catnip.eventBus().send(type, entityBuilder.createMember(data.getString("guild_id"), data));
                break;
            }
            case Raw.GUILD_MEMBER_UPDATE: {
                final String guild = data.getString("guild_id");
                final JsonObject partialMember = new JsonObject()
                        .put("user", payload.getJsonObject("user"))
                        .put("roles", payload.getJsonArray("roles"))
                        .put("nick", payload.getString("nick"));
                
                catnip.eventBus().send(type, entityBuilder.createPartialMember(guild, partialMember));
                break;
            }
            
            // Users
            case Raw.USER_UPDATE: {
                catnip.eventBus().send(type, entityBuilder.createUser(data));
                break;
            }
            case Raw.PRESENCE_UPDATE: {
                catnip.eventBus().send(type, entityBuilder.createPresence(data));
                break;
            }
            
            // Voice
            case Raw.VOICE_STATE_UPDATE: {
                catnip.eventBus().send(type, entityBuilder.createVoiceState(data));
                break;
            }
            case Raw.VOICE_SERVER_UPDATE: {
                catnip.eventBus().send(type, entityBuilder.createVoiceServerUpdate(data));
                break;
            }
            
            default: {
                catnip.logAdapter().warn("Got unimplemented gateway event: {}", type);
                break;
            }
        }
    }
}
