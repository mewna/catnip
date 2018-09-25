package com.mewna.catnip.shard.event;

import com.google.common.collect.ImmutableList;
import io.vertx.core.json.JsonObject;
import lombok.Value;
import lombok.experimental.Accessors;

import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

import static com.mewna.catnip.shard.DiscordEvent.*;

/**
 * An implementation of {@link EventBuffer} used for the case of caching all
 * guilds sent in the {@code READY} payload, as well as for caching data as it
 * comes over the websocket connection.
 * <p>
 * TODO: Actually cache data lol
 *
 * @author amy
 * @since 9/9/18.
 */
@SuppressWarnings("unused")
public class CachingBuffer extends AbstractBuffer {
    private static final List<String> CACHE_EVENTS = ImmutableList.copyOf(new String[] {
            // Lifecycle
            READY,
            // Channels
            CHANNEL_CREATE, CHANNEL_UPDATE, CHANNEL_DELETE,
            // Guilds
            GUILD_CREATE, GUILD_UPDATE, GUILD_DELETE,
            // Roles
            GUILD_ROLE_CREATE, GUILD_ROLE_UPDATE, GUILD_ROLE_DELETE,
            // Emoji
            GUILD_EMOJIS_UPDATE,
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
            case READY: {
                final Set<String> guilds = d.getJsonArray("guilds").stream()
                        .map(e -> ((JsonObject) e).getString("id"))
                        .collect(Collectors.toSet());
                buffers.put(id, new BufferState(id, guilds));
                catnip().logAdapter().info("Prepared new BufferState for shard {} with {} guilds.", id, guilds.size());
                // READY is also a cache event, as it does come with
                // information about the current user
                maybeCache(type, d);
                break;
            }
            case GUILD_CREATE: {
                final String guild = d.getString("id");
                catnip().logAdapter().info("Got possibly-BufferState-ed guild {}", guild);
                final BufferState bufferState = buffers.get(id);
                // Make sure to cache guild
                maybeCache(type, d);
                if(bufferState != null) {
                    if(bufferState.readyGuilds().isEmpty()) {
                        // No guilds left, can just dispatch normally
                        catnip().logAdapter().info("BufferState for shard {} empty, removing and emitting.", id);
                        buffers.remove(id);
                        emitter().emit(event);
                    } else {
                        // Remove READY guild if necessary, otherwise buffer
                        if(bufferState.readyGuilds().contains(guild)) {
                            bufferState.recvGuild(guild);
                            catnip().logAdapter().info("Buffered guild {} for BufferState {}", guild, id);
                            // Replay all buffered events once we run out
                            if(bufferState.readyGuilds().isEmpty()) {
                                catnip().logAdapter().info("BufferState for {} empty, replaying {} events...", id, bufferState.buffer().size());
                                buffers.remove(id);
                                bufferState.replay();
                            }
                        } else {
                            catnip().logAdapter().info("Buffering event for BufferState {} @ {}", id, guild);
                            bufferState.buffer(event);
                        }
                    }
                }
                break;
            }
            default: {
                // Buffer and replay later
                final BufferState bufferState = buffers.get(id);
                if(bufferState != null) {
                    final String guildId = d.getString("guild_id", null);
                    if(guildId != null) {
                        if(bufferState.readyGuilds().contains(guildId)) {
                            bufferState.buffer(event);
                        } else {
                            // Emit if the payload is for a non-buffered guild
                            maybeCache(type, d);
                            emitter().emit(event);
                        }
                    } else {
                        // Emit if the payload has no guild id
                        maybeCache(type, d);
                        emitter().emit(event);
                    }
                } else {
                    // Emit if not buffering right now
                    maybeCache(type, d);
                    emitter().emit(event);
                }
                break;
            }
        }
    }
    
    private void maybeCache(final String eventType, final JsonObject payload) {
        if(CACHE_EVENTS.contains(eventType)) {
            try {
                catnip().cacheWorker().updateCache(eventType, payload);
            } catch(final Exception e) {
                catnip().logAdapter().warn("Got error updating cache for payload {}", eventType, e);
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
