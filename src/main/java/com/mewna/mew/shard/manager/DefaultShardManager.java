package com.mewna.mew.shard.manager;

import com.mewna.mew.Mew;
import com.mewna.mew.shard.MewShard;
import com.mewna.mew.shard.MewShard.ShardConnectState;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.LinkedList;

/**
 * @author amy
 * @since 8/15/18.
 */
public class DefaultShardManager implements ShardManager {
    private static final String POLL_QUEUE = "mew:shard:manager:poll";
    
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private final int customShardCount;
    private final WebClient client = WebClient.create(Mew.vertx());
    @Getter
    private final Deque<Integer> connectQueue = new LinkedList<>();
    @Getter
    @Setter
    private Mew mew;
    
    public DefaultShardManager() {
        this(-1);
    }
    
    @SuppressWarnings("WeakerAccess")
    public DefaultShardManager(final int customShardCount) {
        this.customShardCount = customShardCount;
    }
    
    @Override
    public int getShardCount() {
        return 0;
    }
    
    @Override
    public void start() {
        if(customShardCount == -1) {
            // Load shard count from API
            client.getAbs(Mew.getShardCountUrl()).putHeader("Authorization", "Bot " + mew.token()).ssl(true)
                    .send(ar -> {
                        if(ar.succeeded()) {
                            final int recommendedShards = ar.result().bodyAsJsonObject().getInteger("shards");
                            loadShards(recommendedShards);
                        } else {
                            throw new IllegalStateException("Couldn't load shard count from API!");
                        }
                    });
        } else {
            loadShards(customShardCount);
        }
    }
    
    private void loadShards(final int count) {
        logger.info("Booting {} shards", count);
        // Deploy verticles
        for(int id = 0; id < count; id++) {
            //noinspection TypeMayBeWeakened
            final MewShard shard = new MewShard(mew, id, count);
            Mew.vertx().deployVerticle(shard);
            connectQueue.addLast(id);
            logger.info("Deployed shard {}", id);
        }
        // Start verticles
        Mew.eventBus().consumer(POLL_QUEUE, msg -> connect());
        connect();
    }
    
    private void connect() {
        if(connectQueue.isEmpty()) {
            Mew.vertx().setTimer(1000L, __ -> Mew.eventBus().send(POLL_QUEUE, null));
            return;
        }
        final int nextId = connectQueue.removeFirst();
        logger.info("Connecting shard {}", nextId);
        Mew.eventBus().<JsonObject>send(MewShard.getControlAddress(nextId), new JsonObject().put("mode", "START"),
                reply -> {
                    if(reply.succeeded()) {
                        switch(ShardConnectState.valueOf(reply.result().body().getString("state"))) {
                            case READY:
                            case RESUMED: {
                                // TODO: Emit?
                                logger.info("Connected shard {} with state {}", nextId, reply.result().body());
                                break;
                            }
                            case FAILED: {
                                logger.warn("Failed connecting shard {}, re-queueing", nextId);
                                addToConnectQueue(nextId);
                                break;
                            }
                            default: {
                                break;
                            }
                        }
                        Mew.eventBus().send(POLL_QUEUE, null);
                    } else {
                        // TODO: Logging
                        logger.warn("Failed connecting shard {} entirely, re-queueing", nextId);
                        addToConnectQueue(nextId);
                        Mew.eventBus().send(POLL_QUEUE, null);
                    }
                });
    }
    
    @Override
    public void addToConnectQueue(final int shard) {
        if(!connectQueue.contains(shard)) {
            connectQueue.add(shard);
        } else {
            logger.warn("Ignoring duplicate queue for shard {}", shard);
        }
    }
}
