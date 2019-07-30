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

import com.mewna.catnip.Catnip;
import com.mewna.catnip.shard.CatnipShard;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

/**
 * Manages the lifecycle of shards - starting, stopping, resuming, etc.
 *
 * @author amy
 * @since 8/15/18.
 */
public interface ShardManager {
    /**
     * @return The number of shards this shard manager owns.
     */
    @Nonnegative
    int shardCount();
    
    /**
     * @return The collection of shard ids owned by this shard manager
     * instance.
     */
    Collection<Integer> shardIds();
    
    /**
     * Starts booting shards.
     */
    void start();
    
    /**
     * @return The shard conditions for this shard manager instance.
     *
     * @see ShardCondition
     */
    List<ShardCondition> conditions();
    
    /**
     * Add a condition to this shard manager.
     *
     * @param condition The new condition.
     *
     * @return Itself.
     */
    ShardManager addCondition(@Nonnull ShardCondition condition);
    
    /**
     * Adds the given shard id to the connect queue.
     *
     * @param shard The shard id to add.
     */
    void addToConnectQueue(@Nonnegative int shard);
    
    /**
     * Fetches a shard.
     *
     * @param shard The id of the shard to fetch.
     *
     * @return The shard, if it exists, otherwise it throws a {@link NullPointerException}.
     */
    @Nonnull
    CatnipShard shard(@Nonnegative int shard);
    
    /**
     * @return The catnip instance this shard manager is for.
     */
    @Nonnull
    Catnip catnip();
    
    @SuppressWarnings("UnusedReturnValue")
    @Nonnull
    ShardManager catnip(@Nonnull Catnip catnip);
    
    /**
     * Shuts down all shards.
     */
    void shutdown();
}
