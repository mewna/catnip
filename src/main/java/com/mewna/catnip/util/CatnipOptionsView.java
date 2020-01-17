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

package com.mewna.catnip.util;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.cache.CacheFlag;
import com.mewna.catnip.cache.EntityCacheWorker;
import com.mewna.catnip.cache.SplitMemoryEntityCache;
import com.mewna.catnip.entity.guild.Guild;
import com.mewna.catnip.entity.lifecycle.HighWebsocketLatency;
import com.mewna.catnip.entity.lifecycle.MemberChunkRerequest;
import com.mewna.catnip.entity.serialization.EntitySerializer;
import com.mewna.catnip.entity.user.Presence;
import com.mewna.catnip.extension.Extension;
import com.mewna.catnip.rest.requester.Requester;
import com.mewna.catnip.rest.requester.SerialRequester;
import com.mewna.catnip.shard.CompressionMode;
import com.mewna.catnip.shard.DiscordEvent.Raw;
import com.mewna.catnip.shard.GatewayIntent;
import com.mewna.catnip.shard.buffer.CachingBuffer;
import com.mewna.catnip.shard.buffer.EventBuffer;
import com.mewna.catnip.shard.buffer.NoopBuffer;
import com.mewna.catnip.shard.event.DefaultDispatchManager;
import com.mewna.catnip.shard.event.DispatchManager;
import com.mewna.catnip.shard.manager.DefaultShardManager;
import com.mewna.catnip.shard.manager.ShardManager;
import com.mewna.catnip.shard.ratelimit.MemoryRatelimiter;
import com.mewna.catnip.shard.ratelimit.Ratelimiter;
import com.mewna.catnip.shard.session.DefaultSessionManager;
import com.mewna.catnip.shard.session.SessionManager;
import com.mewna.catnip.util.logging.DefaultLogAdapter;
import com.mewna.catnip.util.logging.LogAdapter;
import com.mewna.catnip.util.rx.RxHelpers;
import com.mewna.catnip.util.scheduler.RxTaskScheduler;
import com.mewna.catnip.util.scheduler.TaskScheduler;
import io.reactivex.Scheduler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.http.HttpClient;
import java.util.Set;

/**
 * An immutable view of {@link com.mewna.catnip.CatnipOptions}.
 *
 * @author amy
 * @since 10/18/19.
 */
@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public interface CatnipOptionsView {
    /**
     * The token for catnip to use.
     * <p>
     * May not be overridden by extensions.
     */
    @Nonnull
    String token();
    
    /**
     * The shard manager for catnip to use. Defaults to {@link DefaultShardManager}.
     */
    @Nonnull
    ShardManager shardManager();
    
    /**
     * The session manager for catnip to use. Defaults to {@link DefaultSessionManager}
     */
    @Nonnull
    SessionManager sessionManager();
    
    /**
     * The gateway ratelimiter for catnip to use. Defaults to {@link MemoryRatelimiter}
     */
    @Nonnull
    Ratelimiter gatewayRatelimiter();
    
    /**
     * The log adapter for catnip to use. Defaults to {@link DefaultLogAdapter},
     * which uses SLF4J.
     */
    @Nonnull
    LogAdapter logAdapter();
    
    /**
     * The event buffer for catnip to use. Defaults to {@link CachingBuffer}.
     * If you want to use an alternative event buffering strategy (ex. no
     * buffering, only buffer certain events, ...) you can write your own
     * implementation. For no buffering, {@link NoopBuffer} is provided.
     * <p>Do NOT change this if you don't know what you're doing!</p>
     */
    @Nonnull
    EventBuffer eventBuffer();
    
    /**
     * The cache worker for catnip to use. Defaults to {@link SplitMemoryEntityCache}.
     * Change this if you want to use your own {@link EntityCacheWorker}.
     */
    @Nonnull
    EntityCacheWorker cacheWorker();
    
    /**
     * The set of cache flags for catnip to obey. Used to prevent caching certain
     * things.
     */
    @Nonnull
    Set<CacheFlag> cacheFlags();
    
    /**
     * Manages event dispatching and consumers. Defaults to {@link DefaultDispatchManager}.
     */
    @Nonnull
    DispatchManager dispatchManager();
    
    /**
     * Whether or not catnip should chunk members. Do not disable this if you
     * don't know what it does.
     */
    boolean chunkMembers();
    
    /**
     * Whether or not catnip should emit full event objects. Do not disable
     * this if you don't know what it does. Mainly only useful for ex. an
     * {@link Extension} that does things with the raw gateway payloads, like
     * sending them to a message queue.
     */
    boolean emitEventObjects();
    
    /**
     * Whether or not catnip should enforce permissions for REST actions. Note
     * that this will NOT enforce permissions if you directly call methods via
     * {@link Catnip#rest()}, but will enforce them if you call them from
     * entity objects (ex. doing {@link Guild#delete()}).
     */
    boolean enforcePermissions();
    
    /**
     * The presence that catnip should set for shards as they log in. This is
     * for setting whether your bot appears online/DND/away/offline, as well as
     * the "playing XXX" status.
     */
    @Nullable
    Presence initialPresence();
    
    /**
     * The events that catnip should not emit. You can use {@link Raw} to get
     * the event names.
     */
    @Nonnull
    Set<String> disabledEvents();
    
    /**
     * The requester catnip uses for REST requests. Defaults to {@link SerialRequester}.
     */
    @Nonnull
    Requester requester();
    
    /**
     * Whether or not extensions overriding options should be logged. Defaults
     * to {@code true}.
     * <p>
     * May not be overridden by extensions.
     */
    boolean logExtensionOverrides();
    
    /**
     * Whether or not to validate the provided token when setting up catnip. It
     * is HIGHLY recommended that you leave this with the default setting.
     * <p>
     * May not be overridden by extensions.
     */
    boolean validateToken();
    
    /**
     * Whether or not catnip should capture REST stacktraces before running
     * REST requests.
     * <p>
     * catnip runs REST requests asynchronously. Because of this, we lose the
     * caller's stacktrace, and exceptions thrown from REST calls are lost to
     * the ether basically. If this option is enabled, catnip will capture a
     * stacktrace before REST requests, and make it available to any exceptions
     * thrown by the REST handler.
     * <p>
     * NOTE: Capturing stacktraces is <strong>s l o w</strong>. If you have
     * performance problems around REST requests, you can disable this, at the
     * cost of losing debuggability. Note that it may be useful to add
     * <code>-XX:-OmitStackTraceInFastThrow</code> to your JVM flags to ensure
     * that this doesn't get optimized out.
     * <p>
     * TODO: Verify that -XX:-OmitStackTraceInFastThrow isn't needed now.
     */
    boolean captureRestStacktraces();
    
    /**
     * Whether or not to log "Received presence for uncached user XXX" when
     * catnip is not chunking members. Basically, this avoids a ton of logspam.
     */
    boolean logUncachedPresenceWhenNotChunking();
    
    /**
     * Whether or not Discord should subscribe to guild workers and provide stuff
     * such as presence updates, typing events, member updates and other stuff,
     * see discord-api-docs#1016 for more information.
     */
    boolean enableGuildSubscriptions();
    
    /**
     * How long catnip should wait to ensure that all member chunks have been
     * received, in milliseconds. If all member chunks still haven't been
     * received after this period, member chunking will be re-requested, to try
     * to make sure we're not missing any.
     */
    long memberChunkTimeout();
    
    /**
     * The RxJava scheduler that catnip should use for scheduling things like
     * stream subscriptions. Defaults to {@link RxHelpers#FORK_JOIN_SCHEDULER}.
     */
    Scheduler rxScheduler();
    
    /**
     * If this option is enabled, lifecycle-related events -- things like shard
     * connects / disconnects, 429 HTTP responses, ... -- will be directly
     * logged via the configured {@link #logAdapter}, in addition to being
     * emitted over the event bus.
     */
    boolean logLifecycleEvents();
    
    /**
     * If this option is enabled, emit a {@link MemberChunkRerequest} over the
     * event bus when a shard needs to re-request member chunks for a guild,
     * instead of automatically re-requesting them.
     */
    boolean manualChunkRerequesting();
    
    /**
     * Total number of members where the gateway will stop sending offline
     * members in the guild member list. If a guild's member count is over this
     * limit, member chunking will happen. See {@link #chunkMembers}
     * {@link #manualChunkRerequesting} {@link #memberChunkTimeout} for more.
     *
     * <strong>This must be between 50 and 250.</strong>
     * <p>
     * For Discord's documentation, go here:
     * https://discordapp.com/developers/docs/topics/gateway#identify-identify-structure
     */
    int largeThreshold();
    
    /**
     * The task scheduler that catnip will use for scheduling its own internal
     * tasks. This scheduler is exposed to the outside world through
     * {@link Catnip#taskScheduler()}, and can safely be used for any task
     * scheduling needs you may have. Defaults to {@link RxTaskScheduler}.
     */
    TaskScheduler taskScheduler();
    
    /**
     * The HTTP client that catnip uses internally for websockets and REST
     * requests. Defaults to an instance that uses
     * {@link RxHelpers#FORK_JOIN_POOL} as its executor.
     */
    HttpClient httpClient();
    
    /**
     * How catnip compresses incoming events from Discord. Default is
     * {@link CompressionMode#ZLIB}.
     */
    CompressionMode compressionMode();
    
    /**
     * Whether or not catnip should assume the inability to have a properly
     * synchronized clock when computing REST ratelimits. When this option is
     * set to {@code true}, catnip will assume that the local clock cannot be
     * properly synced, and will use a less-efficient method provided by
     * Discord for computing REST ratelimits. See
     * https://github.com/discordapp/discord-api-docs/pull/1069 for more info.
     */
    boolean restRatelimitsWithoutClockSync();
    
    /**
     * If heartbeat latency takes longer than this much time, catnip will emit
     * a {@link HighWebsocketLatency} event containing information about which
     * shard is experiencing high latency and how high the latency is.<br />
     *
     * <strong>This value is specified in nanoseconds.</strong>
     */
    long highLatencyThreshold();
    
    /**
     * The entity serializer that catnip uses for de/serializing entities for
     * external usage. The value of this option will not affect how catnip
     * behaves internally, but rather will affect user-controlled serialization
     * for interfacing with the outside world.
     */
    EntitySerializer<?> entitySerializer();
    
    /**
     * The host used for Discord API requests. Defaults to
     * {@code https://discordapp.com}. Changing this is only really useful for
     * the case of ex. running tests against a local mock API. Providing the
     * protocol is <strong>REQUIRED</strong>.
     */
    String apiHost();
    
    /**
     * @return The version of the Discord REST API to use. Defaults to
     * {@code 6}. Changing this is really only useful for the case of ex.
     * running tests against a local mock API. Note that catnip is not tested
     * against API v7, nor is v7 actively supported at this time.
     */
    int apiVersion();
    
    /**
     * @return The set of gateway intents that this bot wishes to use. This is
     * OPTIONAL as of gateway v6, but will be REQUIRED in gateway v7.<br />
     * Gateway intents are used to control the events that your bor receives.
     * Intents are relatively-broad categories of events; see the documentation
     * on each member of {@link GatewayIntent} for more information about what
     * events are covered by each intent.<br/>
     * <strong>NOTE THAT THIS IS NOT THE SAME AS
     * {@link #disabledEvents()}!</strong>
     */
    Set<GatewayIntent> intents();
}
