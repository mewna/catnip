package com.mewna.catnip;

import com.mewna.catnip.cache.CacheFlag;
import com.mewna.catnip.cache.EntityCache;
import com.mewna.catnip.cache.EntityCacheWorker;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.extension.Extension;
import com.mewna.catnip.extension.manager.ExtensionManager;
import com.mewna.catnip.internal.CatnipImpl;
import com.mewna.catnip.internal.logging.LogAdapter;
import com.mewna.catnip.internal.ratelimit.Ratelimiter;
import com.mewna.catnip.rest.Rest;
import com.mewna.catnip.rest.RestRequester;
import com.mewna.catnip.rest.Routes;
import com.mewna.catnip.shard.DiscordEvent.EventType;
import com.mewna.catnip.shard.event.EventBuffer;
import com.mewna.catnip.shard.manager.ShardManager;
import com.mewna.catnip.shard.session.SessionManager;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author amy
 * @since 9/3/18.
 */
@SuppressWarnings("unused")
public interface Catnip {
    static Catnip catnip(@Nonnull final String token) {
        return catnip(token, Vertx.vertx());
    }
    
    static Catnip catnip(@Nonnull final CatnipOptions options) {
        return catnip(options, Vertx.vertx());
    }
    
    static Catnip catnip(@Nonnull final String token, @Nonnull final Vertx vertx) {
        return catnip(new CatnipOptions(token), vertx);
    }
    
    static Catnip catnip(@Nonnull final CatnipOptions options, @Nonnull final Vertx vertx) {
        return new CatnipImpl(vertx, options).setup();
    }
    
    @Nonnull
    @CheckReturnValue
    static String getGatewayUrl() {
        // TODO: Allow injecting other gateway URLs for eg. mocks?
        return "wss://gateway.discord.gg/?v=6&encoding=json&compress=zlib-stream";
    }
    
    @Nonnull
    @CheckReturnValue
    static String getShardCountUrl() {
        // TODO: Allow injecting other endpoints for eg. mocks?
        //return "https://discordapp.com/api/v6/gateway/bot";
        return RestRequester.API_HOST + RestRequester.API_BASE + Routes.GET_GATEWAY_BOT.baseRoute();
    }
    
    @Nonnull
    @CheckReturnValue
    Vertx vertx();
    
    // Implementations are lombok-generated
    
    @Nonnull
    @CheckReturnValue
    EventBus eventBus();
    
    @Nonnull
    Catnip startShards();
    
    @Nonnull
    String token();
    
    @Nonnull
    ShardManager shardManager();
    
    @Nonnull
    SessionManager sessionManager();
    
    @Nonnull
    Ratelimiter gatewayRatelimiter();
    
    @Nonnull
    @CheckReturnValue
    Rest rest();
    
    @Nonnull
    LogAdapter logAdapter();
    
    @Nonnull
    EventBuffer eventBuffer();
    
    @Nonnull
    EntityCache cache();
    
    @Nonnull
    EntityCacheWorker cacheWorker();
    
    @Nonnull
    Set<CacheFlag> cacheFlags();
    
    @Nonnull
    ExtensionManager extensionManager();
    
    @Nonnull
    Catnip loadExtension(@Nonnull Extension extension);
    
    @Nullable
    User selfUser();
    
    boolean chunkMembers();
    
    default <T> MessageConsumer<T> on(@Nonnull final EventType<T> type) {
        return eventBus().consumer(type.key());
    }
    
    default <T> MessageConsumer<T> on(@Nonnull final EventType<T> type, @Nonnull final Consumer<T> handler) {
        return eventBus().consumer(type.key(), message -> handler.accept(message.body()));
    }
}
