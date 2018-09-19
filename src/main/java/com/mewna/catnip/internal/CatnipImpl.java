package com.mewna.catnip.internal;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.impl.MemberImpl;
import com.mewna.catnip.entity.impl.MessageImpl;
import com.mewna.catnip.entity.impl.RoleImpl;
import com.mewna.catnip.entity.impl.UserImpl;
import com.mewna.catnip.extension.Extension;
import com.mewna.catnip.extension.manager.DefaultExtensionManager;
import com.mewna.catnip.extension.manager.ExtensionManager;
import com.mewna.catnip.internal.logging.DefaultLogAdapter;
import com.mewna.catnip.internal.logging.LogAdapter;
import com.mewna.catnip.internal.ratelimit.MemoryRatelimiter;
import com.mewna.catnip.internal.ratelimit.Ratelimiter;
import com.mewna.catnip.rest.Rest;
import com.mewna.catnip.rest.RestRequester;
import com.mewna.catnip.shard.event.EventBuffer;
import com.mewna.catnip.shard.event.NoopBuffer;
import com.mewna.catnip.shard.manager.DefaultShardManager;
import com.mewna.catnip.shard.manager.ShardManager;
import com.mewna.catnip.shard.session.DefaultSessionManager;
import com.mewna.catnip.shard.session.SessionManager;
import com.mewna.catnip.util.JsonPojoCodec;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * TODO: This thing has a giant number of dependencies - how to split it up?
 *
 * @author amy
 * @since 8/31/18.
 */
@SuppressWarnings("OverlyCoupledClass")
@Accessors(fluent = true, chain = true)
public class CatnipImpl implements Catnip {
    @Getter
    private final Vertx vertx;
    // TODO: Allow changing the backend
    @Getter
    private final RestRequester requester;
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
    @Setter
    private Rest rest = new Rest(this);
    @Getter
    @Setter
    private LogAdapter logAdapter = new DefaultLogAdapter();
    @Getter
    @Setter
    private ExtensionManager extensionManager = new DefaultExtensionManager(this);
    @Getter
    @Setter
    private EventBuffer eventBuffer = new NoopBuffer();
    
    public CatnipImpl() {
        this(Vertx.vertx());
    }
    
    public CatnipImpl(@Nonnull final Vertx vertx) {
        this.vertx = vertx;
        requester = new RestRequester(this);
    }
    
    @Nonnull
    @Override
    @CheckReturnValue
    public EventBus eventBus() {
        return vertx.eventBus();
    }
    
    @Nonnull
    @Override
    public Catnip loadExtension(@Nonnull final Extension extension) {
        extensionManager.loadExtension(extension);
        return this;
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
        eventBus().registerDefaultCodec(MessageImpl.class, new JsonPojoCodec<>(this, MessageImpl.class));
        eventBus().registerDefaultCodec(UserImpl.class, new JsonPojoCodec<>(this, UserImpl.class));
        eventBus().registerDefaultCodec(RoleImpl.class, new JsonPojoCodec<>(this, RoleImpl.class));
        eventBus().registerDefaultCodec(MemberImpl.class, new JsonPojoCodec<>(this, MemberImpl.class));
        
        return this;
    }
    
    @Nonnull
    public Catnip startShards() {
        if(token == null || token.isEmpty()) {
            throw new IllegalStateException("Provided token is empty!");
        }
        shardManager.catnip(this);
        eventBuffer.catnip(this);
        shardManager.start();
        return this;
    }
}
