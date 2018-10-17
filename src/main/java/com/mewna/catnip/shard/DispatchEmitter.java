package com.mewna.catnip.shard;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.Snowflake;
import com.mewna.catnip.entity.impl.EntityBuilder;
import com.mewna.catnip.entity.misc.Ready;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.internal.CatnipImpl;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;

import static com.mewna.catnip.shard.DiscordEvent.Raw;

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
            
            // Guilds
            case Raw.GUILD_CREATE: {
                final String id = data.getString("id");
                if(catnip.isUnavailable(id)) {
                    catnip.eventBus().publish(Raw.GUILD_AVAILABLE, entityBuilder.createGuild(data));
                    ((CatnipImpl) catnip).markAvailable(id);
                } else {
                    catnip.eventBus().publish(type, entityBuilder.createGuild(data));
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
                    catnip.eventBus().publish(type, entityBuilder.createGuild(data, false));
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
                catnip.eventBus().publish(type, entityBuilder.createPresence(data));
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
            
            default: {
                catnip.logAdapter().warn("Got unimplemented gateway event: {}", type);
                break;
            }
        }
    }
}
