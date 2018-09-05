package com.mewna.catnip.entity;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * @author natanbc
 * @since 9/5/18.
 */
public interface Emoji extends Snowflake {
    @Nullable
    @CheckReturnValue
    String id();
    
    @Nonnull
    @CheckReturnValue
    String name();
    
    @Nonnull
    @CheckReturnValue
    List<String> roles();
    
    @Nullable
    @CheckReturnValue
    User user();
    
    @CheckReturnValue
    boolean requiresColons();
    
    @CheckReturnValue
    boolean managed();
    
    @CheckReturnValue
    boolean animated();
    
    @CheckReturnValue
    boolean custom();
    
    interface CustomEmoji extends Emoji {
        @Override
        @Nonnull
        @CheckReturnValue
        String id();
    
        @Override
        @CheckReturnValue
        default boolean custom() {
            return true;
        }
    }
    
    interface UnicodeEmoji extends Emoji {
        @Override
        default String id() {
            throw new IllegalStateException("Unicode emojis have no IDs!");
        }
        
        @Override
        @Nonnull
        @CheckReturnValue
        default List<String> roles() {
            return Collections.emptyList();
        }
        
        @Override
        @Nullable
        @CheckReturnValue
        default User user() {
            return null;
        }
    
        @Override
        @CheckReturnValue
        default boolean managed() {
            return false;
        }
    
        @Override
        @CheckReturnValue
        default boolean animated() {
            return false;
        }
        
        @Override
        @CheckReturnValue
        default boolean custom() {
            return false;
        }
    }
}
