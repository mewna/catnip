/*
 * Copyright (c) 2018 amy, All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.mewna.catnip;

import com.mewna.catnip.cache.EntityCache;
import com.mewna.catnip.cache.EntityCacheWorker;
import com.mewna.catnip.entity.channel.Webhook;
import com.mewna.catnip.entity.impl.EntityBuilder;
import com.mewna.catnip.entity.misc.GatewayInfo;
import com.mewna.catnip.entity.serialization.EntitySerializer;
import com.mewna.catnip.entity.user.Presence;
import com.mewna.catnip.entity.user.Presence.ActivityType;
import com.mewna.catnip.entity.user.Presence.OnlineStatus;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.extension.Extension;
import com.mewna.catnip.extension.manager.ExtensionManager;
import com.mewna.catnip.internal.CatnipImpl;
import com.mewna.catnip.rest.Rest;
import com.mewna.catnip.shard.buffer.EventBuffer;
import com.mewna.catnip.shard.event.DispatchManager;
import com.mewna.catnip.shard.event.DoubleEventType;
import com.mewna.catnip.shard.event.EventType;
import com.mewna.catnip.shard.event.MessageConsumer;
import com.mewna.catnip.shard.manager.ShardManager;
import com.mewna.catnip.shard.session.SessionManager;
import com.mewna.catnip.util.CatnipOptionsView;
import com.mewna.catnip.util.Utils;
import com.mewna.catnip.util.logging.LogAdapter;
import com.mewna.catnip.util.scheduler.TaskScheduler;
import io.reactivex.rxjava3.core.*;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.tuple.Pair;
import org.bouncycastle.asn1.edec.EdECObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * The base catnip interface. Almost everything catnip-related will be accessed
 * through this interface -- REST access, shard management, some utilities, and
 * more. The main exception to this rule is entities that have convenience
 * methods exposed on their interfaces.<p />
 * <p>
 * Note that this interface extends {@link AutoCloseable}; this is meant for
 * cases where some relatively-fast blocking operations are desirable, and a
 * long-term catnip instance is not needed. An example of this is doing some
 * quick REST calls when blocking a thread is not an issue. Another possible
 * use-case is (ab)using the built-in {@link TaskScheduler} for one reason or
 * another, although creating an entire catnip instance solely for that would
 * be quite silly.
 *
 * @author amy
 * @since 9/3/18.
 */
@SuppressWarnings({"unused", "OverlyCoupledClass", "RedundantSuppression"})
public interface Catnip extends AutoCloseable {
    /**
     * Create a new catnip instance with the given token.
     * <p>
     * <strong>This method may block while validating the provided token.</strong>
     *
     * @param token The token to be used for all API operations.
     *
     * @return A new catnip instance.
     */
    static Catnip catnip(@Nonnull final String token) {
        return catnipAsync(token).blockingGet();
    }
    
    /**
     * Create a new catnip instance with the given token.
     * <p>
     * <strong>This method may block while validating the provided token.</strong>
     *
     * @param token The token to be used for all API operations.
     *
     * @return A new catnip instance.
     */
    static Single<Catnip> catnipAsync(@Nonnull final String token) {
        return catnipAsync(new CatnipOptions(token));
    }
    
    /**
     * Create a new catnip instance with the given options.
     * <p>
     * <strong>This method may block while validating the provided token.</strong>
     *
     * @param options The options to be applied to the catnip instance.
     *
     * @return A new catnip instance.
     */
    static Catnip catnip(@Nonnull final CatnipOptions options) {
        return catnipAsync(options).blockingGet();
    }
    
    /**
     * Create a new catnip instance with the given options.
     * <p>
     * <strong>This method may block while validating the provided token.</strong>
     *
     * @param options The options to be applied to the catnip instance.
     *
     * @return A new catnip instance.
     */
    static Single<Catnip> catnipAsync(@Nonnull final CatnipOptions options) {
        return new CatnipImpl(options).setup();
    }
    
    /**
     * Parses a token and returns the client id encoded therein. Throws a
     * {@link IllegalArgumentException} if the provided token is not
     * well-formed.<br />
     * <p>
     * See the following image for an explanation of the Discord token format:<br/>
     *
     * <img src="https://i.imgur.com/7WdehGn.png" alt="Token format" />
     *
     * @param token The token to parse.
     *
     * @return The client id encoded in the token.
     *
     * @throws IllegalArgumentException If the provided token is not well-formed.
     */
    static long parseIdFromToken(final String token) {
        try {
            final String clientIdBase64 = token.split("\\.")[0];
            final String clientId = new String(Base64.getDecoder().decode(clientIdBase64));
            return Long.parseUnsignedLong(clientId);
        } catch(final IllegalArgumentException e) {
            throw new IllegalArgumentException("Provided token was invalid!", e);
        }
    }
    
    /**
     * @return An immutable view of the current instance's options.
     */
    @Nonnull
    @CheckReturnValue
    CatnipOptionsView options();
    
    /**
     * @return The cached gateway info. May be null if it hasn't been fetched
     * yet.
     */
    @Nullable
    @CheckReturnValue
    GatewayInfo gatewayInfo();
    
    /**
     * Fetches the gateway info and updates the cache. Calls made to {@link #gatewayInfo()}
     * after this stage completes successfully are guaranteed to return a non null value.
     * <p>
     * Updates the cached gateway info.
     *
     * @return The gateway info fetched from discord.
     */
    @Nonnull
    @CheckReturnValue
    Single<GatewayInfo> fetchGatewayInfo();
    
    // Implementations are lombok-generated
    
    /**
     * Start all shards asynchronously. To customize the shard spawning /
     * management strategy, see {@link CatnipOptions}.
     *
     * @return Itself.
     */
    @Nonnull
    Catnip connect();
    
    @Nonnull
    @CheckReturnValue
    default Scheduler rxScheduler() {
        return options().rxScheduler();
    }
    
    /**
     * Handles dispatching and listening to events.
     *
     * @return The current dispatch manager instance.
     */
    @Nonnull
    @CheckReturnValue
    default DispatchManager dispatchManager() {
        return options().dispatchManager();
    }
    
    /**
     * @return The shard manager being used by this catnip instance.
     */
    @Nonnull
    default ShardManager shardManager() {
        return options().shardManager();
    }
    
    /**
     * @return The session manager being used by this catnip instance.
     */
    @Nonnull
    default SessionManager sessionManager() {
        return options().sessionManager();
    }
    
    /**
     * @return The event buffer being used by this catnip instance.
     */
    @Nonnull
    default EventBuffer eventBuffer() {
        return options().eventBuffer();
    }
    
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
    default LogAdapter logAdapter() {
        return options().logAdapter();
    }
    
    /**
     * @return The entity cache being used by this catnip instance.
     */
    @Nonnull
    default EntityCache cache() {
        return options().cacheWorker();
    }
    
    /**
     * The cache worker being used by this catnip instance. You should use this
     * if you need to do special caching for some reason.
     *
     * @return The cache worker being used by this catnip instance.
     */
    @Nonnull
    default EntityCacheWorker cacheWorker() {
        return options().cacheWorker();
    }
    
    /**
     * The task scheduler allows for scheduling one-off and recurring tasks
     * that are executed at some point in the future. By default, the task
     * scheduler is effectively just a simple wrapper over
     * {@link Observable#timer(long, TimeUnit)} and
     * {@link Observable#interval(long, TimeUnit)}.
     *
     * @return The task scheduler used by this catnip instance.
     */
    default TaskScheduler taskScheduler() {
        return options().taskScheduler();
    }
    
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
     * Return a single extension by class. If multiple extensions are loaded
     * from the same class, there is no guarantee which extension instance will
     * be returned, in which case you should be using {@link ExtensionManager#matchingExtensions(Class)}.
     *
     * @param extensionClass The extension class to find instances of
     * @param <T>            Type of the extension.
     *
     * @return A possibly-{@code null} instance of the passed extension class.
     */
    @Nullable
    default <T extends Extension> T extension(@Nonnull final Class<T> extensionClass) {
        return extensionManager().extension(extensionClass);
    }
    
    /**
     * Inject options into this catnip instance from the given extension. This
     * allows extensions to do things like automatically register a new cache
     * worker without having to tell the end-user to specify options. By
     * default, options that get injected will be logged.
     *
     * @param extension      The extension injecting the options.
     * @param optionsPatcher Function responsible for updating the settings.
     *
     * @return Itself.
     *
     * @throws IllegalArgumentException When the given extension isn't loaded.
     */
    @Nonnull
    @SuppressWarnings("UnusedReturnValue")
    Catnip injectOptions(@Nonnull Extension extension, @Nonnull UnaryOperator<CatnipOptions> optionsPatcher);
    
    /**
     * @return The currently-logged-in user. May be {@code null} if no shards
     * have logged in.
     */
    @Nonnull
    @CheckReturnValue
    Maybe<User> selfUser();
    
    /**
     * The ID of this client
     *
     * @return The ID of this client.
     */
    String clientId();
    
    /**
     * The ID of this client, as a long.
     *
     * @return The ID of the client, as a long.
     */
    long clientIdAsLong();
    
    /**
     * @return The entity serializer that catnip uses for converting entities
     * into an external-friendly format.
     */
    default EntitySerializer<?> entitySerializer() {
        return options().entitySerializer();
    }
    
    /**
     * Opens a voice connection to the provided guild and channel. The connection is
     * opened asynchronously, with
     * {@link com.mewna.catnip.shard.DiscordEvent#VOICE_STATE_UPDATE VOICE_STATE_UPDATE} and
     * {@link com.mewna.catnip.shard.DiscordEvent#VOICE_SERVER_UPDATE VOICE_SERVER_UPDATE}
     * events being fired when the connection is opened.
     *
     * @param guildId   Guild to connect.
     * @param channelId Channel to connect.
     */
    default void openVoiceConnection(@Nonnull final String guildId, @Nonnull final String channelId) {
        openVoiceConnection(guildId, channelId, false, false);
    }
    
    /**
     * Opens a voice connection to the provided guild and channel. The connection is
     * opened asynchronously, with
     * {@link com.mewna.catnip.shard.DiscordEvent#VOICE_STATE_UPDATE VOICE_STATE_UPDATE} and
     * {@link com.mewna.catnip.shard.DiscordEvent#VOICE_SERVER_UPDATE VOICE_SERVER_UPDATE}
     * events being fired when the connection is opened.
     *
     * @param guildId   Guild to connect.
     * @param channelId Channel to connect.
     * @param selfMute  Whether or not to connect as muted.
     * @param selfDeaf  Whether or not to connect as deafened.
     */
    void openVoiceConnection(@Nonnull String guildId, @Nonnull String channelId, boolean selfMute, boolean selfDeaf);
    
    /**
     * Opens a voice connection to the provided guild and channel. The connection is
     * opened asynchronously, with
     * {@link com.mewna.catnip.shard.DiscordEvent#VOICE_STATE_UPDATE VOICE_STATE_UPDATE} and
     * {@link com.mewna.catnip.shard.DiscordEvent#VOICE_SERVER_UPDATE VOICE_SERVER_UPDATE}
     * events being fired when the connection is opened.
     *
     * @param guildId   Guild to connect.
     * @param channelId Channel to connect.
     */
    default void openVoiceConnection(final long guildId, final long channelId) {
        openVoiceConnection(guildId, channelId, false, false);
    }
    
    /**
     * Opens a voice connection to the provided guild and channel. The connection is
     * opened asynchronously, with
     * {@link com.mewna.catnip.shard.DiscordEvent#VOICE_STATE_UPDATE VOICE_STATE_UPDATE} and
     * {@link com.mewna.catnip.shard.DiscordEvent#VOICE_SERVER_UPDATE VOICE_SERVER_UPDATE}
     * events being fired when the connection is opened.
     *
     * @param guildId   Guild to connect.
     * @param channelId Channel to connect.
     * @param selfMute  Whether or not to connect as muted.
     * @param selfDeaf  Whether or not to connect as deafened.
     */
    default void openVoiceConnection(final long guildId, final long channelId, final boolean selfMute,
                                     final boolean selfDeaf) {
        openVoiceConnection(String.valueOf(guildId), String.valueOf(channelId), selfMute, selfDeaf);
    }
    
    /**
     * Closes the voice connection on the specified guild.
     *
     * @param guildId Guild to disconnect.
     */
    void closeVoiceConnection(@Nonnull String guildId);
    
    /**
     * Closes the voice connection on the specified guild.
     *
     * @param guildId Guild to disconnect.
     */
    void closeVoiceConnection(long guildId);
    
    /**
     * Request all guild members for the given guild.
     *
     * @param guildId Guild to request for.
     */
    default void chunkMembers(final long guildId) {
        chunkMembers(Long.toString(guildId));
    }
    
    /**
     * Request all guild members for the given guild.
     *
     * @param guildId Guild to request for.
     */
    default void chunkMembers(@Nonnull final String guildId) {
        chunkMembers(guildId, "", 0, null);
    }
    
    /**
     * Request guild members for the given guild.
     *
     * @param guildId Guild to request for.
     * @param query   Member names must start with this.
     */
    default void chunkMembers(final long guildId, @Nonnull final String query) {
        chunkMembers(Long.toString(guildId), query);
    }
    
    /**
     * Request guild members for the given guild.
     *
     * @param guildId Guild to request for.
     * @param query   Member names must start with this.
     */
    default void chunkMembers(@Nonnull final String guildId, @Nonnull final String query) {
        chunkMembers(guildId, query, 0, null);
    }
    
    /**
     * Request guild members for the given guild.
     *
     * @param guildId Guild to request for.
     * @param limit   Maximum number of members to return. 0 for no limit.
     */
    default void chunkMembers(final long guildId, @Nonnegative final int limit) {
        chunkMembers(Long.toString(guildId), "", limit, null);
    }
    
    /**
     * Request guild members for the given guild.
     *
     * @param guildId Guild to request for.
     * @param limit   Maximum number of members to return. 0 for no limit.
     */
    default void chunkMembers(@Nonnull final String guildId, @Nonnegative final int limit) {
        chunkMembers(guildId, "", limit, null);
    }
    
    /**
     * Request guild members for the given guild.
     *
     * @param guildId Guild to request for.
     * @param query   Members returned must have a username starting with this.
     * @param limit   Maximum number of members to return. 0 for no limit.
     */
    default void chunkMembers(final long guildId, @Nonnull final String query, @Nonnegative final int limit) {
        chunkMembers(Long.toString(guildId), query, limit, null);
    }
    
    /**
     * Request guild members for the given guild.
     *
     * @param guildId  Guild to request for.
     * @param nonce    Nonce to use for knowing which chunks came from which request.
     * @param _useless Differentiates this method from {@link #chunkMembers(String, String)}.
     *                 Otherwise useless.
     */
    default void chunkMembers(@Nonnull final String guildId, @Nonnull final String nonce, final boolean _useless) {
        chunkMembers(guildId, "", 0, nonce);
    }
    
    /**
     * Request guild members for the given guild.
     *
     * @param guildId Guild to request for.
     * @param query   Members returned must have a username starting with this.
     * @param nonce   Nonce to use for knowing which chunks came from which request.
     */
    default void chunkMembers(@Nonnull final String guildId, @Nonnull final String query, @Nonnull final String nonce) {
        chunkMembers(guildId, query, 0, nonce);
    }
    
    /**
     * Request guild members for the given guild.
     *
     * @param guildId Guild to request for.
     * @param limit   Maximum number of members to return. 0 for no limit.
     * @param nonce   Nonce to use for knowing which chunks came from which request.
     */
    default void chunkMembers(@Nonnull final String guildId, @Nonnegative final int limit, @Nonnull final String nonce) {
        chunkMembers(guildId, "", limit, nonce);
    }
    
    /**
     * Request guild members for the given guild.
     *
     * @param guildId Guild to request for.
     * @param query   Members returned must have a username starting with this.
     * @param limit   Maximum number of members to return. 0 for no limit.
     * @param nonce   Nonce to use for knowing which chunks came from which request.
     */
    void chunkMembers(@Nonnull String guildId, @Nonnull String query, @Nonnegative int limit, @Nullable String nonce);
    
    /**
     * Get the presence for the specified shard.
     *
     * @param shardId The shard id to get presence for.
     *
     * @return The shard's presence.
     */
    Presence presence(@Nonnegative final int shardId);
    
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
     * @return The message consumer.
     */
    private <T> MessageConsumer<T> on(@Nonnull final EventType<T> type) {
        return dispatchManager().createConsumer(type.key());
    }
    
    /**
     * Add a consumer for the specified event type with the given handler
     * callback.
     *
     * @param type    The type of event to listen on.
     * @param handler The handler for the event object.
     * @param <T>     The object type of event being listened on.
     *
     * @return The message consumer.
     */
    private <T> MessageConsumer<T> on(@Nonnull final EventType<T> type, @Nonnull final Consumer<T> handler) {
        return on(type).handler(handler);
    }
    
    /**
     * Add a reactive stream handler for events of the given type. Can be
     * disposed of with {@link Observable#unsubscribeOn(Scheduler)}. The
     * {@code scheduler} argument can be created with
     * {@link #rxScheduler()}.
     * <p>
     * This method automatically subscribes on {@link #rxScheduler()}.
     *
     * @param type The type of event to stream.
     * @param <T>  The object type of the event being streamed.
     *
     * @return The observable.
     */
    default <T> Observable<T> observable(@Nonnull final EventType<T> type) {
        return on(type).asObservable().subscribeOn(rxScheduler()).observeOn(rxScheduler());
    }
    
    /**
     * Add a reactive stream handler for events of the given type.  Can be
     * disposed of with {@link Flowable#unsubscribeOn(Scheduler)}. The
     * {@code scheduler} argument can be created with
     * {@link #rxScheduler()}.
     * <p>
     * This method automatically subscribes on {@link #rxScheduler()}.
     *
     * @param type The type of event to stream.
     * @param <T>  The object type of the event being streamed.
     *
     * @return The flowable.
     */
    default <T> Flowable<T> flowable(@Nonnull final EventType<T> type) {
        return on(type).asFlowable().subscribeOn(rxScheduler()).observeOn(rxScheduler());
    }
    
    /**
     * Add a consumer for the specified event type with the given handler
     * callback.
     *
     * @param type The type of event to listen on.
     * @param <T>  The first object type of event being listened on.
     * @param <E>  The second object type of event being listened on.
     *
     * @return The message consumer.
     */
    private <T, E> MessageConsumer<Pair<T, E>> on(@Nonnull final DoubleEventType<T, E> type) {
        return dispatchManager().createConsumer(type.key());
    }
    
    /**
     * Add a consumer for the specified event type with the given handler
     * callback.
     *
     * @param type    The type of event to listen on.
     * @param handler The handler for the event object.
     * @param <T>     The first object type of event being listened on.
     * @param <E>     The second object type of event being listened on.
     *
     * @return The message consumer.
     */
    private <T, E> MessageConsumer<Pair<T, E>> on(@Nonnull final DoubleEventType<T, E> type,
                                                  @Nonnull final BiConsumer<T, E> handler) {
        return on(type).handler(m -> handler.accept(m.getLeft(), m.getRight()));
    }
    
    /**
     * Add a reactive stream handler for events of the given type. Can be
     * disposed of with {@link Observable#unsubscribeOn(Scheduler)}. The
     * {@code scheduler} argument can be created with
     * {@link #rxScheduler()}.
     * <p>
     * This method automatically subscribes on {@link #rxScheduler()}.
     *
     * @param type The type of event to stream.
     * @param <T>  The object type of the event being streamed.
     * @param <E>  The object type of the event being streamed.
     *
     * @return The observable.
     */
    default <T, E> Observable<Pair<T, E>> observable(@Nonnull final DoubleEventType<T, E> type) {
        return on(type).asObservable().subscribeOn(rxScheduler()).observeOn(rxScheduler());
    }
    
    /**
     * Add a reactive stream handler for events of the given type. Can be
     * disposed of with {@link Flowable#unsubscribeOn(Scheduler)}. The
     * {@code scheduler} argument can be created with
     * {@link #rxScheduler()}.
     * <p>
     * This method automatically subscribes on {@link #rxScheduler()}.
     *
     * @param type The type of event to stream.
     * @param <T>  The object type of the event being streamed.
     * @param <E>  The object type of the event being streamed.
     *
     * @return The flowable.
     */
    default <T, E> Flowable<Pair<T, E>> flowable(@Nonnull final DoubleEventType<T, E> type) {
        return on(type).asFlowable().subscribeOn(rxScheduler()).observeOn(rxScheduler());
    }
    
    /**
     * Shutdown the catnip instance, and undeploy all shards.
     */
    void shutdown();
    
    /**
     * Get a webhook object for the specified webhook URL. This method will
     * attempt to validate the webhook.
     *
     * @param webhookUrl The URL of the webhook.
     *
     * @return A Single that completes when the webhook is validated.
     */
    default Single<Webhook> parseWebhook(final String webhookUrl) {
        final Pair<String, String> parse = Utils.parseWebhook(webhookUrl);
        return parseWebhook(parse.getLeft(), parse.getRight());
    }
    
    /**
     * Get a webhook object for the specified webhook URL. This method will
     * attempt to validate the webhook.
     *
     * @param id    The webhook's id.
     * @param token The webhook's token.
     *
     * @return A Single that completes when the webhook is validated.
     */
    default Single<Webhook> parseWebhook(final String id, final String token) {
        return rest().webhook().getWebhookToken(id, token);
    }
    
    @Override
    default void close() {
        shutdown();
    }
    
    /**
     * Validates an ed25519 signature for interactions.
     *
     * @param signature The signature to validate. This is the
     *                  {@code x-signature-ed25519} header.
     * @param ts        The timestamp of the request. This is the
     *                  {@code x-signature-timestamp} heaqder.
     * @param data      The data to validate. This is the body of the request.
     *
     * @return Whether or not the signature is valid.
     */
    default boolean validateSignature(@Nonnull final String signature, @Nonnull final String ts, @Nonnull final String data) {
        try {
            if(options().publicKey() == null) {
                throw new IllegalStateException("cannot validate signature when public key is null!");
            }
            
            @SuppressWarnings("ConstantConditions")
            final var byteKey = Hex.decodeHex(options().publicKey());
            final var pki = new SubjectPublicKeyInfo(new AlgorithmIdentifier(EdECObjectIdentifiers.id_Ed25519), byteKey);
            final var pkSpec = new X509EncodedKeySpec(pki.getEncoded());
            final var kf = KeyFactory.getInstance("ed25519", CatnipImpl.BOUNCY_CASTLE_PROVIDER);
            final var publicKey = kf.generatePublic(pkSpec);
            final var signedData = Signature.getInstance("ed25519", CatnipImpl.BOUNCY_CASTLE_PROVIDER);
            signedData.initVerify(publicKey);
            signedData.update(ts.getBytes(StandardCharsets.UTF_8));
            signedData.update(data.getBytes(StandardCharsets.UTF_8));
            return signedData.verify(Hex.decodeHex(signature));
        } catch(final NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to get ed25519 signature provider!", e);
        } catch(final InvalidKeySpecException e) {
            throw new IllegalStateException("Unable to create keyspec for public key!", e);
        } catch(final InvalidKeyException e) {
            throw new IllegalArgumentException("Invalid public key!", e);
        } catch(final SignatureException e) {
            throw new IllegalStateException("Signature improperly initialised!", e);
        } catch(final DecoderException e) {
            throw new IllegalStateException("Couldn't decode public key into bytes!", e);
        } catch(final IOException e) {
            throw new IllegalArgumentException("Couldn't encode pubkey into X509!", e);
        }
    }
    
    /**
     * The entity builder used by this catnip instance. This is exposed
     * publicly so that, if necessary, it can be used to construct entities
     * from JSON objects as needed.
     *
     * @return This catnip instance's {@code EntityBuilder}.
     */
    EntityBuilder entityBuilder();
}
