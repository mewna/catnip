package com.mewna.catnip.entity.guild;

import com.mewna.catnip.entity.Snowflake;
import com.mewna.catnip.entity.user.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

/**
 * @author amy
 * @since 10/4/18.
 */
public interface PartialMember extends Snowflake {
    @Nonnull
    default String id() {
        return user().id();
    }
    
    @Nonnull
    User user();
    
    @Nonnull
    String guildId();
    
    @Nonnull
    Set<String> roleIds();
    
    @Nullable
    String nick();
}
