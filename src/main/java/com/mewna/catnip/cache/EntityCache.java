package com.mewna.catnip.cache;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.*;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author amy
 * @since 9/13/18.
 */
@SuppressWarnings("unused")
public interface EntityCache {
    @Nonnull
    EntityCache updateCache(@Nonnull String eventType, @Nonnull JsonObject payload);
    
    @Nullable
    Guild guild(@Nonnull String id);
    
    @Nullable
    User user(@Nonnull String id);
    
    @Nullable
    Member member(@Nonnull String guildId, @Nonnull String id);
    
    @Nullable
    Role role(@Nonnull String guildId, @Nonnull String id);
    
    @Nullable
    Channel channel(@Nonnull String guildId, @Nonnull String id);
    
    @Nonnull
    Catnip catnip();
    
    @Nonnull
    EntityCache catnip(@Nonnull Catnip catnip);
}
