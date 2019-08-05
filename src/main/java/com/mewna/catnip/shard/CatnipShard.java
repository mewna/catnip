package com.mewna.catnip.shard;

import com.grack.nanojson.JsonObject;
import com.mewna.catnip.entity.impl.user.PresenceImpl;
import com.mewna.catnip.entity.user.Presence;
import io.reactivex.Single;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.List;

public interface CatnipShard {
    /**
     * Fetches the trace from the shard.
     *
     * @return The shard's trace.
     */
    @Nonnull
    List<String> getTrace();
    
    /**
     * Checks whether or not the current shard is currently connected to the
     * websocket gateway. This is done as a boolean because - at least for now
     * - there's only 2 meaningful states: connected, and queued to be connected.
     *
     * @return Whether or not the shard with the given id is currently
     * connected to the websocket gateway.
     */
    @CheckReturnValue
    boolean isConnected();
    
    /**
     * Get the lifecycle state for the current shard. This provides more
     * meaningful info than {@link #isConnected()}, because it provides more
     * granular info about the shard's state. However, it does not
     * differentiate between "connected" or not per se; it is up to the
     * end-user to determine whether or not the lifecycle state is actually a
     * state of being "connected."
     *
     * @return The lifecycle state of the shard.
     */
    @Nonnull
    @CheckReturnValue
    LifecycleState lifecycleState();
    
    /**
     * Return the shard's computed gateway latency, ie. the time it takes for
     * the shard to send a heartbeat to Discord and get a response.
     *
     * @return The shard's computed gateway latency.
     */
    @CheckReturnValue
    long lastHeartbeatLatency();
    
    /**
     * Return the shard's current presence.
     *
     * @return The shard's current presence.
     */
    @Nonnull
    @CheckReturnValue
    Presence presence();
    
    // internal methods
    
    @Nonnull
    @CheckReturnValue
    Single<ShardConnectState> connect();
    
    void disconnect();
    
    void updatePresence(@Nonnull final PresenceImpl presence);
    
    void queueSendToSocket(@Nonnull final JsonObject json);
    
    void queueVoiceStateUpdate(@Nonnull final JsonObject json);
    
    void sendToSocket(@Nonnull final JsonObject json);
}
