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

import com.grack.nanojson.JsonObject;
import com.mewna.catnip.Catnip;
import com.mewna.catnip.CatnipOptions;
import com.mewna.catnip.entity.impl.EntityBuilder;
import com.mewna.catnip.entity.impl.user.PresenceImpl;
import com.mewna.catnip.entity.impl.user.PresenceImpl.ActivityImpl;
import com.mewna.catnip.entity.misc.GatewayInfo;
import com.mewna.catnip.entity.user.Presence;
import com.mewna.catnip.entity.user.Presence.Activity;
import com.mewna.catnip.entity.user.Presence.ActivityType;
import com.mewna.catnip.entity.user.Presence.OnlineStatus;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.entity.misc.ApplicationFlag;
import com.mewna.catnip.entity.util.Permission;
import com.mewna.catnip.extension.Extension;
import com.mewna.catnip.extension.manager.DefaultExtensionManager;
import com.mewna.catnip.extension.manager.ExtensionManager;
import com.mewna.catnip.rest.Rest;
import com.mewna.catnip.rest.requester.Requester;
import com.mewna.catnip.shard.CatnipShardImpl;
import com.mewna.catnip.shard.GatewayIntent;
import com.mewna.catnip.shard.GatewayOp;
import com.mewna.catnip.util.PermissionUtil;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.security.Security;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * @author amy
 * @since 8/31/18.
 */
@Getter
@Accessors(fluent = true, chain = true)
public final class CatnipImpl implements Catnip {
    public static final BouncyCastleProvider BOUNCY_CASTLE_PROVIDER = new BouncyCastleProvider();
    private static final List<String> UNPATCHABLE_OPTIONS = List.of(
            "token",
            "logExtensionOverrides",
            "validateToken",
            "publicKey"
    );
    
    static {
        Security.addProvider(BOUNCY_CASTLE_PROVIDER);
    }
    
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
    private CatnipOptions options;
    private final EntityBuilder entityBuilder = new EntityBuilder(this);
    
    public CatnipImpl(@Nonnull final CatnipOptions options) {
        sanityCheckOptions(options);
        this.options = options;
        
        token = options.token();
        logExtensionOverrides = options.logExtensionOverrides();
        validateToken = options.validateToken();
        
        keepaliveThread = new Thread(() -> {
            try {
                // Whenever it's done, the keepalive thread dies along with the rest of catnip
                latch.await();
            } catch(final InterruptedException ignored) {
            }
        });
        // Just to be safe
        keepaliveThread.setDaemon(false);
        keepaliveThread.setName("catnip keepalive thread");
        injectSelf();
    }
    
    private void sanityCheckOptions(@Nonnull final CatnipOptions options) {
        if(options.largeThreshold() > 250 || options.largeThreshold() < 50) {
            throw new IllegalArgumentException("Large threshold of " + options.largeThreshold() + " not between 50 and 250!");
        }
        if(options.highLatencyThreshold() < 0) {
            throw new IllegalArgumentException("High latency threshold of " + options.highLatencyThreshold() + " not greater than zero!");
        }
        if(options.apiVersion() < 8) {
            throw new IllegalArgumentException("Minimum required API version is v8!");
        }
        if(options.intents().isEmpty()) {
            if(options.iReallyWantToStartTheBotWithNoIntents()) {
                logAdapter().warn("Starting with no intents!");
            } else {
                throw new IllegalArgumentException("Intents are required, but you didn't provide any! Are you *sure* you want a bot that can't do anything?");
            }
        }
    }
    
    @Nonnull
    @Override
    public Catnip injectOptions(@Nonnull final Extension extension, @Nonnull final UnaryOperator<CatnipOptions> optionsPatcher) {
        if(!extensionManager.matchingExtensions(extension.getClass()).isEmpty()) {
            final CatnipOptions patchedOptions = optionsPatcher.apply((CatnipOptions) options.clone());
            final Map<String, Pair<Object, Object>> diff = diff(patchedOptions);
            if(!diff.isEmpty()) {
                sanityCheckOptions(patchedOptions);
                options = patchedOptions;
                injectSelf();
                if(logExtensionOverrides) {
                    diff.forEach((name, patch) -> logAdapter().info("Extension {} updated {} from \"{}\" to \"{}\".",
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
            // Don't compare certain because there's no point; they only get
            // checked / set once at startup and never again.
            if(!UNPATCHABLE_OPTIONS.contains(field.getName())) {
                try {
                    field.setAccessible(true);
                    final Object input = field.get(patch);
                    final Object original = field.get(options);
                    if(!Objects.equals(original, input)) {
                        diff.put(field.getName(), ImmutablePair.of(original, input));
                    }
                } catch(final IllegalAccessException e) {
                    logAdapter().error("Reflection did a \uD83D\uDCA9", e);
                }
            }
        }
        return diff;
    }
    
    // We don't expose this to the outside world because the ability to make
    // raw requests is already exposed via #rest(). If someone REALLY needs
    // this functionality, they can just get it by casting to CatnipImpl.
    // The only reason I can think of to have this as a part of the public
    // API is because someone wants to use undocumented routes -- in which case
    // they shouldn't be doing that -- or if we haven't added a route yet, in
    // which case they should be opening an issue so that we can get it added
    // for everyone.
    // TLDR, if you *really* need this then you know what you're doing, so you
    // can live with casting instead of exposing it as part of the public API.
    public Requester requester() {
        return options.requester();
    }
    
    @Nonnull
    @Override
    public Catnip loadExtension(@Nonnull final Extension extension) {
        extensionManager.loadExtension(extension);
        return this;
    }
    
    @Nonnull
    @Override
    @CheckReturnValue
    public Maybe<User> selfUser() {
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
        logAdapter().info("Shutting down!");
        shardManager().shutdown();
        extensionManager.shutdown();
        dispatchManager().close();
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
                JsonObject.builder()
                        .value("guild_id", guildId)
                        .value("channel_id", channelId)
                        .value("self_mute", selfMute)
                        .value("self_deaf", selfDeaf)
                        .done());
    }
    
    @Override
    public void closeVoiceConnection(@Nonnull final String guildId) {
        shardManager().shard(shardIdFor(guildId)).queueVoiceStateUpdate(
                JsonObject.builder()
                        .value("guild_id", guildId)
                        .nul("channel_id")
                        .value("self_mute", false)
                        .value("self_deaf", false)
                        .done());
    }
    
    @Override
    public void closeVoiceConnection(final long guildId) {
        closeVoiceConnection(String.valueOf(guildId));
    }
    
    @Override
    public void chunkMembers(@Nonnull final String guildId, @Nonnull final String query, @Nonnegative final int limit,
                             @Nullable final String nonce) {
        shardManager().shard(shardIdFor(guildId)).queueSendToSocket(
                CatnipShardImpl.basePayload(GatewayOp.REQUEST_GUILD_MEMBERS,
                        JsonObject.builder()
                                .value("guild_id", guildId)
                                .value("query", query)
                                .value("limit", limit)
                                .value("nonce", nonce)
                                .done()));
    }
    
    @Override
    public Presence presence(@Nonnegative final int shardId) {
        return shardManager().shard(shardId).presence();
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
        //noinspection ResultOfMethodCallIgnored
        selfUser()
                .flatMap(self -> {
                    if(self != null && status == null) {
                        return cache()
                                .presence(self.id())
                                .map(presence -> presence == null ? OnlineStatus.ONLINE : presence.status());
                    } else {
                        return Maybe.just(status == null ? OnlineStatus.ONLINE : status);
                    }
                })
                .subscribe(stat -> {
                    // TODO: Seriously, what the fuck Rx.
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
                            .activities(activity != null ? List.of(activity) : List.of())
                            .build());
                });
    }
    
    @Nonnull
    public Single<Catnip> setup() {
        if(!startedKeepalive) {
            startedKeepalive = true;
            keepaliveThread.start();
        }
        if(validateToken) {
            return fetchGatewayInfo()
                    .flatMap(gateway -> {
                        logAdapter().info("Token validated!");
                        clientIdAsLong = Catnip.parseIdFromToken(token);
                        return checkIntentsAndLog();
                        // return Single.just((Catnip) this);
                    }).doOnError(e -> {
                        logAdapter().warn("Couldn't validate token!", e);
                        throw new RuntimeException(e);
                    });
        } else {
            if(!token.isEmpty()) {
                try {
                    clientIdAsLong = Catnip.parseIdFromToken(token);
                } catch(final IllegalArgumentException e) {
                    final Exception wrapped = new RuntimeException("The provided token was invalid!", e);
                    return Single.error(wrapped);
                }
            }
            
            return Single.just(this);
        }
    }
    
    private Single<Catnip> checkIntentsAndLog() {
        return rest.user().getCurrentApplicationInformation().map(info -> {
            final var hasMembersIntent = info.flags().contains(ApplicationFlag.GATEWAY_GUILD_MEMBERS) || info.flags().contains(ApplicationFlag.GATEWAY_GUILD_MEMBERS_LIMITED);
            final var hasPresenceIntent = info.flags().contains(ApplicationFlag.GATEWAY_PRESENCE) || info.flags().contains(ApplicationFlag.GATEWAY_PRESENCE_LIMITED);
            if(!hasMembersIntent || !hasPresenceIntent) {
                logAdapter().error("#########################################");
            }
            if(!hasMembersIntent && options.intents().contains(GatewayIntent.GUILD_MEMBERS)) {
                logAdapter().error("GUILD_MEMBERS intent passed but is not enabled in the developer dashboard, this will fail!");
                logAdapter().error("Please go enable those intents first, and THEN try running your bot.");
                logAdapter().error("Click here to go to the dashboard: https://discord.com/developers/applications/{}",
                        Catnip.parseIdFromToken(options.token()));
            }
            if(!hasPresenceIntent && options.intents().contains(GatewayIntent.GUILD_PRESENCES)) {
                logAdapter().error("GUILD_PRESENCES intent passed but is not enabled in the developer dashboard, this will fail!");
                logAdapter().error("Please go enable those intents first, and THEN try running your bot.");
                logAdapter().error("Click here to go to the dashboard: https://discord.com/developers/applications/{}",
                        Catnip.parseIdFromToken(options.token()));
            }
            if(!hasMembersIntent || !hasPresenceIntent) {
                logAdapter().error("#########################################");
            }
            return this;
        });
    }
    
    private void injectSelf() {
        // Inject catnip instance into dependent fields
        dispatchManager().catnip(this);
        shardManager().catnip(this);
        eventBuffer().catnip(this);
        cacheWorker().catnip(this);
        options.requester().catnip(this);
        taskScheduler().catnip(this);
        final List<GatewayIntent> privilegedIntents = options.intents()
                .stream()
                .filter(GatewayIntent::privileged)
                .collect(Collectors.toList());
        if(!options.enableGuildSubscriptions() && options.intents().isEmpty()) {
            logAdapter().warn("Guild subscriptions are disabled and no intents specified!");
            logAdapter().warn("You probably want to use intents instead.");
        }
        if(!privilegedIntents.isEmpty() && options.logPrivilegedIntentWarning()) {
            // TODO: Check application flags to make sure this is actually a
            //  necessary log -- requires REST request.
            logAdapter().warn("catnip is configured with the following privileged intents: {}", privilegedIntents);
            logAdapter().warn("Please make sure your bot is whitelisted to use these intents!");
        }
    }
    
    @Nonnull
    public Catnip connect() {
        shardManager().start();
        return this;
    }
    
    private int shardIdFor(@Nonnull final String guildId) {
        final long idLong = Long.parseUnsignedLong(guildId);
        return (int) ((idLong >>> 22) % shardManager().shardCount());
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
