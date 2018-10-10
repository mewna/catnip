package com.mewna.catnip.entity.guild;

import com.mewna.catnip.entity.Snowflake;
import com.mewna.catnip.entity.util.ImageOptions;
import com.mewna.catnip.entity.util.Permission;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

/**
 * @author natanbc
 * @since 10/10/18.
 */
public interface PartialGuild extends Snowflake {
    @Nonnull
    @CheckReturnValue
    String name();
    
    @Nullable
    @CheckReturnValue
    String icon();
    
    @Nullable
    @CheckReturnValue
    String iconUrl(@Nonnull final ImageOptions options);
    
    @Nullable
    @CheckReturnValue
    default String iconUrl() {
        return iconUrl(new ImageOptions());
    }
    
    @CheckReturnValue
    boolean owned();
    
    @Nonnull
    @CheckReturnValue
    Set<Permission> permissions();
}
