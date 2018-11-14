package com.mewna.catnip.shard.manager;

import com.google.common.collect.ImmutableList;
import com.mewna.catnip.Catnip;
import com.mewna.catnip.shard.CatnipShard;
import com.mewna.catnip.shard.CatnipShard.ShardConnectState;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.experimental.Accessors;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.io.IOException;
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
    @Getter
    private OkHttpClient client;
    
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
        client = new OkHttpClient();
        if(shardCount == 0) {
            // Load shard count from API
            catnip().vertx().<JsonObject>executeBlocking(future -> {
                try {
                    @SuppressWarnings({"UnnecessarilyQualifiedInnerClassAccess", "ConstantConditions"})
                    final String body = client.newCall(new Request.Builder()
                            .get().url(Catnip.getShardCountUrl())
                            .header("Authorization", "Bot " + catnip().token())
                            .build()).execute().body().string();
                    future.complete(new JsonObject(body));
                } catch(final IOException | NullPointerException e) {
                    future.fail(e);
                }
            }, res -> {
                if(res.succeeded()) {
                    final JsonObject body = res.result();
                    final int shards = body.getInteger("shards", -1);
                    if(shards != -1) {
                        // Update from API
                        shardCount = shards;
                        catnip().logAdapter().info("Loaded expected shard count: {}", shardCount);
                        shardIds.clear();
                        shardIds.addAll(IntStream.range(0, shardCount).boxed().collect(Collectors.toList()));
                        loadShards();
                    } else {
                        throw new IllegalStateException("Invalid token provided (Gateway JSON response doesn't have `shards` key)!");
                    }
                } else {
                    throw new IllegalStateException("Couldn't load shard count from API!", res.cause());
                }
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
        CompletableFuture.allOf(conditions().stream().map(ShardCondition::get).toArray(CompletableFuture[]::new))
                .thenAccept(__ -> connect())
                .exceptionally(e -> {
                    catnip().logAdapter().warn("Couldn't complete shard conditions, polling again in 1s", e);
                    catnip().vertx().setTimer(1000L, __ -> poll());
                    return null;
                });
    }
    
    private void connect() {
        if(connectQueue.isEmpty()) {
            catnip().vertx().setTimer(1000L, __ -> poll());
            return;
        }
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
}
