package com.mewna.catnip.internal;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.CatnipOptions;
import com.mewna.catnip.cache.CacheFlag;
import com.mewna.catnip.cache.EntityCacheWorker;
import com.mewna.catnip.entity.impl.*;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.extension.Extension;
import com.mewna.catnip.extension.manager.DefaultExtensionManager;
import com.mewna.catnip.extension.manager.ExtensionManager;
import com.mewna.catnip.internal.logging.LogAdapter;
import com.mewna.catnip.internal.ratelimit.Ratelimiter;
import com.mewna.catnip.rest.Rest;
import com.mewna.catnip.rest.RestRequester;
import com.mewna.catnip.shard.event.EventBuffer;
import com.mewna.catnip.shard.manager.ShardManager;
import com.mewna.catnip.shard.session.SessionManager;
import com.mewna.catnip.util.JsonPojoCodec;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import lombok.Getter;
import lombok.experimental.Accessors;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

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
    private final String token;
    @Getter
    private final ShardManager shardManager;
    @Getter
    private final SessionManager sessionManager;
    @Getter
    private final Ratelimiter gatewayRatelimiter;
    @Getter
    private final Rest rest = new Rest(this);
    @Getter
    private final LogAdapter logAdapter;
    @Getter
    private final ExtensionManager extensionManager = new DefaultExtensionManager(this);
    @Getter
    private final EventBuffer eventBuffer;
    @Getter
    private final EntityCacheWorker cache;
    @Getter
    private final Set<CacheFlag> cacheFlags;
    
    public CatnipImpl(@Nonnull final Vertx vertx, @Nonnull final CatnipOptions options) {
        this.vertx = vertx;
        requester = new RestRequester(this);
        token = options.token();
        shardManager = options.shardManager();
        sessionManager = options.sessionManager();
        gatewayRatelimiter = options.gatewayRatelimiter();
        logAdapter = options.logAdapter();
        eventBuffer = options.eventBuffer();
        cache = options.cacheWorker();
        cacheFlags = options.cacheFlags();
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
    
    @Nullable
    @Override
    public User selfUser() {
        return cache.selfUser();
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
        
        // Lifecycle
        codec(ReadyImpl.class);
        
        // Messages
        codec(MessageImpl.class);
        codec(DeletedMessageImpl.class);
        codec(BulkDeletedMessagesImpl.class);
        codec(TypingUserImpl.class);
        codec(ReactionUpdateImpl.class);
        codec(BulkRemovedReactionsImpl.class);
        
        // Channels
        codec(CategoryImpl.class);
        codec(GroupDMChannelImpl.class);
        codec(TextChannelImpl.class);
        codec(UserDMChannelImpl.class);
        codec(VoiceChannelImpl.class);
        codec(WebhookImpl.class);
        
        // Guilds
        codec(GuildImpl.class);
        codec(GatewayGuildBanImpl.class);
        
        // Roles
        codec(RoleImpl.class);
        codec(PartialRoleImpl.class);
        codec(PermissionOverrideImpl.class);
        
        // Members
        codec(MemberImpl.class);
        codec(PartialMemberImpl.class);
        
        // Users
        codec(UserImpl.class);
        codec(PresenceImpl.class);
        
        // Voice
        codec(VoiceStateImpl.class);
        codec(VoiceServerUpdateImpl.class);
        
        return this;
    }
    
    private <T> void codec(@Nonnull final Class<T> cls) {
        eventBus().registerDefaultCodec(cls, new JsonPojoCodec<>(this, cls));
    }
    
    @Nonnull
    @Override
    public EntityCacheWorker cacheWorker() {
        return cache;
    }
    
    @Nonnull
    public Catnip startShards() {
        if(token == null || token.isEmpty()) {
            throw new IllegalStateException("Provided token is empty!");
        }
        shardManager.catnip(this);
        eventBuffer.catnip(this);
        cache.catnip(this);
        shardManager.start();
        return this;
    }
}
