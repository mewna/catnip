package com.mewna.mew;

import com.mewna.mew.rest.Rest;
import com.mewna.mew.rest.RestRequester;
import com.mewna.mew.shard.manager.DefaultShardManager;
import com.mewna.mew.shard.manager.ShardManager;
import com.mewna.mew.shard.session.DefaultSessionManager;
import com.mewna.mew.shard.session.SessionManager;
import com.mewna.mew.util.ratelimit.MemoryRatelimiter;
import com.mewna.mew.util.ratelimit.Ratelimiter;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author amy
 * @since 8/31/18.
 */
@Accessors(fluent = true, chain = true)
public class Mew {
    @Getter
    private static final Vertx vertx = Vertx.vertx();
    private final Logger logger = LoggerFactory.getLogger(Mew.class);
    
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
    
    @Getter
    private final RestRequester restRequester = new RestRequester(this);
    
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
    
    public void boot() {
        shardManager.setMew(this);
        shardManager.start();
    }
}
