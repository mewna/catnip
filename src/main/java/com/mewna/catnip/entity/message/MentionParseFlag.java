package com.mewna.catnip.entity.message;

import lombok.Getter;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * @author kjp12
 * @see MessageOptions#parseFlags()
 * @since March 07, 2020
 */
public enum MentionParseFlag {
    ROLES("roles"),
    USERS("users"),
    EVERYONE("everyone"),
    ;
    @Getter
    private final String flagName;
    
    MentionParseFlag(final String flagName) {
        this.flagName = flagName;
    }
    
    @Nonnull
    @CheckReturnValue
    public static MentionParseFlag byName(final String flagName) {
        for(final MentionParseFlag m : values()) {
            if (m.flagName.equals(flagName)) {
                return m;
            }
        }
        throw new IllegalArgumentException("No such MentionParseFlag: " + flagName);
    }
}
