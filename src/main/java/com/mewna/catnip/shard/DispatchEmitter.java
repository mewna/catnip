package com.mewna.catnip.shard;

import com.google.common.collect.ImmutableList;
import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.EntityBuilder;
import com.mewna.catnip.entity.Member;
import com.mewna.catnip.entity.Message;
import com.mewna.catnip.entity.Message.MessageType;
import com.mewna.catnip.entity.User;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.stream.Collectors;

import static com.mewna.catnip.shard.DiscordEvent.*;

/**
 * @author amy
 * @since 9/2/18.
 */
class DispatchEmitter {
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
        Catnip.eventBus().send(MESSAGE_CREATE, EntityBuilder.createMessage(data));
    }
}
