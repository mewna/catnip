package com.mewna.catnip;

import com.mewna.catnip.cache.CacheFlag;
import com.mewna.catnip.cache.EntityCache;
import com.mewna.catnip.cache.EntityCacheWorker;
import com.mewna.catnip.extension.Extension;
import com.mewna.catnip.extension.manager.ExtensionManager;
import com.mewna.catnip.internal.CatnipImpl;
import com.mewna.catnip.internal.logging.LogAdapter;
import com.mewna.catnip.internal.ratelimit.Ratelimiter;
import com.mewna.catnip.rest.Rest;
import com.mewna.catnip.rest.RestRequester;
import com.mewna.catnip.rest.Routes;
import com.mewna.catnip.shard.event.EventBuffer;
import com.mewna.catnip.shard.manager.ShardManager;
import com.mewna.catnip.shard.session.SessionManager;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

/**
 * @author amy
 * @since 9/3/18.
 */
@SuppressWarnings("unused")
public interface Catnip {
    static Catnip catnip() {
        return new CatnipImpl().setup();
    }
    
    static Catnip catnip(@Nonnull final Vertx vertx) {
        return new CatnipImpl(vertx).setup();
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
    
    @Nullable
    String token();
    
    @Nonnull
    Catnip token(@Nonnull String token);
    
    @Nonnull
    ShardManager shardManager();
    
    @Nonnull
    Catnip shardManager(@Nonnull ShardManager shardManager);
    
    @Nonnull
    SessionManager sessionManager();
    
    @Nonnull
    Catnip sessionManager(@Nonnull SessionManager sessionManager);
    
    @Nonnull
    Ratelimiter gatewayRatelimiter();
    
    @Nonnull
    Catnip gatewayRatelimiter(@Nonnull Ratelimiter ratelimiter);
    
    @Nonnull
    @CheckReturnValue
    Rest rest();
    
    @Nonnull
    Catnip rest(@Nonnull Rest rest);
    
    @Nonnull
    LogAdapter logAdapter();
    
    @Nonnull
    Catnip logAdapter(@Nonnull LogAdapter adapter);

    @Nonnull
    EventBuffer eventBuffer();
    
    @Nonnull
    Catnip eventBuffer(@Nonnull EventBuffer eventBuffer);
    
    @Nonnull
    EntityCache cache();
    
    @Nonnull
    EntityCacheWorker cacheWorker();
    
    @Nonnull
    Catnip cache(@Nonnull EntityCacheWorker cache);
    
    @Nonnull
    Set<CacheFlag> cacheFlags();
    
    @Nonnull
    Catnip cacheFlags(@Nonnull Set<CacheFlag> cacheFlags);
    
    @Nonnull
    ExtensionManager extensionManager();
    
    @Nonnull
    Catnip extensionManager(@Nonnull ExtensionManager extensionManager);
    
    @Nonnull
    Catnip loadExtension(@Nonnull Extension extension);
}
