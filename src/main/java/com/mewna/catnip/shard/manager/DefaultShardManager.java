package com.mewna.catnip.shard.manager;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.shard.CatnipShard;
import com.mewna.catnip.shard.CatnipShard.ShardConnectState;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.annotation.Nonnegative;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * @author amy
 * @since 8/15/18.
 */
@Accessors(fluent = true)
public class DefaultShardManager implements ShardManager {
    private static final String POLL_QUEUE = "catnip:shard:manager:poll";
    
    private final int customShardCount;
    @Getter
    private final Deque<Integer> connectQueue = new ConcurrentLinkedDeque<>();
    @Getter
    private WebClient client;
    @Getter
    @Setter
    private Catnip catnip;
    
    public DefaultShardManager() {
        this(-1);
    }
    
    @SuppressWarnings("WeakerAccess")
    public DefaultShardManager(final int customShardCount) {
        this.customShardCount = customShardCount;
    }
    
    @Override
    public int shardCount() {
        return 0;
    }
    
    @Override
    public void start() {
        client = WebClient.create(catnip.vertx());
        if(customShardCount == -1) {
            // Load shard count from API
            client.getAbs(Catnip.getShardCountUrl()).putHeader("Authorization", "Bot " + catnip.token()).ssl(true)
                    .send(ar -> {
                        if(ar.succeeded()) {
                            final JsonObject body = ar.result().bodyAsJsonObject();
                            final int shards = body.getInteger("shards", -1);
                            if(shards != -1) {
                                loadShards(shards);
                            } else {
                                throw new IllegalStateException("Invalid token provided (Gateway JSON response doesn't have `shards` key)!");
                            }
                        } else {
                            throw new IllegalStateException("Couldn't load shard count from API!");
                        }
                    });
        } else {
            loadShards(customShardCount);
        }
    }
    
    private void loadShards(final int count) {
        catnip.logAdapter().info("Booting {} shards", count);
        // Deploy verticles
        for(int id = 0; id < count; id++) {
            //noinspection TypeMayBeWeakened
            final CatnipShard shard = new CatnipShard(catnip, id, count);
            catnip.vertx().deployVerticle(shard);
            connectQueue.addLast(id);
            catnip.logAdapter().info("Deployed shard {}", id);
        }
        // Start verticles
        catnip.eventBus().consumer(POLL_QUEUE, msg -> connect());
        catnip.eventBus().send(POLL_QUEUE, null);
    }
    
    private void connect() {
        if(connectQueue.isEmpty()) {
            catnip.vertx().setTimer(1000L, __ -> catnip.eventBus().send(POLL_QUEUE, null));
            return;
        }
        final int nextId = connectQueue.removeFirst();
        catnip.logAdapter().info("Connecting shard {} (queue len {})", nextId, connectQueue.size());
        catnip.eventBus().<JsonObject>send(CatnipShard.getControlAddress(nextId), new JsonObject().put("mode", "START"),
                reply -> {
                    if(reply.succeeded()) {
                        final ShardConnectState state = ShardConnectState.valueOf(reply.result().body().getString("state"));
                        switch(state) {
                            case READY:
                            case RESUMED: {
                                catnip.logAdapter().info("Connected shard {} with state {}", nextId, reply.result().body());
                                break;
                            }
                            case FAILED: {
                                catnip.logAdapter().warn("Failed connecting shard {}, re-queueing", nextId);
                                addToConnectQueue(nextId);
                                break;
                            }
                            default: {
                                catnip.logAdapter().error("Got unexpected / unknown shard connect state: {}", state);
                                break;
                            }
                        }
                    } else {
                        catnip.logAdapter().warn("Failed connecting shard {} entirely, re-queueing", nextId);
                        addToConnectQueue(nextId);
                    }
                    catnip.eventBus().send(POLL_QUEUE, null);
                });
    }
    
    @Override
    public void addToConnectQueue(@Nonnegative final int shard) {
        if(!connectQueue.contains(shard)) {
            connectQueue.add(shard);
        } else {
            catnip.logAdapter().warn("Ignoring duplicate queue for shard {}", shard);
        }
    }
}
