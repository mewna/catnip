package com.mewna.mew;

import com.mewna.mew.entity.Message;
import com.mewna.mew.rest.Rest;
import com.mewna.mew.rest.RestRequester;
import com.mewna.mew.shard.manager.DefaultShardManager;
import com.mewna.mew.shard.manager.ShardManager;
import com.mewna.mew.shard.session.DefaultSessionManager;
import com.mewna.mew.shard.session.SessionManager;
import com.mewna.mew.util.JsonPojoCodec;
import com.mewna.mew.util.ratelimit.MemoryRatelimiter;
import com.mewna.mew.util.ratelimit.Ratelimiter;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Possible names:
 * - solace
 * - sapphire
 * - mew (lol no)
 * - catnip
 * - sakura
 *
 * @author amy
 * @since 8/31/18.
 */
@Accessors(fluent = true, chain = true)
public class Mew {
    @Getter
    private static final Vertx vertx = Vertx.vertx();
    @Getter
    private final RestRequester _requester = new RestRequester(this);
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
    
    public static String getGatewayUrl() {
        // TODO: Allow injecting other gateway URLs for eg. mocks?
        return "wss://gateway.discord.gg/?v=6&encoding=json";
    }
    
    public static String getShardCountUrl() {
        // TODO: Allow injecting other endpoints for eg. mocks?
        return "https://discordapp.com/api/v6/gateway/bot";
    }
    
    public static EventBus eventBus() {
        return vertx.eventBus();
    }
    
    public Mew setup() {
        // Register codecs
        // God I hate having to do this
        // This is necessary to make Vert.x allow passing arbitrary objects
        // over the bus tho, since it doesn't obey typical Java serialization
        // stuff (for reasons I don't really get) and won't just dump stuff to
        // JSON when it doesn't have a codec
        // *sigh*
        eventBus().registerDefaultCodec(Message.class, new JsonPojoCodec<>(Message.class));
        
        shardManager.setMew(this);
        return this;
    }
    
    public Mew startShards() {
        shardManager.start();
        return this;
    }
}
