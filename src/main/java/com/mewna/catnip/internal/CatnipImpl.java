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

package com.mewna.catnip.internal;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.CatnipOptions;
import com.mewna.catnip.cache.CacheFlag;
import com.mewna.catnip.cache.EntityCacheWorker;
import com.mewna.catnip.entity.impl.user.PresenceImpl;
import com.mewna.catnip.entity.impl.user.PresenceImpl.ActivityImpl;
import com.mewna.catnip.entity.misc.GatewayInfo;
import com.mewna.catnip.entity.user.Presence;
import com.mewna.catnip.entity.user.Presence.Activity;
import com.mewna.catnip.entity.user.Presence.ActivityType;
import com.mewna.catnip.entity.user.Presence.OnlineStatus;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.entity.util.Permission;
import com.mewna.catnip.extension.Extension;
import com.mewna.catnip.extension.manager.DefaultExtensionManager;
import com.mewna.catnip.extension.manager.ExtensionManager;
import com.mewna.catnip.rest.Rest;
import com.mewna.catnip.rest.requester.Requester;
import com.mewna.catnip.shard.CatnipShardImpl;
import com.mewna.catnip.shard.GatewayOp;
import com.mewna.catnip.shard.buffer.EventBuffer;
import com.mewna.catnip.shard.event.DispatchManager;
import com.mewna.catnip.shard.manager.ShardManager;
import com.mewna.catnip.shard.ratelimit.Ratelimiter;
import com.mewna.catnip.shard.session.SessionManager;
import com.mewna.catnip.util.PermissionUtil;
import com.mewna.catnip.util.logging.LogAdapter;
import com.mewna.catnip.util.scheduler.TaskScheduler;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.net.http.HttpClient;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * @author amy
 * @since 8/31/18.
 */
@Getter
@SuppressWarnings("OverlyCoupledClass")
@Accessors(fluent = true, chain = true)
public class CatnipImpl implements Catnip {
    private final String token;
    private final boolean logExtensionOverrides;
    private final boolean validateToken;
    
    private final Rest rest = new Rest(this);
    private final ExtensionManager extensionManager = new DefaultExtensionManager(this);
    private final Set<String> unavailableGuilds = ConcurrentHashMap.newKeySet();
    private final AtomicReference<GatewayInfo> gatewayInfo = new AtomicReference<>(null);
    
    private final Thread keepaliveThread;
    private final CountDownLatch latch = new CountDownLatch(1);
    
    private boolean startedKeepalive;
    private long clientIdAsLong;
    
    private DispatchManager dispatchManager;
    private Requester requester;
    private ShardManager shardManager;
    private SessionManager sessionManager;
    private Ratelimiter gatewayRatelimiter;
    private LogAdapter logAdapter;
    private EventBuffer eventBuffer;
    private EntityCacheWorker cache;
    private Set<CacheFlag> cacheFlags;
    private boolean chunkMembers;
    private boolean emitEventObjects;
    private boolean enforcePermissions;
    private boolean captureRestStacktraces;
    private boolean logUncachedPresenceWhenNotChunking;
    private boolean warnOnEntityVersionMismatch;
    private long memberChunkTimeout;
    private Presence initialPresence;
    private Set<String> disabledEvents;
    private Scheduler rxScheduler;
    private boolean logLifecycleEvents;
    private boolean manualChunkRerequesting;
    private int largeThreshold;
    private TaskScheduler taskScheduler;
    private HttpClient httpClient;
    
    private CatnipOptions options;
    
    public CatnipImpl(@Nonnull final CatnipOptions options) {
        applyOptions(options);
        token = options.token();
        logExtensionOverrides = options.logExtensionOverrides();
        validateToken = options.validateToken();
        keepaliveThread = new Thread(() -> {
            try {
                latch.await();
            } catch(final InterruptedException ignored) {
            }
        });
        // Just to be safe
        keepaliveThread.setDaemon(false);
        keepaliveThread.setName("catnip keepalive thread");
    }
    
    private void applyOptions(@Nonnull final CatnipOptions options) {
        // TODO: Should probably make this behave like #diff
        //  so that we don't need to update this every single time that the
        //  options change.
        this.options = options;
        dispatchManager = options.dispatchManager();
        requester = options.requester();
        shardManager = options.shardManager();
        sessionManager = options.sessionManager();
        gatewayRatelimiter = options.gatewayRatelimiter();
        logAdapter = options.logAdapter();
        eventBuffer = options.eventBuffer();
        cache = options.cacheWorker();
        cacheFlags = options.cacheFlags();
        chunkMembers = options.chunkMembers();
        emitEventObjects = options.emitEventObjects();
        enforcePermissions = options.enforcePermissions();
        captureRestStacktraces = options.captureRestStacktraces();
        initialPresence = options.presence();
        memberChunkTimeout = options.memberChunkTimeout();
        disabledEvents = Set.copyOf(options.disabledEvents());
        logUncachedPresenceWhenNotChunking = options.logUncachedPresenceWhenNotChunking();
        warnOnEntityVersionMismatch = options.warnOnEntityVersionMismatch();
        rxScheduler = options.rxScheduler();
        logLifecycleEvents = options.logLifecycleEvents();
        manualChunkRerequesting = options.manualChunkRerequesting();
        largeThreshold = options.largeThreshold();
        taskScheduler = options.taskScheduler();
        httpClient = options.httpClient();
        
        // Sanity checks
        if(largeThreshold > 250 || largeThreshold < 50) {
            throw new IllegalArgumentException("Large threshold of " + largeThreshold + " not between 50 and 250!");
        }
        
        injectSelf();
    }
    
    @Nonnull
    @Override
    public Catnip injectOptions(@Nonnull final Extension extension, @Nonnull final Function<CatnipOptions, CatnipOptions> optionsPatcher) {
        if(!extensionManager.matchingExtensions(extension.getClass()).isEmpty()) {
            final CatnipOptions patchedOptions = optionsPatcher.apply((CatnipOptions) options.clone());
            final Map<String, Pair<Object, Object>> diff = diff(patchedOptions);
            if(!diff.isEmpty()) {
                applyOptions(patchedOptions);
                if(logExtensionOverrides) {
                    diff.forEach((name, patch) -> logAdapter.info("Extension {} updated {} from \"{}\" to \"{}\".",
                            extension.name(), name, patch.getLeft(), patch.getRight()));
                }
            }
        } else {
            throw new IllegalArgumentException("Extension with class " + extension.getClass().getName()
                    + " isn't loaded, but tried to inject options!");
        }
        
        return this;
    }
    
    private Map<String, Pair<Object, Object>> diff(@Nonnull final CatnipOptions patch) {
        final Map<String, Pair<Object, Object>> diff = new LinkedHashMap<>();
        // Yeah this is ugly reflection bs, I know. But this allows it to
        // automatically diff it without having to know about what every
        // field is.
        for(final Field field : patch.getClass().getDeclaredFields()) {
            // Don't compare tokens because there's no point
            if(!field.getName().equals("token")) {
                try {
                    field.setAccessible(true);
                    final Object input = field.get(patch);
                    final Object original = field.get(options);
                    if(!Objects.equals(original, input)) {
                        diff.put(field.getName(), ImmutablePair.of(original, input));
                    }
                } catch(final IllegalAccessException e) {
                    logAdapter.error("Reflection did a \uD83D\uDCA9", e);
                }
            }
        }
        return diff;
    }
    
    @Nonnull
    @Override
    public Catnip loadExtension(@Nonnull final Extension extension) {
        extensionManager.loadExtension(extension);
        return this;
    }
    
    @Nullable
    @Override
    public User selfUser() {
        return cache().selfUser();
    }
    
    @Override
    public String clientId() {
        return Long.toUnsignedString(clientIdAsLong());
    }
    
    @Override
    public long clientIdAsLong() {
        return clientIdAsLong;
    }
    
    @Override
    public void shutdown() {
        logAdapter.info("Shutting down!");
        dispatchManager.close();
        shardManager.shutdown();
        // Will let the keepalive thread halt
        latch.countDown();
    }
    
    @Nonnull
    @Override
    public Set<String> unavailableGuilds() {
        return Set.copyOf(unavailableGuilds);
    }
    
    public void markAvailable(final String id) {
        unavailableGuilds.remove(id);
    }
    
    public void markUnavailable(final String id) {
        unavailableGuilds.add(id);
    }
    
    @Override
    public boolean isUnavailable(@Nonnull final String guildId) {
        return unavailableGuilds.contains(guildId);
    }
    
    @Override
    public void openVoiceConnection(@Nonnull final String guildId, @Nonnull final String channelId, final boolean selfMute,
                                    final boolean selfDeaf) {
        PermissionUtil.checkPermissions(this, guildId, channelId, Permission.CONNECT);
        shardManager().shard(shardIdFor(guildId)).queueVoiceStateUpdate(
                new JsonObject()
                        .put("guild_id", guildId)
                        .put("channel_id", channelId)
                        .put("self_mute", selfMute)
                        .put("self_deaf", selfDeaf));
    }
    
    @Override
    public void closeVoiceConnection(@Nonnull final String guildId) {
        shardManager().shard(shardIdFor(guildId)).queueVoiceStateUpdate(
                new JsonObject()
                        .put("guild_id", guildId)
                        .putNull("channel_id")
                        .put("self_mute", false)
                        .put("self_deaf", false));
    }
    
    @Override
    public void closeVoiceConnection(final long guildId) {
        closeVoiceConnection(String.valueOf(guildId));
    }
    
    @Override
    public void chunkMembers(@Nonnull final String guildId, @Nonnull final String query, @Nonnegative final int limit) {
        shardManager().shard(shardIdFor(guildId)).queueSendToSocket(
                CatnipShardImpl.basePayload(GatewayOp.REQUEST_GUILD_MEMBERS,
                        new JsonObject()
                                .put("guild_id", guildId)
                                .put("query", query)
                                .put("limit", limit)));
    }
    
    @Override
    public Presence presence(@Nonnegative final int shardId) {
        return shardManager().shard(shardId).getPresence();
    }
    
    @Override
    public void presence(@Nonnull final Presence presence) {
        int shardCount = shardManager().shardCount();
        if(shardCount == 0) {
            shardCount = 1;
        }
        for(int i = 0; i < shardCount; i++) {
            presence(presence, i);
        }
    }
    
    @Override
    public void presence(@Nonnull final Presence presence, @Nonnegative final int shardId) {
        shardManager().shard(shardId).updatePresence((PresenceImpl) presence);
    }
    
    @Override
    public void presence(@Nullable final OnlineStatus status, @Nullable final String game, @Nullable final ActivityType type,
                         @Nullable final String url) {
        final OnlineStatus stat;
        if(status != null) {
            stat = status;
        } else {
            final User self = selfUser();
            if(self != null) {
                final Presence presence = cache().presence(self.id());
                stat = presence == null ? OnlineStatus.ONLINE : presence.status();
            } else {
                stat = OnlineStatus.ONLINE;
            }
        }
        final Activity activity = game != null
                ? ActivityImpl.builder()
                .name(game)
                .type(type == null ? ActivityType.PLAYING : type)
                .url(type == ActivityType.STREAMING ? url : null)
                .build()
                : null;
        presence(PresenceImpl.builder()
                .catnip(this)
                .status(stat)
                .activity(activity)
                .build());
    }
    
    @Nonnull
    public Single<Catnip> setup() {
        if(!startedKeepalive) {
            startedKeepalive = true;
            keepaliveThread.start();
        }
        if(validateToken) {
            return fetchGatewayInfo()
                    .map(gateway -> {
                        logAdapter.info("Token validated!");
                        
                        parseClientId();
                        
                        //this is actually needed because generics are dumb
                        return (Catnip) this;
                    }).doOnError(e -> {
                        logAdapter.warn("Couldn't validate token!", e);
                        throw new RuntimeException(e);
                    });
        } else {
            try {
                parseClientId();
            } catch(final IllegalArgumentException e) {
                final Exception wrapped = new RuntimeException("The provided token was invalid!", e);
                return Single.error(wrapped);
            }
            
            return Single.just(this);
        }
    }
    
    private void injectSelf() {
        // Inject catnip instance into dependent fields
        dispatchManager.catnip(this);
        shardManager.catnip(this);
        eventBuffer.catnip(this);
        cache.catnip(this);
        requester.catnip(this);
        taskScheduler.catnip(this);
    }
    
    @Nonnull
    @Override
    public EntityCacheWorker cacheWorker() {
        return cache;
    }
    
    @Nonnull
    public Catnip connect() {
        shardManager.start();
        return this;
    }
    
    private int shardIdFor(@Nonnull final String guildId) {
        final long idLong = Long.parseUnsignedLong(guildId);
        return (int) ((idLong >>> 22) % shardManager.shardCount());
    }
    
    private void parseClientId() {
        // bot tokens are comprised of 3 parts, each encoded in base 64 and joined by periods.
        // the first part is the client id.
        final String clientIdBase64 = token.split("\\.")[0];
        final String clientId = new String(Base64.getDecoder().decode(clientIdBase64));
        clientIdAsLong = Long.parseUnsignedLong(clientId);
    }
    
    @Nullable
    @Override
    public GatewayInfo gatewayInfo() {
        return gatewayInfo.get();
    }
    
    @Nonnull
    @Override
    public Single<GatewayInfo> fetchGatewayInfo() {
        return rest.user().getGatewayBot()
                .map(g -> {
                    if(g.valid()) {
                        gatewayInfo.set(g);
                        return g;
                    } else {
                        throw new RuntimeException("Gateway info not valid! Is your token valid?");
                    }
                })
                .doOnError(e -> {
                    throw new RuntimeException(e);
                });
    }
}
