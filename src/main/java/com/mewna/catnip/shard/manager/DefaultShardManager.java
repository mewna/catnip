package com.mewna.catnip.shard.manager;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.shard.CatnipShard;
import com.mewna.catnip.shard.CatnipShard.ShardConnectState;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.experimental.Accessors;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import javax.annotation.Nonnegative;
import java.io.IOException;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * @author amy
 * @since 8/15/18.
 */
@Accessors(fluent = true)
public class DefaultShardManager extends AbstractShardManager {
    private static final String POLL_QUEUE = "catnip:shard:manager:poll";
    
    @Getter
    private final int shardCount;
    @Getter
    private final Deque<Integer> connectQueue = new ConcurrentLinkedDeque<>();
    @Getter
    private OkHttpClient client;
    
    public DefaultShardManager() {
        this(-1);
    }
    
    @SuppressWarnings("WeakerAccess")
    public DefaultShardManager(final int customShardCount) {
        shardCount = customShardCount;
    }
    
    @Override
    public void start() {
        client = new OkHttpClient();
        if(shardCount == -1) {
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
                        loadShards(shards);
                    } else {
                        throw new IllegalStateException("Invalid token provided (Gateway JSON response doesn't have `shards` key)!");
                    }
                } else {
                    throw new IllegalStateException("Couldn't load shard count from API!", res.cause());
                }
            });
        } else {
            loadShards(shardCount);
        }
    }
    
    private void loadShards(final int count) {
        catnip().logAdapter().info("Booting {} shards", count);
        
        // Deploy verticles
        for(int id = 0; id < count; id++) {
            deployShard(id, count);
            catnip().logAdapter().info("Deployed shard {}", id);
        }
        // Start verticles
        catnip().eventBus().consumer(POLL_QUEUE, msg -> connect());
        catnip().eventBus().publish(POLL_QUEUE, null);
    }
    
    private void connect() {
        if(connectQueue.isEmpty()) {
            catnip().vertx().setTimer(1000L, __ -> catnip().eventBus().publish(POLL_QUEUE, null));
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
                    catnip().eventBus().publish(POLL_QUEUE, null);
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
