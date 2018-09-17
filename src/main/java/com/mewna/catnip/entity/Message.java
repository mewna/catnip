package com.mewna.catnip.entity;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author amy
 * @since 9/4/18.
 */
public interface Message extends Snowflake {
    /**
     * The type of message. Use this to tell normal messages from system messages.
     *
     * @return enum representing the message type. Never null.
     */
    @Nonnull
    MessageType type();
    
    /**
     * Whether or not the message was sent using the /TTS command, making clients read
     * <br>the message aloud using their text-to-speech engine.
     *
     * @return True if the message is to be spoken, false otherwise.
     */
    boolean tts();
    
    /**
     * When the message was sent.
     *
     * @return {@link OffsetDateTime Date and time} the message was sent.
     */
    @Nonnull
    OffsetDateTime timestamp();
    
    /**
     * Whether the message is pinned.
     * <br>This will always be false on new messages.
     *
     * @return True if the message is pinned, false otherwise.
     */
    boolean pinned();
    
    /**
     * Whether the message mentions everyone.
     *
     * @return True if the message mentions everyone, false otherwise.
     */
    boolean mentionsEveryone();
    
    /**
     * The message's nonce snowflake.
     * <br>Nonces are used to validating messages have been sent, as two identical messages
     * <br>with the same nonce are considered to be re-sent due to network errors.
     *
     * @return unique String nonce for the message. Can be null.
     */
    @Nullable
    String nonce();
    
    /**
     * List of users @mentioned by this message.
     *
     * @return List of Users. Never null.
     */
    @Nonnull
    List<User> mentionedUsers();
    
    /**
     * List of roles @mentioned by this message.
     * <br>All users with these roles will also be mentioned
     * //TODO: Check if users are included in mentionedUsers() and include that.
     *
     * @return List of Roles. Never null.
     */
    @Nonnull
    List<String> mentionedRoles();
    
    /**
     * The author of the message, as a member of the guild.
     * <br>Can be null if the author is no longer in the guild, or if the message was sent
     * <br>by a fake user.
     *
     * @return Member representation of the {@link Message#author() author}. Can be null.
     */
    @Nullable
    Member member();
    
    /**
     * The unique snowflake ID of the message.
     *
     * @return String containing the message ID. Never null.
     */
    @Nonnull
    String id();
    
    /**
     * List of embeds in the message.
     *
     * @return List of embeds. Never null.
     */
    @Nonnull
    List<Embed> embeds();
    
    /**
     * When the message was last edited, if ever.
     * <br>Previous edits are not exposed, only the most recent.
     *
     * @return The {@link OffsetDateTime date and time} the message was last edited. Null if the message was never edited.
     */
    @Nullable
    OffsetDateTime editedTimestamp();
    
    /**
     * The message's content.
     * //TODO: Check if embed-only messages return null.
     *
     * @return String containing the message body. Never null.
     */
    @Nonnull
    String content();
    
    /**
     * The snowflake ID of the channel this message was sent in.
     *
     * @return String representing the channel ID. Never null.
     */
    @Nonnull
    String channelId();
    
    /**
     * Guild-agnostic representation of the author of the message.
     *
     * @see Message#member() Guild-specific representation of the author.
     *
     * @return The author of the message. Never null.
     */
    @Nonnull
    User author();
    
    /**
     * List of files sent with the message.
     *
     * @return List of files sent with the message. Never null.
     */
    @Nonnull
    List<Attachment> attachments();
    
    /**
     * List of reactions added to the message.
     * <br>This list will <b>not</b> be updated.
     *
     * @return List of reactions added to the message. Never null.
     */
    @Nonnull
    List<Reaction> reactions();
    
    /**
     * The snowflake ID of the guild this message was sent in.
     *
     * @return String representing the guild ID. Null if sent in DMs.
     */
    @Nullable
    String guildId();
    
    /**
     * The snowflake ID of the webhook this message was sent by.
     *
     * @return String representing the webhook ID. Null if not sent by a webhook.
     */
    @Nullable
    String webhookId();
    
    /**
     * Adds a reaction to this message.
     * <br>Note: this object will <b>not</b> be updated.
     *
     * @param emoji Emoji to react with.
     *
     * @return Future for the reaction.
     */
    @Nonnull
    default CompletableFuture<Void> react(@Nonnull final Emoji emoji) {
        return catnip().rest().channel().addReaction(channelId(), id(), emoji);
    }
    
    /**
     * Adds a reaction to this message.
     * <br>Note: this object will <b>not</b> be updated.
     *
     * @param emoji Emoji to react with.
     *
     * @return Future for the reaction.
     */
    @Nonnull
    default CompletableFuture<Void> react(@Nonnull final String emoji) {
        return catnip().rest().channel().addReaction(channelId(), id(), emoji);
    }
    
    @Nonnull
    default CompletableFuture<Void> delete() {
        return catnip().rest().channel().deleteMessage(channelId(), id());
    }
    
    @Nonnull
    default CompletableFuture<Message> edit(@Nonnull final String content) {
        return catnip().rest().channel().editMessage(channelId(), id(), content);
    }
    
    @Nonnull
    default CompletableFuture<Message> edit(@Nonnull final Embed embed) {
        return catnip().rest().channel().editMessage(channelId(), id(), embed);
    }
    
    @Nonnull
    default CompletableFuture<Message> edit(@Nonnull final Message message) {
        return catnip().rest().channel().editMessage(channelId(), id(), message);
    }
    
    interface Attachment extends Snowflake {
        /**
         * The name of the file represented by this attachment.
         *
         * @return String representing the file name. Never null.
         */
        @Nonnull
        @CheckReturnValue
        String fileName();
    
        /**
         * The size of the file represented by this attachment, in bytes.
         *
         * @return Integer representing the file size. Never negative.
         */
        @Nonnegative
        @CheckReturnValue
        int size();
    
        /**
         *The source URL for the file.
         *
         * @return String representing the source URL. Never null.
         */
        @Nonnull
        @CheckReturnValue
        String url();
    
        /**
         *The proxied URL for the file.
         *
         * @return String representing the proxied URL. Never null.
         */
        @Nonnull
        @CheckReturnValue
        String proxyUrl();
    
        /**
         * The height of this attachment, if it's an image.
         *
         * @return Integer representing the height, or -1 if this attachment is not an image.
         */
        @CheckReturnValue
        int height();
    
        /**
         * The width of this attachment, if it's an image.
         *
         * @return Integer representing the width, or -1 if this attachment is not an image.
         */
        @CheckReturnValue
        int width();
    
        /**
         * Whether this attachment is an image.
         *
         * @return True if this attachment is an image, false otherwise.
         */
        @CheckReturnValue
        default boolean image() {
            return height() > 0 && width() > 0;
        }
    }
    
    interface Reaction {
        /**
         * The count of reactions.
         *
         * @return Integer representing how many reactions were added.
         */
        @Nonnegative
        @CheckReturnValue
        int count();
    
        /**
         * Whether the current logged in account added this reaction.
         *
         * @return True if the current account added this reaction, false otherwise.
         */
        @CheckReturnValue
        boolean self();
    
        /**
         * The emoji representing this reaction.
         *
         * @return Emoji object of this reaction.
         */
        @Nonnull
        @CheckReturnValue
        Emoji emoji();
    }
}
