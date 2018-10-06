package com.mewna.catnip.shard;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.impl.EntityBuilder;
import io.vertx.core.json.JsonObject;

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
    
    public DispatchEmitter(final Catnip catnip) {
        this.catnip = catnip;
        entityBuilder = new EntityBuilder(catnip);
    }
    
    public void emit(final JsonObject payload) {
        final String type = payload.getString("t");
        final JsonObject data = payload.getJsonObject("d");
        
        switch(type) {
            // Lifecycle
            case READY: {
                catnip.eventBus().send(type, entityBuilder.createReady(data));
                break;
            }
            
            // Messages
            case MESSAGE_CREATE: {
                catnip.eventBus().send(type, entityBuilder.createMessage(data));
                break;
            }
            case MESSAGE_UPDATE: {
                catnip.eventBus().send(type, entityBuilder.createMessage(data));
                break;
            }
            case MESSAGE_DELETE: {
                catnip.eventBus().send(type, entityBuilder.createDeletedMessage(data));
                break;
            }
            case MESSAGE_DELETE_BULK: {
                catnip.eventBus().send(type, entityBuilder.createBulkDeletedMessages(data));
                break;
            }
            case TYPING_START: {
                catnip.eventBus().send(type, entityBuilder.createTypingUser(data));
            }
            
            // Channels
            case CHANNEL_CREATE: {
                catnip.eventBus().send(type, entityBuilder.createChannel(data));
                break;
            }
            case CHANNEL_UPDATE: {
                catnip.eventBus().send(type, entityBuilder.createChannel(data));
                break;
            }
            case CHANNEL_DELETE: {
                catnip.eventBus().send(type, entityBuilder.createChannel(data));
                break;
            }
            
            // Guilds
            case GUILD_CREATE: {
                catnip.eventBus().send(type, entityBuilder.createGuild(data));
                break;
            }
            case GUILD_UPDATE: {
                catnip.eventBus().send(type, entityBuilder.createGuild(data));
                break;
            }
            case GUILD_DELETE: {
                catnip.eventBus().send(type, entityBuilder.createUnavailableGuild(data));
                break;
            }
            
            // Roles
            case GUILD_ROLE_CREATE: {
                catnip.eventBus().send(type, entityBuilder.createRole(data.getString("guild_id"), data.getJsonObject("role")));
                break;
            }
            case GUILD_ROLE_UPDATE: {
                catnip.eventBus().send(type, entityBuilder.createRole(data.getString("guild_id"), data.getJsonObject("role")));
                break;
            }
            case GUILD_ROLE_DELETE: {
                catnip.eventBus().send(type, entityBuilder.createPartialRole(data.getString("guild_id"), data.getString("role_id")));
                break;
            }
            
            // Emoji
            case GUILD_EMOJIS_UPDATE: {
                // TODO: ???
                catnip.logAdapter().warn("Got GUILD_EMOJIS_UPDATE, but this isn't implemented!");
                break;
            }
            
            // Members
            case GUILD_MEMBER_ADD: {
                catnip.eventBus().send(type, entityBuilder.createMember(data.getString("guild_id"), data));
                break;
            }
            case GUILD_MEMBER_REMOVE: {
                catnip.eventBus().send(type, entityBuilder.createMember(data.getString("guild_id"), data.getJsonObject("user")));
                break;
            }
            case GUILD_MEMBER_UPDATE: {
                final String guild = data.getString("guild_id");
                final JsonObject partialMember = new JsonObject()
                        .put("user", payload.getJsonObject("user"))
                        .put("roles", payload.getJsonArray("roles"))
                        .put("nick", payload.getString("nick"));
                
                catnip.eventBus().send(type, entityBuilder.createPartialMember(guild, partialMember));
                break;
            }
            
            // Users
            case USER_UPDATE: {
                catnip.eventBus().send(type, entityBuilder.createUser(data));
                break;
            }
            case PRESENCE_UPDATE: {
                catnip.eventBus().send(type, entityBuilder.createPresence(data));
                break;
            }
            
            // Voice
            case VOICE_STATE_UPDATE: {
                catnip.eventBus().send(type, entityBuilder.createVoiceState(data));
                break;
            }
            
            default: {
                catnip.logAdapter().warn("Got unimplemented gateway event: {}", type);
                break;
            }
        }
    }
}
