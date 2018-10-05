package com.mewna.catnip.entity.guild;

import com.mewna.catnip.entity.Snowflake;
import com.mewna.catnip.entity.util.Permission;
import lombok.Getter;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Set;

/**
 * @author natanbc
 * @since 9/15/18
 */
public interface PermissionOverride extends Snowflake {
    @Nonnull
    @CheckReturnValue
    OverrideType type();
    
    @Nonnull
    @CheckReturnValue
    Set<Permission> allow();
    
    @Nonnull
    @CheckReturnValue
    Set<Permission> deny();
    
    enum OverrideType {
        ROLE("role"), MEMBER("member");
        
        @Getter
        private final String key;
    
        OverrideType(final String key) {
            this.key = key;
        }
        
        @Nonnull
        public static OverrideType byKey(final String key) {
            for(final OverrideType level : values()) {
                if(level.key.equals(key)) {
                    return level;
                }
            }
            throw new IllegalArgumentException("No override type for key " + key);
        }
    }
}
