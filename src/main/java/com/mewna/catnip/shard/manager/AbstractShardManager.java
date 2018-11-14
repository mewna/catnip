package com.mewna.catnip.shard.manager;

import com.google.common.collect.ImmutableList;
import com.mewna.catnip.Catnip;
import com.mewna.catnip.shard.CatnipShard;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author amy
 * @since 11/13/18.
 */
@Accessors(fluent = true)
@SuppressWarnings("unused")
@RequiredArgsConstructor
public abstract class AbstractShardManager implements ShardManager {
    @Getter
    @Setter
    private Catnip catnip;
    
    @SuppressWarnings("WeakerAccess")
    protected void deployShard(@Nonnegative final int id, @Nonnegative final int count) {
        // because each shard has its own presence, so no global presence on catnip class
        @SuppressWarnings("TypeMayBeWeakened")
        final CatnipShard shard = new CatnipShard(catnip, id, count, catnip.initialPresence());
        catnip.vertx().deployVerticle(shard);
        addToConnectQueue(id);
    }
    
    @Nonnull
    @Override
    public Future<List<String>> trace(@Nonnegative final int shard) {
        final Future<List<String>> future = Future.future();
        catnip.eventBus().<JsonArray>send(CatnipShard.controlAddress(shard), new JsonObject().put("mode", "TRACE"),
                reply -> {
                    if(reply.succeeded()) {
                        // ow
                        future.complete(ImmutableList.copyOf(reply.result().body().stream()
                                .map(e -> (String) e).collect(Collectors.toList())));
                    } else {
                        future.fail(reply.cause());
                    }
                });
        return future;
    }
    
    @Override
    public void shutdown() {
        for(int i = 0; i < shardCount(); i++) {
            catnip.eventBus().send(CatnipShard.controlAddress(i), new JsonObject().put("mode", "SHUTDOWN"));
        }
    }
}
