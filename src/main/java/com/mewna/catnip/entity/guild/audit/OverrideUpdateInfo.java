package com.mewna.catnip.entity.guild.audit;

import com.mewna.catnip.entity.guild.PermissionOverride.OverrideType;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author SamOphis
 * @since 10/07/18
 */
@SuppressWarnings("unused")
public interface OverrideUpdateInfo extends OptionalEntryInfo {
    @CheckReturnValue
    long overriddenEntityId();
    
    @CheckReturnValue
    @Nonnull
    OverrideType overrideType();

    @CheckReturnValue
    @Nullable
    String roleName();
}