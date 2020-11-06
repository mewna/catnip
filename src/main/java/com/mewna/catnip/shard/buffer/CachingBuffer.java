/*
 * Copyright (c) 2019 amy, All rights reserved.
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

package com.mewna.catnip.shard.buffer;

import com.grack.nanojson.JsonObject;
import com.mewna.catnip.entity.impl.lifecycle.ChunkingDoneImpl;
import com.mewna.catnip.entity.impl.lifecycle.MemberChunkRerequestImpl;
import com.mewna.catnip.shard.GatewayIntent;
import com.mewna.catnip.shard.LifecycleEvent;
import com.mewna.catnip.shard.LifecycleState;
import com.mewna.catnip.shard.ShardInfo;
import com.mewna.catnip.util.JsonUtil;
import com.mewna.catnip.util.rx.RxHelpers;
import io.reactivex.rxjava3.core.Completable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.Accessors;

import java.util.Deque;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import static com.mewna.catnip.shard.DiscordEvent.Raw;

/**
 * An implementation of {@link EventBuffer} used for the case of caching all
 * guilds sent in the {@code READY} payload, as well as for caching data as it
 * comes over the websocket connection.
 *
 * @author amy
 * @since 9/9/18.
 */
@SuppressWarnings("unused")
public class CachingBuffer extends AbstractBuffer {
    private static final Set<String> CACHE_EVENTS = Set.of(
            // Lifecycle
            Raw.READY,
            // Channels
            Raw.CHANNEL_CREATE, Raw.CHANNEL_UPDATE, Raw.CHANNEL_DELETE,
            // Guilds
            Raw.GUILD_CREATE, Raw.GUILD_UPDATE, Raw.GUILD_DELETE,
            // Roles
            Raw.GUILD_ROLE_CREATE, Raw.GUILD_ROLE_UPDATE, Raw.GUILD_ROLE_DELETE,
            // Emoji
            Raw.GUILD_EMOJIS_UPDATE,
            // Members
            Raw.GUILD_MEMBER_ADD, Raw.GUILD_MEMBER_REMOVE, Raw.GUILD_MEMBER_UPDATE,
            // Member chunking
            Raw.GUILD_MEMBERS_CHUNK,
            // Users
            Raw.USER_UPDATE, Raw.PRESENCE_UPDATE,
            // Voice
            Raw.VOICE_STATE_UPDATE
    );
    
    private static final Set<String> DELETE_EVENTS = Set.of(
            // Channels
            Raw.CHANNEL_DELETE,
            // Guilds
            Raw.GUILD_DELETE,
            // Roles
            Raw.GUILD_ROLE_DELETE,
            // Members
            Raw.GUILD_MEMBER_REMOVE
    );
    
    private final Map<Integer, BufferState> buffers = new ConcurrentHashMap<>();
    
    @Override
    public void buffer(final JsonObject event) {
        final JsonObject shardData = event.getObject("shard");
        final int id = shardData.getInt("id");
        final String type = event.getString("t");
        
        final JsonObject d = event.getObject("d");
        final BufferState bufferState = buffers.get(id);
        switch(type) {
            case Raw.READY: {
                handleReady(id, event);
                // In theory, we shouldn't need `bufferState != null` checks
                // beyond this point. The default vert.x event bus is
                // single-threaded, and #handleReady will already insert a
                // BufferState into the mappings for us.
                break;
            }
            case Raw.GUILD_CREATE: {
                handleGuildCreate(bufferState, event);
                break;
            }
            case Raw.GUILD_MEMBERS_CHUNK: {
                handleGuildMemberChunk(bufferState, event);
                break;
            }
            default: {
                // Buffer and replay later
                handleEvent(id, bufferState, event);
                break;
            }
        }
    }
    
    private void handleReady(final int shardId, final JsonObject event) {
        final JsonObject payloadData = event.getObject("d");
        final String eventType = event.getString("t");
        final Set<String> guilds = JsonUtil.toMutableSet(payloadData.getArray("guilds"), g -> g.getString("id"));
        buffers.put(shardId, new BufferState(shardId, guilds));
        catnip().logAdapter().debug("Prepared new BufferState for shard {} with {} guilds.", shardId, guilds.size());
        // READY is also a cache event, as it does come with
        // information about the current user
        //noinspection ResultOfMethodCallIgnored
        maybeCache(eventType, shardId, payloadData).subscribe(() -> emitter().emit(event));
    }
    
    private void handleGuildCreate(final BufferState bufferState, final JsonObject event) {
        final int shardId = bufferState.id();
        final JsonObject payloadData = event.getObject("d");
        final String guild = payloadData.getString("id");
        // Make sure to cache guild
        // This will always succeed unless something goes horribly wrong
        //noinspection ResultOfMethodCallIgnored
        maybeCache(Raw.GUILD_CREATE, shardId, payloadData).subscribe(() -> {
            // Add the guild to be awaited so that we can buffer members
            bufferState.awaitGuild(guild, event);
            
            // Trigger member chunking if needed
            final int memberCount = payloadData.getInt("member_count");
            // TODO: v6 pre-intents compatibility hack
            // REQUEST_GUILD_MEMBERS when using intents requires GUILD_MEMBERS
            // intent when requesting the entire guild member list when using
            // intents.
            // See https://github.com/discordapp/discord-api-docs/pull/1307#issuecomment-581561519
            final boolean canChunkViaIntents = catnip().options().intents().isEmpty()
                    || catnip().options().intents().contains(GatewayIntent.GUILD_MEMBERS);
            if (canChunkViaIntents && catnip().options().chunkMembers() && memberCount > catnip().options().largeThreshold()) {
                // Actually send the chunking request
                catnip().chunkMembers(guild);
                catnip().taskScheduler().setTimer(catnip().options().memberChunkTimeout(), taskId -> {
                    if (catnip().shardManager().shard(shardId).lifecycleState() != LifecycleState.LOGGED_IN) {
                        catnip().logAdapter().warn("Chunk rerequest task {} for disconnected shard {}, cancelling!",
                                taskId, shardId);
                        return;
                    }
                    if (bufferState.guildCreateCache().containsKey(guild)) {
                        catnip().logAdapter()
                                .warn("Didn't recv. member chunks for guild {} in time, re-requesting... " +
                                                "If you see this a lot, you should probably increase the value of " +
                                                "CatnipOptions#memberChunkTimeout.",
                                        guild);
                        if (catnip().options().manualChunkRerequesting()) {
                            emitter().emit(LifecycleEvent.Raw.MEMBER_CHUNK_REREQUEST,
                                    MemberChunkRerequestImpl.builder()
                                            .catnip(catnip())
                                            .guildId(guild)
                                            .shardInfo(new ShardInfo(shardId, catnip().shardManager().shardCount()))
                                            .build());
                        } else {
                            catnip().chunkMembers(guild);
                            catnip().taskScheduler().setTimer(catnip().options().memberChunkTimeout(), __ -> {
                                if (bufferState.guildCreateCache.containsKey(guild)) {
                                    catnip().logAdapter()
                                            .warn("Didn't recv. member chunks for guild {} after {}ms even " +
                                                            "after retrying! You should probably " +
                                                            "increase the value of CatnipOptions#memberChunkTimeout!",
                                                    guild, catnip().options().memberChunkTimeout());
                                }
                            });
                        }
                    }
                });
            } else {
                // Defer 100ms to try to wait for the guild role create event
                // that might come
                catnip().taskScheduler().setTimer(100L, __ -> {
                    emitter().emit(event);
                    bufferState.replayGuild(guild);
                    // Replay all buffered events once we run out
                    if (bufferState.awaitedGuilds().isEmpty()) {
                        bufferState.replay();
                    }
                });
            }
        });
    }
    
    private void handleGuildMemberChunk(final BufferState bufferState, final JsonObject event) {
        final String eventType = event.getString("t");
        final JsonObject payloadData = event.getObject("d");
        final int index = payloadData.getInt("chunk_index");
        final int count = payloadData.getInt("chunk_count");
        
        if (catnip().options().chunkMembers()) {
            final String guild = payloadData.getString("guild_id");
            cacheAndDispatch(eventType, bufferState.id(), event);
            // TODO: I assumed this was zero-based and didn't test. I should test it.
            if (index == count - 1) {
                emitter().emit(bufferState.guildCreate(guild));
                bufferState.replayGuild(guild);
                // Replay all buffered events once we run out
                if (bufferState.awaitedGuilds().isEmpty()) {
                    bufferState.replay();
                }
            }
        }
    }
    
    private void handleEvent(final int id, final BufferState bufferState, final JsonObject event) {
        final JsonObject payloadData = event.getObject("d");
        final String eventType = event.getString("t");
        
        final String guildId = payloadData.getString("guild_id", null);
        if (guildId != null) {
            if (bufferState.awaitedGuilds().contains(guildId)) {
                // If we have a guild id, and we have a guild being awaited,
                // buffer the event
                bufferState.receiveGuildEvent(guildId, event);
            } else {
                // If we're not awaiting the guild, it means that we're done
                // buffering events for the guild - ie. all member chunks have
                // been received - and so we can emit
                cacheAndDispatch(eventType, id, event);
            }
        } else {
            // Emit if the payload has no guild id
            cacheAndDispatch(eventType, id, event);
        }
    }
    
    private void cacheAndDispatch(final String type, final int id, final JsonObject event) {
        final JsonObject d = event.getObject("d");
        if (DELETE_EVENTS.contains(type)) {
            // TODO: Will this work always?
            emitter().emit(event);
            maybeCache(type, id, d);
        } else {
            //noinspection ResultOfMethodCallIgnored
            maybeCache(type, id, d).subscribe(() -> emitter().emit(event));
        }
    }
    
    private Completable maybeCache(final String eventType, final int shardId, final JsonObject data) {
        if (CACHE_EVENTS.contains(eventType)) {
            try {
                return catnip().cacheWorker().updateCache(eventType, shardId, data);
            } catch(final Exception e) {
                catnip().logAdapter().warn("Got error updating cache for payload {}", eventType, e);
                catnip().logAdapter().warn("Payload: {}", JsonUtil.encodePrettily(data));
                return RxHelpers.completedCompletable(catnip());
            }
        } else {
            return RxHelpers.completedCompletable(catnip());
        }
    }
    
    @Accessors(fluent = true)
    @AllArgsConstructor
    private static final class Counter {
        @Getter
        private final JsonObject guildCreate;
    }
    
    @Value
    @Accessors(fluent = true)
    private class BufferState {
        int id;
        Set<String> awaitedGuilds;
        Map<String, Deque<JsonObject>> guildBuffers = new ConcurrentHashMap<>();
        Map<String, JsonObject> guildCreateCache = new ConcurrentHashMap<>();
        Deque<JsonObject> buffer = new ConcurrentLinkedDeque<>();
        
        void awaitGuild(final String id, final JsonObject event) {
            awaitedGuilds.add(id);
            guildCreateCache.put(id, event);
        }
        
        void receiveGuildEvent(final String id, final JsonObject event) {
            final Deque<JsonObject> queue = guildBuffers.computeIfAbsent(id, __ -> new ConcurrentLinkedDeque<>());
            queue.addLast(event);
        }
        
        void buffer(final JsonObject event) {
            buffer.addLast(event);
        }
        
        void replayGuild(final String id) {
            awaitedGuilds.remove(id);
            guildCreateCache.remove(id);
            if (guildBuffers.containsKey(id)) {
                final Deque<JsonObject> queue = guildBuffers.get(id);
                queue.forEach(e -> cacheAndDispatch(e.getString("t"), this.id, e));
                guildBuffers.remove(id);
            }
        }
        
        void replay() {
            while(!buffer.isEmpty()) {
                // Properly empty the event buffer
                final JsonObject e = buffer.pop();
                cacheAndDispatch(e.getString("t"), id, e);
            }
            if (buffers.values().stream().allMatch(e -> e.buffer.isEmpty())) {
                // If all buffers are empty, emit an event saying as much
                emitter().emit(LifecycleEvent.Raw.CHUNKING_DONE, ChunkingDoneImpl.builder().catnip(catnip()).build());
            }
        }
        
        JsonObject guildCreate(final String guild) {
            return guildCreateCache.get(guild);
        }
    }
}
