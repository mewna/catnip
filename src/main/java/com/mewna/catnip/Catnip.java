package com.mewna.catnip;

import com.mewna.catnip.internal.CatnipImpl;
import com.mewna.catnip.rest.Rest;
import com.mewna.catnip.shard.manager.ShardManager;
import com.mewna.catnip.shard.session.SessionManager;
import com.mewna.catnip.util.ratelimit.Ratelimiter;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;

/**
 * @author amy
 * @since 9/3/18.
 */
public interface Catnip {
    static Catnip catnip() {
        return new CatnipImpl().setup();
    }
    
    Vertx vertx();
    
    EventBus eventBus();
    
    Catnip startShards();
    
    // Implementations are lombok-generated
    
    String token();
    
    Catnip token(String token);
    
    ShardManager shardManager();
    
    Catnip shardManager(ShardManager shardManager);
    
    SessionManager sessionManager();
    
    Catnip sessionManager(SessionManager sessionManager);
    
    Ratelimiter gatewayRatelimiter();
    
    Catnip gatewayRatelimiter(Ratelimiter ratelimiter);
    
    Rest rest();
    
    Catnip rest(Rest rest);
}
