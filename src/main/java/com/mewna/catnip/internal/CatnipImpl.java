package com.mewna.catnip.internal;

import com.google.common.collect.ImmutableSet;
import com.mewna.catnip.Catnip;
import com.mewna.catnip.CatnipOptions;
import com.mewna.catnip.cache.CacheFlag;
import com.mewna.catnip.cache.EntityCacheWorker;
import com.mewna.catnip.entity.impl.*;
import com.mewna.catnip.entity.impl.PresenceImpl.ActivityImpl;
import com.mewna.catnip.entity.user.Presence;
import com.mewna.catnip.entity.user.Presence.Activity;
import com.mewna.catnip.entity.user.Presence.ActivityType;
import com.mewna.catnip.entity.user.Presence.OnlineStatus;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.extension.Extension;
import com.mewna.catnip.extension.manager.DefaultExtensionManager;
import com.mewna.catnip.extension.manager.ExtensionManager;
import com.mewna.catnip.internal.logging.LogAdapter;
import com.mewna.catnip.internal.ratelimit.Ratelimiter;
import com.mewna.catnip.rest.Rest;
import com.mewna.catnip.rest.RestRequester;
import com.mewna.catnip.shard.CatnipShard;
import com.mewna.catnip.shard.ShardInfo;
import com.mewna.catnip.shard.event.EventBuffer;
import com.mewna.catnip.shard.manager.ShardManager;
import com.mewna.catnip.shard.session.SessionManager;
import com.mewna.catnip.util.JsonPojoCodec;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.experimental.Accessors;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * @author amy
 * @since 8/31/18.
 */
@Getter
@SuppressWarnings("OverlyCoupledClass")
@Accessors(fluent = true, chain = true)
public class CatnipImpl implements Catnip {
    private final Vertx vertx;
    private final RestRequester requester;
    private final String token;
    private final ShardManager shardManager;
    private final SessionManager sessionManager;
    private final Ratelimiter gatewayRatelimiter;
    private final Rest rest = new Rest(this);
    private final LogAdapter logAdapter;
    private final ExtensionManager extensionManager = new DefaultExtensionManager(this);
    private final EventBuffer eventBuffer;
    private final EntityCacheWorker cache;
    private final Set<CacheFlag> cacheFlags;
    private final boolean chunkMembers;
    private final boolean emitEventObjects;
    private final Presence initialPresence;
    
    private final AtomicReference<User> selfUser = new AtomicReference<>(null);
    private final Set<String> unavailableGuilds = new HashSet<>();
    private final Set<String> disabledEvents;
    
    public CatnipImpl(@Nonnull final Vertx vertx, @Nonnull final CatnipOptions options) {
        this.vertx = vertx;
        requester = new RestRequester(this, options.restBucketBackend(), options.restHttpClient());
        token = options.token();
        shardManager = options.shardManager();
        sessionManager = options.sessionManager();
        gatewayRatelimiter = options.gatewayRatelimiter();
        logAdapter = options.logAdapter();
        eventBuffer = options.eventBuffer();
        cache = options.cacheWorker();
        cacheFlags = options.cacheFlags();
        chunkMembers = options.chunkMembers();
        emitEventObjects = options.emitEventObjects();
        initialPresence = options.presence();
        disabledEvents = ImmutableSet.copyOf(options.disabledEvents());
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
        return selfUser.get();
    }
    
    @Override
    public void shutdown(final boolean vertx) {
        shardManager.shutdown();
        if(vertx) {
            this.vertx.close();
        }
    }
    
    @Nonnull
    @SuppressWarnings("UnusedReturnValue")
    public Catnip selfUser(@Nonnull final User self) {
        selfUser.set(self);
        return this;
    }
    
    @Nonnull
    @Override
    public Set<String> unavailableGuilds() {
        return ImmutableSet.copyOf(unavailableGuilds);
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
    public void openVoiceConnection(@Nonnull final String guildId, @Nonnull final String channelId) {
        eventBus().send(CatnipShard.websocketMessageQueueAddress(shardIdFor(guildId)),
                new JsonObject()
                        .put("op", 4)
                        .put("d", new JsonObject()
                                .put("guild_id", guildId)
                                .put("channel_id", channelId)
                                .put("self_mute", false)
                                .put("self_deaf", false)
                        )
        );
    }
    
    @Override
    public void closeVoiceConnection(@Nonnull final String guildId) {
        eventBus().send(CatnipShard.websocketMessageQueueAddress(shardIdFor(guildId)),
                new JsonObject()
                        .put("op", 4)
                        .put("d", new JsonObject()
                                .put("guild_id", guildId)
                                .putNull("channel_id")
                                .put("self_mute", false)
                                .put("self_deaf", false)
                        )
        );
    }
    
    @Override
    public void presence(@Nonnegative final int shardId, @Nonnull final Consumer<Presence> callback) {
        eventBus().send(CatnipShard.websocketMessagePresenceUpdateAddress(shardId), null,
                result -> callback.accept((Presence) result.result().body()));
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
        eventBus().publish(CatnipShard.websocketMessagePresenceUpdateAddress(shardId), presence);
    }
    
    @Override
    public void presence(@Nullable final OnlineStatus status, @Nullable final String game, @Nullable final ActivityType type,
                         @Nullable final String url) {
        final OnlineStatus stat;
        if(status != null) {
            stat = status;
        } else {
            final User self = selfUser();
            if(self != null) {
                final Presence presence = cache().presence(self.id());
                stat = presence == null ? OnlineStatus.ONLINE : presence.status();
            } else {
                stat = OnlineStatus.ONLINE;
            }
        }
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
                .activity(activity)
                .build());
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
        codec(ResumedImpl.class);
        
        // Messages
        codec(MessageImpl.class);
        codec(DeletedMessageImpl.class);
        codec(BulkDeletedMessagesImpl.class);
        codec(TypingUserImpl.class);
        codec(ReactionUpdateImpl.class);
        codec(BulkRemovedReactionsImpl.class);
        codec(MessageEmbedUpdateImpl.class);
        
        // Channels
        codec(CategoryImpl.class);
        codec(GroupDMChannelImpl.class);
        codec(TextChannelImpl.class);
        codec(UserDMChannelImpl.class);
        codec(VoiceChannelImpl.class);
        codec(WebhookImpl.class);
        codec(ChannelPinsUpdateImpl.class);
        codec(WebhooksUpdateImpl.class);
        
        // Guilds
        codec(GuildImpl.class);
        codec(GatewayGuildBanImpl.class);
        codec(EmojiUpdateImpl.class);
        
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
        
        // Shards
        codec(ShardInfo.class);
        
        // Inject catnip instance into dependent fields
        shardManager.catnip(this);
        eventBuffer.catnip(this);
        cache.catnip(this);
        
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
        shardManager.start();
        return this;
    }
    
    private int shardIdFor(@Nonnull final String guildId) {
        final long idLong = Long.parseUnsignedLong(guildId);
        return (int)((idLong >>> 22) % shardManager.shardCount());
    }
}
