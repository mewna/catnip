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
    private final String name;
    
    MentionParseFlag(final String name) {
        this.name = name;
    }
    
    @Nonnull
    @CheckReturnValue
    public static MentionParseFlag byName(final String name) {
        for(final MentionParseFlag m : values()) {
            if(m.name.equals(name)) {
                return m;
            }
        }
        throw new IllegalArgumentException("No such MessageParse: " + name);
    }
}
