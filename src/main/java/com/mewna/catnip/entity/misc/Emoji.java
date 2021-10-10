/*
 * Copyright (c) 2018 amy, All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.mewna.catnip.entity.misc;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.guild.Guild;
import com.mewna.catnip.entity.impl.misc.CustomEmojiImpl;
import com.mewna.catnip.entity.impl.misc.UnicodeEmojiImpl;
import com.mewna.catnip.entity.partials.GuildEntity;
import com.mewna.catnip.entity.partials.HasNullableName;
import com.mewna.catnip.entity.partials.Snowflake;
import com.mewna.catnip.entity.user.User;
import com.vdurmont.emoji.EmojiManager;
import io.reactivex.rxjava3.core.Maybe;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author natanbc
 * @since 9/5/18.
 */
@SuppressWarnings({"unused", "RedundantSuppression", "SameReturnValue"})
public interface Emoji extends Snowflake, HasNullableName {
    @Nonnull
    @SuppressWarnings("ClassReferencesSubclass")
    static UnicodeEmoji fromUnicode(@Nonnull final Catnip catnip, @Nonnull final String unicode) {
        if(!EmojiManager.isEmoji(unicode)) {
            throw new IllegalArgumentException("String '" + unicode + "' is not an emoji!");
        }
        return new UnicodeEmojiImpl(catnip, unicode, true);
    }
    
    @Nonnull
    @SuppressWarnings("ClassReferencesSubclass")
    static CustomEmoji fromString(@Nonnull final Catnip catnip, @Nonnull final String name, @Nonnull final String id,
                                  final boolean animated) {
        if(name.length() < 2 || name.length() > 32) {
            throw new IllegalArgumentException("Name must be between 2 and 32 characters (got '" + name + "')");
        }
        if(!id.matches("\\d+")) {
            throw new IllegalArgumentException("String '" + id + "' is not a valid snowflake");
        }
        return new CustomEmojiImpl(catnip, Long.parseUnsignedLong(id), 0L, name, List.of(), null,
                true, false, animated);
    }
    
    /**
     * ID of this emoji, or null if it has no ID.
     * <br>Always null for {@link #unicode() unicode} emoji.
     *
     * @return String representing the ID.
     */
    @Nonnull
    @SuppressWarnings("NullableProblems")
    @CheckReturnValue
    String id();
    
    /**
     * Name of this emoji, if it's {@link #custom() custom}, or it's {@link #unicode() unicode} value.<br/>
     * This may be null in the case of reactions.
     *
     * @return String representing the name or unicode value.
     */
    @Nullable
    @CheckReturnValue
    String name();
    
    /**
     * Roles that are allowed to use this emoji. If empty, all users can use it.
     * <br>Always empty for {@link #unicode() unicode} emoji.
     *
     * @return List of role IDs allowed.
     */
    @Nonnull
    @CheckReturnValue
    List<String> roles();
    
    /**
     * User who uploaded this emoji.
     * <br>Always null for {@link #unicode() unicode} emoji.
     *
     * @return User who uploaded the emoji.
     */
    @SuppressWarnings("SameReturnValue")
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
     * <br>Always false for {@link #unicode() unicode} emoji.
     *
     * @return True if it's managed, false otherwise.
     */
    @SuppressWarnings("SameReturnValue")
    @CheckReturnValue
    boolean managed();
    
    /**
     * Whether this emoji is animated.
     * <br>Always false for {@link #unicode() unicode} emoji.
     *
     * @return True if it's animated, false otherwise.
     */
    @SuppressWarnings("SameReturnValue")
    @CheckReturnValue
    boolean animated();
    
    /**
     * Whether this emoji is {@link CustomEmoji custom}.
     *
     * @return True if this emoji is custom, false otherwise.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
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
    
    /**
     * A string that may be sent in a message and will render this emoji, if the user has permission to.
     *
     * @return A string that yields this emoji when inside a message.
     */
    @Nonnull
    @CheckReturnValue
    String forMessage();
    
    /**
     * A string that may be added as a reaction to a message, if the user has permission to.
     *
     * @return A string that yields this emoji when added as a reaction.
     */
    @Nonnull
    @CheckReturnValue
    String forReaction();
    
    /**
     * Checks whether or not this emoji is the provided emoji string.
     * <br>If this emoji is {@link UnicodeEmoji unicode}, it's
     * {@link #name() name} is compared for equality with the provided string.
     * <br>If this emoji is {@link CustomEmoji custom}, the following checks,
     * in order, are applied:
     * <ul>
     * <li>{@link Emoji#id() id} equality</li>
     * <li>{@link Emoji#forMessage() forMessage()} equality</li>
     * <li>{@link Emoji#forReaction()} forReaction()} equality</li>
     * </ul>
     *
     * @param emoji Emoji string to compare against.
     *
     * @return True, if this emoji is equal to the provided string.
     */
    @CheckReturnValue
    boolean is(@Nonnull String emoji);
    
    interface CustomEmoji extends Emoji, GuildEntity {
        @Override
        @Nonnull
        @CheckReturnValue
        default String id() {
            return Long.toUnsignedString(idAsLong());
        }
        
        /**
         * Guild that owns this emoji, or {@code null} if it has no guild.
         * <p>
         * NOTE: This may be null in the case of a reaction, because the data
         * may not be available to get the id for the emoji!
         *
         * @return String representing the ID.
         */
        @Nonnull
        @CheckReturnValue
        default Maybe<Guild> guild() {
            final long id = guildIdAsLong();
            if(id == 0) {
                return Maybe.empty();
            }
            return catnip().cache().guild(guildIdAsLong());
        }
        
        /**
         * ID of guild that owns this emoji, or {@code null} if it has no guild.
         * <p>
         * NOTE: This may be null in the case of a reaction, because the data
         * may not be available to get the id for the emoji!
         *
         * @return String representing the ID.
         */
        @Nullable
        @CheckReturnValue
        default String guildId() {
            final long id = guildIdAsLong();
            if(id == 0) {
                return null;
            }
            return Long.toUnsignedString(id);
        }
        
        /**
         * ID of guild that owns this emoji, or {@code 0} if it has no guild.
         * <p>
         * NOTE: This may be null in the case of a reaction, because the data
         * may not be available to get the id for the emoji!
         *
         * @return Long representing the ID.
         */
        @CheckReturnValue
        long guildIdAsLong();
        
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
        
        @Override
        @CheckReturnValue
        default boolean is(@Nonnull final String emoji) {
            return id().equals(emoji) || forMessage().equals(emoji) || forReaction().equals(emoji);
        }
    }
    
    interface UnicodeEmoji extends Emoji {
        @Nonnull
        @Override
        default String id() {
            throw new IllegalStateException("Unicode emojis have no IDs!");
        }
        
        @Override
        default long idAsLong() {
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
            return Objects.requireNonNull(name());
        }
        
        @Override
        @Nonnull
        @CheckReturnValue
        default String forReaction() {
            return Objects.requireNonNull(name());
        }
        
        @Override
        @CheckReturnValue
        default boolean is(@Nonnull final String emoji) {
            return Objects.requireNonNull(name()).equals(emoji);
        }
    }
    
    /**
     * The emoji in a user's activity, ex. for custom status messages. Note
     * that the majority of getters on this class return default falsey values.
     * The only fields currently set with non-default-falsey values are
     * {@code id}, {@code animated}, and {@code name}.
     */
    interface ActivityEmoji extends Emoji {
        @Nonnull
        @Override
        @CheckReturnValue
        default String id() {
            return idAsLong() != 0 ? Long.toUnsignedString(idAsLong()) : null;
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
        default boolean custom() {
            return false;
        }
        
        @Override
        @CheckReturnValue
        default boolean requiresColons() {
            return false;
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
        
        @Override
        @CheckReturnValue
        default boolean is(@Nonnull final String emoji) {
            return Objects.requireNonNull(name()).equals(emoji);
        }
    }
}
