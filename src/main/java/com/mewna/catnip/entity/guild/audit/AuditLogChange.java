package com.mewna.catnip.entity.guild.audit;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author SamOphis
 * @since 10/07/18
 */
@SuppressWarnings("unused")
public interface AuditLogChange {
    @Nullable
    @CheckReturnValue
    <T> T newValue();
    
    @Nullable
    @CheckReturnValue
    <T> T oldValue();
    
    @Nonnull
    @CheckReturnValue
    String key();
}
