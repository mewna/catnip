package com.mewna.catnip.shard.manager;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.shard.CatnipShard;
import com.mewna.catnip.shard.CatnipShard.ShardConnectState;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * @author amy
 * @since 8/15/18.
 */
public class DefaultShardManager implements ShardManager {
    private static final String POLL_QUEUE = "catnip:shard:manager:poll";
    
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private final int customShardCount;
    private final WebClient client = WebClient.create(Catnip.vertx());
    @Getter
    private final Deque<Integer> connectQueue = new ConcurrentLinkedDeque<>();
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
    public int getShardCount() {
        return 0;
    }
    
    @Override
    public void start() {
        if(customShardCount == -1) {
            // Load shard count from API
            client.getAbs(Catnip.getShardCountUrl()).putHeader("Authorization", "Bot " + catnip.token()).ssl(true)
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
            final CatnipShard shard = new CatnipShard(catnip, id, count);
            Catnip.vertx().deployVerticle(shard);
            connectQueue.addLast(id);
            logger.info("Deployed shard {}", id);
        }
        // Start verticles
        Catnip.eventBus().consumer(POLL_QUEUE, msg -> connect());
        Catnip.eventBus().send(POLL_QUEUE, null);
    }
    
    private void connect() {
        if(connectQueue.isEmpty()) {
            Catnip.vertx().setTimer(1000L, __ -> Catnip.eventBus().send(POLL_QUEUE, null));
            return;
        }
        final int nextId = connectQueue.removeFirst();
        logger.info("Connecting shard {} (queue len {})", nextId, connectQueue.size());
        Catnip.eventBus().<JsonObject>send(CatnipShard.getControlAddress(nextId), new JsonObject().put("mode", "START"),
                reply -> {
                    if(reply.succeeded()) {
                        final ShardConnectState state = ShardConnectState.valueOf(reply.result().body().getString("state"));
                        switch(state) {
                            case READY:
                            case RESUMED: {
                                logger.info("Connected shard {} with state {}", nextId, reply.result().body());
                                break;
                            }
                            case FAILED: {
                                logger.warn("Failed connecting shard {}, re-queueing", nextId);
                                addToConnectQueue(nextId);
                                break;
                            }
                            default: {
                                logger.error("Got unexpected / unknown shard connect state: {}", state);
                                break;
                            }
                        }
                    } else {
                        logger.warn("Failed connecting shard {} entirely, re-queueing", nextId);
                        addToConnectQueue(nextId);
                    }
                    Catnip.eventBus().send(POLL_QUEUE, null);
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
