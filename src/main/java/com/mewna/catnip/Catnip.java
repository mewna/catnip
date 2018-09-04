package com.mewna.catnip;

import com.mewna.catnip.internal.CatnipImpl;
import com.mewna.catnip.internal.logging.LogAdapter;
import com.mewna.catnip.rest.Rest;
import com.mewna.catnip.rest.RestRequester;
import com.mewna.catnip.rest.Routes;
import com.mewna.catnip.shard.manager.ShardManager;
import com.mewna.catnip.shard.session.SessionManager;
import com.mewna.catnip.internal.ratelimit.Ratelimiter;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author amy
 * @since 9/3/18.
 */
public interface Catnip {
    static Catnip catnip() {
        return new CatnipImpl().setup();
    }
    
    @Nonnull
    Vertx vertx();
    
    @Nonnull
    @CheckReturnValue
    EventBus eventBus();
    
    @Nonnull
    Catnip startShards();
    
    // Implementations are lombok-generated
    
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
    Catnip logAdapter(LogAdapter adapter);
    
    @Nonnull
    @CheckReturnValue
    static String getGatewayUrl() {
        // TODO: Allow injecting other gateway URLs for eg. mocks?
        return "wss://gateway.discord.gg/?v=6&encoding=json";
    }
    
    @Nonnull
    @CheckReturnValue
    static String getShardCountUrl() {
        // TODO: Allow injecting other endpoints for eg. mocks?
        //return "https://discordapp.com/api/v6/gateway/bot";
        return RestRequester.API_HOST + RestRequester.API_BASE + Routes.GET_GATEWAY_BOT.baseRoute();
    }
}
