package com.mewna.catnip.shard.event;

import com.mewna.catnip.shard.DiscordEvent;
import io.vertx.core.json.JsonObject;
import lombok.Value;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * An implementation of {@link EventBuffer} used for the case of caching all
 * guilds sent in the {@code READY} payload. This doc needs to be filled out
 * way more than this eventually.
 *
 * @author amy
 * @since 9/9/18.
 */
@SuppressWarnings("unused")
public class CachingBuffer extends AbstractBuffer {
    private final Map<Integer, BufferState> buffers = new ConcurrentHashMap<>();
    
    @Override
    public void buffer(final JsonObject event) {
        final JsonObject data = event.getJsonObject("shard");
        final int id = data.getInteger("id");
        //final int limit = data.getInteger("limit");
        final String type = event.getString("t");
        
        final JsonObject d = data.getJsonObject("d");
        switch(type) {
            case DiscordEvent.READY: {
                final Set<String> guilds = d.getJsonArray("guilds").stream()
                        .map(e -> ((JsonObject) e).getString("id"))
                        .collect(Collectors.toSet());
                buffers.put(id, new BufferState(id, guilds));
                break;
            }
            case DiscordEvent.GUILD_CREATE: {
                final String guild = d.getString("id");
                final BufferState bufferState = buffers.get(id);
                if(bufferState != null) {
                    if(bufferState.readyGuilds().isEmpty()) {
                        // No guilds left, can just dispatch normally
                        emitter().emit(event);
                    } else {
                        // Remove READY guild if necessary, otherwise buffer
                        if(bufferState.readyGuilds().contains(guild)) {
                            bufferState.recvGuild(guild);
                            // Replay all buffered events once we run out
                            if(bufferState.readyGuilds().isEmpty()) {
                                buffers.remove(id);
                                bufferState.replay();
                            }
                        } else {
                            bufferState.buffer(event);
                        }
                    }
                }
                break;
            }
            // TODO: Buffer events ONLY if a guild hasn't been created yet
            // This will prevent a single "stuck" guild from blocking everything,
            // as we learned from JDA.
            default: {
                // Buffer and replay later
                final BufferState bufferState = buffers.get(id);
                if(bufferState != null) {
                    bufferState.buffer(event);
                } else {
                    emitter().emit(event);
                }
                break;
            }
        }
    }
    
    @Value
    @Accessors(fluent = true)
    private final class BufferState {
        private int id;
        private final Set<String> readyGuilds;
        private final List<JsonObject> buffer = new CopyOnWriteArrayList<>();
        
        void recvGuild(final String id) {
            readyGuilds.remove(id);
        }
        
        void buffer(final JsonObject event) {
            buffer.add(event);
        }
        
        void replay() {
            buffer.forEach(emitter()::emit);
        }
    }
}
