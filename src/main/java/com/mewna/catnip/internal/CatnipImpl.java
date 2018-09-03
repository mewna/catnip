package com.mewna.catnip.internal;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.Message;
import com.mewna.catnip.rest.Rest;
import com.mewna.catnip.rest.RestRequester;
import com.mewna.catnip.rest.Routes;
import com.mewna.catnip.shard.manager.DefaultShardManager;
import com.mewna.catnip.shard.manager.ShardManager;
import com.mewna.catnip.shard.session.DefaultSessionManager;
import com.mewna.catnip.shard.session.SessionManager;
import com.mewna.catnip.util.JsonPojoCodec;
import com.mewna.catnip.util.ratelimit.MemoryRatelimiter;
import com.mewna.catnip.util.ratelimit.Ratelimiter;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * @author amy
 * @since 8/31/18.
 */
@Accessors(fluent = true, chain = true)
public class CatnipImpl implements Catnip {
    @Getter
    private static final Vertx _vertx = Vertx.vertx();
    @Getter
    private final RestRequester requester = new RestRequester(this);
    @Getter
    @Setter
    private String token;
    @Getter
    @Setter
    private ShardManager shardManager = new DefaultShardManager();
    @Getter
    @Setter
    private SessionManager sessionManager = new DefaultSessionManager();
    @Getter
    @Setter
    private Ratelimiter gatewayRatelimiter = new MemoryRatelimiter();
    // TODO: Add to builder
    @Getter
    @Setter
    private Rest rest = new Rest(this);
    
    @Nonnull
    @CheckReturnValue
    public static String getGatewayUrl() {
        // TODO: Allow injecting other gateway URLs for eg. mocks?
        return "wss://gateway.discord.gg/?v=6&encoding=json";
    }
    
    @Nonnull
    @CheckReturnValue
    public static String getShardCountUrl() {
        // TODO: Allow injecting other endpoints for eg. mocks?
        //return "https://discordapp.com/api/v6/gateway/bot";
        return RestRequester.API_HOST + RestRequester.API_BASE + Routes.GET_GATEWAY_BOT.baseRoute();
    }
    
    @Nonnull
    @Override
    public Vertx vertx() {
        return _vertx();
    }
    
    @Nonnull
    @Override
    @CheckReturnValue
    public EventBus eventBus() {
        return _vertx().eventBus();
    }
    
    @Nonnull
    public Catnip setup() {
        // Register codecs
        // God I hate having to do this
        // This is necessary to make Vert.x allow passing arbitrary objects
        // over the bus tho, since it doesn't obey typical Java serialization
        // stuff (for reasons I don't really get) and won't just dump stuff to
        // JSON when it doesn't have a codec
        // *sigh*
        // This is mainly important for distributed catnip; locally it'll just
        // not apply any transformations
        eventBus().registerDefaultCodec(Message.class, new JsonPojoCodec<>(Message.class));
        
        shardManager.setCatnip(this);
        return this;
    }
    
    @Nonnull
    public Catnip startShards() {
        if(token == null || token.isEmpty()) {
            throw new IllegalStateException("Provided token is empty!");
        }
        shardManager.start();
        return this;
    }
}
