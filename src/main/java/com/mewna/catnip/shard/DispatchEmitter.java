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
            case READY: {
                catnip.eventBus().send(READY, entityBuilder.createReady(data));
                break;
            }
            case MESSAGE_CREATE: {
                catnip.eventBus().send(MESSAGE_CREATE, entityBuilder.createMessage(data));
                break;
            }
            case GUILD_CREATE: {
                break;
            }
            case MESSAGE_UPDATE: {
                break;
            }
            case MESSAGE_DELETE: {
                catnip.eventBus().send(MESSAGE_DELETE, entityBuilder.createDeletedMessage(data));
                break;
            }
            case MESSAGE_DELETE_BULK: {
                catnip.eventBus().send(MESSAGE_DELETE_BULK, entityBuilder.createBulkDeletedMessages(data));
                break;
            }
            default: {
                break;
            }
        }
    }
}
