package com.mewna.catnip.entity.misc;

import com.mewna.catnip.entity.Snowflake;
import com.mewna.catnip.entity.user.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author amy
 * @since 10/17/18.
 */
public interface ApplicationInfo extends Snowflake {
    @Nonnull
    String name();
    
    @Nullable
    String icon();
    
    @Nullable
    String description();
    
    @Nonnull
    List<String> rpcOrigins();
    
    boolean publicBot();
    
    boolean requiresCodeGrant();
    
    @Nonnull
    User owner();
}
