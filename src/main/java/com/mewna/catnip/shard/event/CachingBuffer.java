package com.mewna.catnip.shard.event;

import com.google.common.collect.ImmutableList;
import com.mewna.catnip.shard.DiscordEvent;
import io.vertx.core.json.JsonObject;
import lombok.Value;
import lombok.experimental.Accessors;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

import static com.mewna.catnip.shard.DiscordEvent.*;

/**
 * An implementation of {@link EventBuffer} used for the case of caching all
 * guilds sent in the {@code READY} payload, as well as for caching data as it
 * comes over the websocket connection.
 *
 * TODO: Actually cache data lol
 *
 * @author amy
 * @since 9/9/18.
 */
@SuppressWarnings("unused")
public class CachingBuffer extends AbstractBuffer {
    private static final List<String> CACHE_EVENTS = ImmutableList.copyOf(new String[] {
            // Channels
            CHANNEL_CREATE, CHANNEL_UPDATE, CHANNEL_DELETE,
            // Guilds
            GUILD_CREATE, GUILD_UPDATE, GUILD_DELETE,
            // Roles
            GUILD_ROLE_CREATE, GUILD_ROLE_UPDATE, GUILD_ROLE_DELETE,
            // Members
            GUILD_MEMBER_ADD, GUILD_MEMBER_REMOVE, GUILD_MEMBER_UPDATE,
            // Member chunking
            GUILD_MEMBERS_CHUNK,
            // Users
            USER_UPDATE, PRESENCE_UPDATE,
            // Voice
            VOICE_STATE_UPDATE,
    });
    
    private final Map<Integer, BufferState> buffers = new ConcurrentHashMap<>();
    
    @Override
    public void buffer(final JsonObject event) {
        final JsonObject shardData = event.getJsonObject("shard");
        final int id = shardData.getInteger("id");
        //final int limit = shardData.getInteger("limit");
        final String type = event.getString("t");
        
        final JsonObject d = event.getJsonObject("d");
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
                    final String guildId = d.getString("guild_id", null);
                    if(guildId != null) {
                        if(bufferState.readyGuilds().contains(guildId)) {
                            bufferState.buffer(event);
                        } else {
                            emitter().emit(event);
                        }
                    } else {
                        emitter().emit(event);
                    }
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
        private final Deque<JsonObject> buffer = new ConcurrentLinkedDeque<>();
        
        void recvGuild(final String id) {
            readyGuilds.remove(id);
        }
        
        void buffer(final JsonObject event) {
            buffer.addLast(event);
        }
        
        void replay() {
            final int count = buffer.size();
            buffer.forEach(emitter()::emit);
            catnip().logAdapter().info("Replayed {} buffered events", count);
        }
    }
}
