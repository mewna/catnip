package com.mewna.catnip;

import com.mewna.catnip.cache.CacheFlag;
import com.mewna.catnip.cache.EntityCache;
import com.mewna.catnip.cache.EntityCacheWorker;
import com.mewna.catnip.entity.user.Presence;
import com.mewna.catnip.entity.user.Presence.ActivityType;
import com.mewna.catnip.entity.user.Presence.OnlineStatus;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.extension.Extension;
import com.mewna.catnip.extension.manager.ExtensionManager;
import com.mewna.catnip.internal.CatnipImpl;
import com.mewna.catnip.internal.logging.LogAdapter;
import com.mewna.catnip.internal.ratelimit.Ratelimiter;
import com.mewna.catnip.rest.Rest;
import com.mewna.catnip.rest.RestRequester;
import com.mewna.catnip.rest.Routes;
import com.mewna.catnip.shard.EventType;
import com.mewna.catnip.shard.event.EventBuffer;
import com.mewna.catnip.shard.manager.ShardManager;
import com.mewna.catnip.shard.session.SessionManager;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author amy
 * @since 9/3/18.
 */
@SuppressWarnings({"unused", "OverlyCoupledClass"})
public interface Catnip {
    /**
     * Create a new catnip instance with the given token.
     *
     * @param token The token to be used for all API operations.
     *
     * @return A new catnip instance.
     */
    static Catnip catnip(@Nonnull final String token) {
        return catnip(token, Vertx.vertx());
    }
    
    /**
     * Create a new catnip instance with the given options.
     *
     * @param options The options to be applied to the catnip instance.
     *
     * @return A new catnip instance.
     */
    static Catnip catnip(@Nonnull final CatnipOptions options) {
        return catnip(options, Vertx.vertx());
    }
    
    /**
     * Create a new catnip instance with the given token and vert.x instance.
     *
     * @param token The token to be used for all API operations.
     * @param vertx The vert.x instance used to run the bot.
     *
     * @return A new catnip instance.
     */
    static Catnip catnip(@Nonnull final String token, @Nonnull final Vertx vertx) {
        return catnip(new CatnipOptions(token), vertx);
    }
    
    /**
     * Create a new catnip instance with the given options and vert.x instance.
     *
     * @param options The options to be applied to the catnip instance.
     * @param vertx   The vert.x instance used to run the bot.
     *
     * @return A new catnip instance.
     */
    static Catnip catnip(@Nonnull final CatnipOptions options, @Nonnull final Vertx vertx) {
        return new CatnipImpl(vertx, options).setup();
    }
    
    /**
     * The gateway URL used to connect shards.
     * <p/>
     * TODO: Properly fetch this from the API.
     *
     * @return The websocket gateway URL.
     */
    @Nonnull
    @CheckReturnValue
    static String getGatewayUrl() {
        // TODO: Allow injecting other gateway URLs for eg. mocks?
        return "wss://gateway.discord.gg/?v=6&encoding=json&compress=zlib-stream";
    }
    
    /**
     * @return The URL used to fetch recommended shard count.
     */
    @Nonnull
    @CheckReturnValue
    static String getShardCountUrl() {
        // TODO: Allow injecting other endpoints for eg. mocks?
        //return "https://discordapp.com/api/v6/gateway/bot";
        return RestRequester.API_HOST + RestRequester.API_BASE + Routes.GET_GATEWAY_BOT.baseRoute();
    }
    
    /**
     * @return The vert.x instance being used by this catnip instance.
     */
    @Nonnull
    @CheckReturnValue
    Vertx vertx();
    
    // Implementations are lombok-generated
    
    /**
     * @return The event bus used by the vert.x instance that this catnip
     * instance uses.
     *
     * @see #vertx()
     */
    @Nonnull
    @CheckReturnValue
    EventBus eventBus();
    
    /**
     * Start all shards asynchronously. To customize the shard spawning /
     * management strategy, see {@link CatnipOptions}.
     *
     * @return Itself.
     */
    @Nonnull
    Catnip startShards();
    
    /**
     * @return The token being used by this catnip instance.
     */
    @Nonnull
    String token();
    
    /**
     * @return The shard manager being used by this catnip instance.
     */
    @Nonnull
    ShardManager shardManager();
    
    /**
     * @return The session manager being used by this catnip instance.
     */
    @Nonnull
    SessionManager sessionManager();
    
    /**
     * @return The gateway message send ratelimiter being used by this catnip
     * instance.
     */
    @Nonnull
    Ratelimiter gatewayRatelimiter();
    
    /**
     * @return The REST API instance for this catnip instance.
     */
    @Nonnull
    @CheckReturnValue
    Rest rest();
    
    /**
     * The logging adapter. This is used throughout the lib to log things, and
     * may additionally be used by user code if you don't want to set up your
     * own logging things. The logging adapter is exposed like this because it
     * is possible to specify a custom logging adapter in
     * {@link CatnipOptions}; generally you should just stick with the provided
     * default SLF4J logging adapter.
     *
     * @return The logging adapter being used by this catnip instance.
     */
    @Nonnull
    LogAdapter logAdapter();
    
    /**
     * @return The event buffer being used by this catnip instance.
     */
    @Nonnull
    EventBuffer eventBuffer();
    
    /**
     * @return The entity cache being used by this catnip instance.
     */
    @Nonnull
    EntityCache cache();
    
    /**
     * The cache worker being used by this catnip instance. You should use this
     * if you need to do special caching for some reason.
     *
     * @return The cache worker being used by this catnip instance.
     */
    @Nonnull
    EntityCacheWorker cacheWorker();
    
    /**
     * The set of cache flags to be applied to the entity cache. These
     * currently allow for simply dropping data instead of caching it, but may
     * be expanded on in the future.
     *
     * @return The set of cache flags being used by this catnip instance.
     */
    @Nonnull
    Set<CacheFlag> cacheFlags();
    
    /**
     * @return Whether or not this catnip instance will chunk guild members
     * when it connects to the websocket gateway.
     */
    boolean chunkMembers();
    
    /**
     * @return Whether or not this catnip instance will emit event objects as
     * it receives events from the gateway. You should only disable this if you
     * want to do something special with the raw event objects via hooks.
     */
    boolean emitEventObjects();
    
    /**
     * @return A set of all ids of unavailable guilds.
     */
    @Nonnull
    Set<String> unavailableGuilds();
    
    /**
     * @param guildId The guild to check.
     *
     * @return Whether or not the guild is unavailable.
     */
    boolean isUnavailable(@Nonnull final String guildId);
    
    /**
     * @return The extension manager being used by this catnip instance.
     */
    @Nonnull
    ExtensionManager extensionManager();
    
    /**
     * Load an extension for this catnip instance. See {@link Extension} for
     * more information.
     *
     * @param extension The extension to load.
     *
     * @return Itself.
     */
    @Nonnull
    Catnip loadExtension(@Nonnull Extension extension);
    
    /**
     * @return The currently-logged-in user. May be {@code null} if no shards
     * have logged in.
     */
    @Nullable
    User selfUser();
    
    /**
     * @return The initial presence to set when logging in via the gateway.
     * Will be null if not set via {@link CatnipOptions}.
     */
    @Nullable
    @CheckReturnValue
    Presence initialPresence();
    
    /**
     * @return The set of events that will not be fired. Empty by default.
     */
    @Nonnull
    @CheckReturnValue
    Set<String> disabledEvents();
    
    /**
     * Opens a voice connection to the provided guild and channel. The connection is
     * opened asynchronously, with
     * {@link com.mewna.catnip.shard.DiscordEvent#VOICE_STATE_UPDATE VOICE_STATE_UPDATE} and
     * {@link com.mewna.catnip.shard.DiscordEvent#VOICE_SERVER_UPDATE VOICE_SERVER_UPDATE}
     * events being fired when the connection is opened.
     *
     * @param guildId Guild to connect.
     * @param channelId Channel to connect.
     */
    //TODO self mute/self deaf?
    void openVoiceConnection(@Nonnull String guildId, @Nonnull String channelId);
    
    /**
     * Closes the voice connection on the specified guild.
     *
     * @param guildId Guild to disconnect.
     */
    void closeVoiceConnection(@Nonnull String guildId);
    
    /**
     * Get the presence for the specified shard.
     *
     * @param shardId  The shard id to get presence for.
     * @param callback The callback invoked when the presence is fetched.
     */
    void presence(@Nonnegative final int shardId, @Nonnull final Consumer<Presence> callback);
    
    /**
     * Update the presence for all shards.
     *
     * @param presence The new presence to set.
     */
    void presence(@Nonnull final Presence presence);
    
    /**
     * Update the presence for a specific shard.
     *
     * @param presence The new presence to set.
     * @param shardId  The shard to set presence for.
     */
    void presence(@Nonnull final Presence presence, @Nonnegative final int shardId);
    
    /**
     * Update the presence for all shards by specifying each part of the
     * presence individually.
     *
     * @param status The new online status. Set to {@code null} for online.
     * @param game   The new game name. Set to {@code null} to clear.
     * @param type   The type of the new game status. Set to {@code null} for
     *               "playing."
     * @param url    The new URL for the presence. Will be ignored if {@code type}
     *               is not {@link ActivityType#STREAMING}.
     */
    void presence(@Nullable final OnlineStatus status, @Nullable final String game, @Nullable final ActivityType type,
                          @Nullable final String url);
    
    /**
     * Update the online status for all shards. Will clear the activity status.
     *
     * @param status The new online status to set.
     */
    default void status(@Nonnull final OnlineStatus status) {
        presence(status, null, null, null);
    }
    
    /**
     * Update the activity status for all shards. Will set the online status to
     * {@link OnlineStatus#ONLINE}
     *
     * @param game The new game to set.
     * @param type The type of the activity.
     * @param url  The URL if streaming. Will be ignored if {@code type} is not
     *             {@link ActivityType#STREAMING}.
     */
    default void game(@Nonnull final String game, @Nonnull final ActivityType type, @Nullable final String url) {
        presence(null, game, type, url);
    }
    
    /**
     * Add a consumer for the specified event type.
     *
     * @param type The type of event to listen on.
     * @param <T>  The object type of event being listened on.
     *
     * @return The vert.x message consumer.
     */
    default <T> MessageConsumer<T> on(@Nonnull final EventType<T> type) {
        return eventBus().consumer(type.key());
    }
    
    /**
     * Add a consumer for the specified event type with the given handler
     * callback.
     *
     * @param type    The type of event to listen on.
     * @param handler The handler for the event object.
     * @param <T>     The object type of event being listened on.
     *
     * @return The vert.x message consumer.
     */
    default <T> MessageConsumer<T> on(@Nonnull final EventType<T> type, @Nonnull final Consumer<T> handler) {
        return eventBus().consumer(type.key(), message -> handler.accept(message.body()));
    }
    
    /**
     * Shutdown the catnip instance, undeploy all shards, and shutdown the
     * vert.x instance.
     */
    default void shutdown() {
        shutdown(true);
    }
    
    /**
     * Shutdown the catnip instance, undeploy all shards, and optionally
     * shutdown the vert.x instance.
     *
     * @param vertx Whether or not to shut down the vert.x instance.
     */
    void shutdown(boolean vertx);
}
