package com.mewna.catnip.shard;

import com.mewna.catnip.entity.impl.user.PresenceImpl;
import com.mewna.catnip.entity.user.Presence;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import java.util.List;

public interface CatnipShard {
    //region handleControlMessage
    @Nonnull
    List<String> getTrace();
    
    boolean isConnected();
    
    @Nonnull
    LifecycleState getLifecycleState();
    
    long getLastHeartbeatLatency();
    
    @Nonnull
    Single<ShardConnectState> connect();
    //endregion
    
    //region handleSocketQueue
    void handleSocketQueue(final JsonObject json);
    //endregion
    
    //region handleSocketSend
    void handleSocketSend(final JsonObject json);
    //endregion
    
    //region handlePresenceUpdate
    void updatePresence(@Nonnull final PresenceImpl presence);
    
    @Nonnull
    Presence getPresence();
    //endregion
    
    //region handleVoiceStateUpdateQueue
    void queueVoiceStateUpdate(final JsonObject json);
    //endregion
}
