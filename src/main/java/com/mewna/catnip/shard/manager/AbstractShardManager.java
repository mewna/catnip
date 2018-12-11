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
import com.mewna.catnip.Catnip;
import com.mewna.catnip.shard.CatnipShard;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.escoffier.vertx.completablefuture.VertxCompletableFuture;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
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
    
    @Getter
    private final List<ShardCondition> conditions = new CopyOnWriteArrayList<>();
    
    @SuppressWarnings("WeakerAccess")
    protected void deployShard(@Nonnegative final int id, @Nonnegative final int count) {
        // because each shard has its own presence, so no global presence on catnip class
        @SuppressWarnings("TypeMayBeWeakened")
        final CatnipShard shard = new CatnipShard(catnip, id, count, catnip.initialPresence());
        catnip.vertx().deployVerticle(shard);
        addToConnectQueue(id);
    }
    
    @Override
    public ShardManager addCondition(@Nonnull final ShardCondition condition) {
        conditions.add(condition);
        return this;
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
    
    @Nonnull
    @Override
    @CheckReturnValue
    public CompletableFuture<Boolean> isConnected(@Nonnegative final int id) {
        final Future<Boolean> future = Future.future();
        catnip.eventBus().<Boolean>send(CatnipShard.controlAddress(id), new JsonObject().put("mode", "CONNECTED"),
                reply -> {
                    if(reply.succeeded()) {
                        future.complete(reply.result().body());
                    } else {
                        future.fail(reply.cause());
                    }
                });
        return VertxCompletableFuture.from(catnip.vertx(), future);
    }
    
    @Override
    public void shutdown() {
        for(int i = 0; i < shardCount(); i++) {
            catnip.eventBus().send(CatnipShard.controlAddress(i), new JsonObject().put("mode", "SHUTDOWN"));
        }
    }
}
