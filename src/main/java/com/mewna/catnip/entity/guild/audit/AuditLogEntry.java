package com.mewna.catnip.entity.guild.audit;

import com.mewna.catnip.entity.Entity;
import com.mewna.catnip.entity.Snowflake;
import com.mewna.catnip.entity.user.User;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author SamOphis
 * @since 10/07/18
 */
@SuppressWarnings("unused")
public interface AuditLogEntry extends Snowflake {
    @Nullable
    @CheckReturnValue
    String targetId();
    
    @Nonnull
    @CheckReturnValue
    User user();
    
    @Nullable
    @CheckReturnValue
    String reason();
    
    @Nullable
    @CheckReturnValue
    OptionalEntryInfo options();
    
    @Nonnull
    @CheckReturnValue
    ActionType type();
    
    @Nonnull
    @CheckReturnValue
    List<AuditLogChange> changes();
}
