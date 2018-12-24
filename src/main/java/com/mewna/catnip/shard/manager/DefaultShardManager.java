/*
 * Copyright (c) 2018 amy, All rights reserved.
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

package com.mewna.catnip.shard.manager;

import com.google.common.collect.ImmutableList;
import com.mewna.catnip.shard.CatnipShard;
import com.mewna.catnip.shard.CatnipShard.ShardConnectState;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.experimental.Accessors;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author amy
 * @since 8/15/18.
 */
@Accessors(fluent = true)
public class DefaultShardManager extends AbstractShardManager {
    @Getter
    private final Deque<Integer> connectQueue = new ConcurrentLinkedDeque<>();
    private final Collection<Integer> shardIds;
    @Getter
    private int shardCount;
    
    public DefaultShardManager() {
        this(0, new ArrayList<>());
    }
    
    @SuppressWarnings({"WeakerAccess", "unused"})
    public DefaultShardManager(@Nonnegative final int customShardCount) {
        this(IntStream.range(0, customShardCount));
    }
    
    @SuppressWarnings("WeakerAccess")
    public DefaultShardManager(final IntStream shardIds) {
        this(shardIds.boxed().collect(Collectors.toList()));
    }
    
    @SuppressWarnings("WeakerAccess")
    public DefaultShardManager(final Iterable<Integer> shardIds) {
        this(iterableLength(shardIds), iterableToCollection(shardIds));
    }
    
    @SuppressWarnings("WeakerAccess")
    public DefaultShardManager(@Nonnegative final int shardCount, final Collection<Integer> shardIds) {
        this.shardCount = shardCount;
        this.shardIds = new ArrayList<>(shardIds);
    }
    
    private static <T> int iterableLength(@Nonnull final Iterable<T> iterable) {
        int count = 0;
        for(final T ignored : iterable) {
            ++count;
        }
        return count;
    }
    
    private static <T> Collection<T> iterableToCollection(@Nonnull final Iterable<T> iterable) {
        final List<T> list = new ArrayList<>();
        iterable.forEach(list::add);
        return list;
    }
    
    @Override
    public Collection<Integer> shardIds() {
        return ImmutableList.copyOf(shardIds);
    }
    
    @Override
    public void start() {
        if(shardCount == 0) {
            // Load shard count from API
            catnip().rest().user().getGatewayBot().thenAccept(gatewayInfo -> {
                shardCount = gatewayInfo.shards();
                catnip().logAdapter().info("Loaded expected shard count: {}", shardCount);
                shardIds.clear();
                shardIds.addAll(IntStream.range(0, shardCount).boxed().collect(Collectors.toList()));
                loadShards();
            }).exceptionally(e -> {
                throw new IllegalStateException("Couldn't load shard count from API!", e);
            });
        } else {
            loadShards();
        }
    }
    
    private void loadShards() {
        catnip().logAdapter().info("Booting {}(/{}) shards", shardIds.size(), shardCount);
        
        // Deploy verticles
        for(final Integer id : shardIds) {
            deployShard(id, shardCount);
            catnip().logAdapter().info("Deployed shard {}/{}", id, shardCount);
        }
        // Start verticles
        poll();
    }
    
    private void poll() {
        if(connectQueue.isEmpty()) {
            catnip().vertx().setTimer(1000L, __ -> poll());
            return;
        }
        CompletableFuture.allOf(conditions().stream().map(ShardCondition::preshard).toArray(CompletableFuture[]::new))
                .thenAccept(__ -> connect())
                .exceptionally(e -> {
                    catnip().logAdapter().warn("Couldn't complete shard conditions, polling again in 1s", e);
                    catnip().vertx().setTimer(1000L, __ -> poll());
                    return null;
                });
    }
    
    private void connect() {
        final int nextId = connectQueue.removeFirst();
        catnip().logAdapter().info("Connecting shard {} (queue len {})", nextId, connectQueue.size());
        catnip().eventBus().<JsonObject>send(CatnipShard.controlAddress(nextId), new JsonObject().put("mode", "START"),
                reply -> {
                    if(reply.succeeded()) {
                        final ShardConnectState state = ShardConnectState.valueOf(reply.result().body().getString("state"));
                        switch(state) {
                            case READY:
                            case RESUMED: {
                                catnip().logAdapter().info("Connected shard {} with state {}", nextId, reply.result().body());
                                break;
                            }
                            case FAILED: {
                                catnip().logAdapter().warn("Failed connecting shard {}, re-queueing", nextId);
                                addToConnectQueue(nextId);
                                break;
                            }
                            default: {
                                catnip().logAdapter().error("Got unexpected / unknown shard connect state: {}", state);
                                break;
                            }
                        }
                        conditions().forEach(e -> e.postshard(state));
                    } else {
                        catnip().logAdapter().warn("Failed connecting shard {} entirely, re-queueing", nextId);
                        addToConnectQueue(nextId);
                    }
                    poll();
                });
    }
    
    @Override
    public void addToConnectQueue(@Nonnegative final int shard) {
        if(!connectQueue.contains(shard)) {
            connectQueue.add(shard);
        } else {
            catnip().logAdapter().warn("Ignoring duplicate queue for shard {}", shard);
        }
    }
    
    @Override
    public void shutdown() {
        shardIds.forEach(i -> {
            catnip().eventBus().send(CatnipShard.controlAddress(i), new JsonObject().put("mode", "SHUTDOWN"));
        });
    }
}
