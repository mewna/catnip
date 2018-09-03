package com.mewna.catnip.shard;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.internal.CatnipImpl;
import com.mewna.catnip.entity.EntityBuilder;
import io.vertx.core.json.JsonObject;

import static com.mewna.catnip.shard.DiscordEvent.*;

/**
 * @author amy
 * @since 9/2/18.
 */
class DispatchEmitter {
    private final Catnip catnip;
    
    DispatchEmitter(final Catnip catnip) {
        this.catnip = catnip;
    }
    
    void emit(final JsonObject payload) {
        final String type = payload.getString("t");
        final JsonObject data = payload.getJsonObject("d");
        
        switch(type) {
            case MESSAGE_CREATE: {
                emitMessageCreate(data);
                break;
            }
            case GUILD_CREATE: {
                break;
            }
            case READY: {
                break;
            }
            case MESSAGE_UPDATE: {
                break;
            }
            case MESSAGE_DELETE: {
                break;
            }
            default: {
                break;
            }
        }
    }
    
    private void emitMessageCreate(final JsonObject data) {
        catnip.eventBus().send(MESSAGE_CREATE, EntityBuilder.createMessage(data));
    }
}
