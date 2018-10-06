package com.mewna.catnip.entity.misc;

import com.mewna.catnip.entity.Snowflake;
import com.mewna.catnip.entity.user.User;

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
     * ID of this emojis, or null if it has no ID.
     * <br>Always null for {@link #unicode() unicode} emojis.
     *
     * @return String representing the ID.
     */
    @Nullable
    @CheckReturnValue
    String id();
    
    /**
     * Name of this emojis, if it's {@link #custom() custom}, or it's {@link #unicode() unicode} value.
     *
     * @return String representing the name or unicode value.
     */
    @Nonnull
    @CheckReturnValue
    String name();
    
    /**
     * Roles that are allowed to use this emojis. If empty, all users can use it.
     * <br>Always empty for {@link #unicode() unicode} emojis.
     *
     * @return List of role IDs allowed.
     */
    @Nonnull
    @CheckReturnValue
    List<String> roles();
    
    /**
     * User who uploaded this emojis.
     * <br>Always null for {@link #unicode() unicode} emojis.
     *
     * @return User who uploaded the emojis.
     */
    @Nullable
    @CheckReturnValue
    User user();
    
    /**
     * Whether this emojis must be wrapped in colons.
     *
     * @return True if it should be wrapped in colons, false otherwise.
     */
    @CheckReturnValue
    boolean requiresColons();
    
    /**
     * Whether this emojis is managed.
     * <br>Always false for {@link #unicode() unicode} emojis.
     *
     * @return True if it's managed, false otherwise.
     */
    @CheckReturnValue
    boolean managed();
    
    /**
     * Whether this emojis is animated.
     * <br>Always false for {@link #unicode() unicode} emojis.
     *
     * @return True if it's animated, false otherwise.
     */
    @CheckReturnValue
    boolean animated();
    
    /**
     * Whether this emojis is {@link CustomEmoji custom}.
     *
     * @return True if this emojis is custom, false otherwise.
     */
    @CheckReturnValue
    boolean custom();
    
    /**
     * Whether this emojis is {@link UnicodeEmoji unicode}.
     * <br>This method is equivalent to {@link #custom() {@code !custom()}}.
     *
     * @return True if this emojis is custom, false otherwise.
     */
    @CheckReturnValue
    default boolean unicode() {
        return !custom();
    }
    
    /**
     * A string that may be sent in a message and will render this emojis, if the user has permission to.
     *
     * @return A string that yields this emojis when inside a message.
     */
    @Nonnull
    @CheckReturnValue
    String forMessage();
    
    /**
     * A string that may be added as a reaction to a message, if the user has permission to.
     *
     * @return A string that yields this emojis when added as a reaction.
     */
    @Nonnull
    @CheckReturnValue
    String forReaction();
    
    interface CustomEmoji extends Emoji {
        @Override
        @Nonnull
        @CheckReturnValue
        String id();
        
        /**
         * ID of guild that owns this emojis, or null if it has no guild.
         * <p />
         * NOTE: This may be null in the case of a reaction, because the data
         * may not be available to get the id for the emoji!
         *
         * @return String representing the ID.
         */
        @Nullable
        @CheckReturnValue
        String guildId();
        
        @Override
        @CheckReturnValue
        default boolean custom() {
            return true;
        }
        
        @Override
        @Nonnull
        @CheckReturnValue
        default String forMessage() {
            return String.format("<%s:%s:%s>", animated() ? "a" : "", name(), id());
        }
        
        @Override
        @Nonnull
        @CheckReturnValue
        default String forReaction() {
            return String.format("%s:%s", name(), id());
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
        
        @Override
        @Nonnull
        @CheckReturnValue
        default String forMessage() {
            return name();
        }
        
        @Override
        @Nonnull
        @CheckReturnValue
        default String forReaction() {
            return name();
        }
    }
}
