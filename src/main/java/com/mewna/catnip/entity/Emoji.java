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
    /**
     * ID of this emoji, or null if it has no ID.
     * <br>Always null for {@link #unicode() unicode} emojis.
     *
     * @return String representing the ID.
     */
    @Nullable
    @CheckReturnValue
    String id();
    
    /**
     * Name of this emoji, if it's {@link #custom() custom}, or it's {@link #unicode() unicode} value.
     *
     * @return String representing the name or unicode value.
     */
    @Nonnull
    @CheckReturnValue
    String name();
    
    /**
     * Roles that are allowed to use this emoji. If empty, all users can use it.
     * <br>Always empty for {@link #unicode() unicode} emojis.
     *
     * @return List of role IDs allowed.
     */
    @Nonnull
    @CheckReturnValue
    List<String> roles();
    
    /**
     * User who uploaded this emoji.
     * <br>Always null for {@link #unicode() unicode} emojis.
     *
     * @return User who uploaded the emoji.
     */
    @Nullable
    @CheckReturnValue
    User user();
    
    /**
     * Whether this emoji must be wrapped in colons.
     *
     * @return True if it should be wrapped in colons, false otherwise.
     */
    @CheckReturnValue
    boolean requiresColons();
    
    /**
     * Whether this emoji is managed.
     * <br>Always false for {@link #unicode() unicode} emojis.
     *
     * @return True if it's managed, false otherwise.
     */
    @CheckReturnValue
    boolean managed();
    
    /**
     * Whether this emoji is animated.
     * <br>Always false for {@link #unicode() unicode} emojis.
     *
     * @return True if it's animated, false otherwise.
     */
    @CheckReturnValue
    boolean animated();
    
    /**
     * Whether this emoji is {@link CustomEmoji custom}.
     *
     * @return True if this emoji is custom, false otherwise.
     */
    @CheckReturnValue
    boolean custom();
    
    /**
     * Whether this emoji is {@link UnicodeEmoji unicode}.
     * <br>This method is equivalent to {@link #custom() {@code !custom()}}.
     *
     * @return True if this emoji is custom, false otherwise.
     */
    @CheckReturnValue
    default boolean unicode() {
        return !custom();
    }
    
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
