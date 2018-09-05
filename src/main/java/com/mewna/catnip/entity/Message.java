package com.mewna.catnip.entity;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.List;

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
    
    interface Attachment {
        @Nonnull
        @CheckReturnValue
        String id();
    
        @Nonnull
        @CheckReturnValue
        String fileName();
        
        @CheckReturnValue
        int size();
    
        @Nonnull
        @CheckReturnValue
        String url();
    
        @Nonnull
        @CheckReturnValue
        String proxyUrl();
        
        @CheckReturnValue
        int height();
        
        @CheckReturnValue
        int width();
        
        @CheckReturnValue
        default boolean image() {
            return height() > 0 && width() > 0;
        }
    }
    
    interface Reaction {
        @Nonnegative
        @CheckReturnValue
        int count();
        
        @CheckReturnValue
        boolean self();
        
        @Nonnull
        @CheckReturnValue
        Emoji emoji();
    }
}
