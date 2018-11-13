package com.mewna.catnip.entity.channel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mewna.catnip.entity.message.Embed;
import com.mewna.catnip.entity.message.Message;
import com.mewna.catnip.entity.message.MessageOptions;
import com.mewna.catnip.entity.misc.Emoji;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.concurrent.CompletionStage;

/**
 * A channel that can have messages sent in it.
 *
 * @author natanbc
 * @since 9/12/18
 */
@SuppressWarnings("unused")
public interface MessageChannel extends Channel {
    /**
     * Send a message to this channel with the specified content.
     *
     * @param content The text content to send.
     *
     * @return A CompletionStage that completes when the message is sent.
     */
    @Nonnull
    @JsonIgnore
    default CompletionStage<Message> sendMessage(@Nonnull final String content) {
        return catnip().rest().channel().sendMessage(id(), content);
    }
    
    /**
     * Send a message to this channel with the specified embed.
     *
     * @param embed The embed to send
     *
     * @return A CompletionStage that completes when the message is sent.
     */
    @Nonnull
    @JsonIgnore
    default CompletionStage<Message> sendMessage(@Nonnull final Embed embed) {
        return catnip().rest().channel().sendMessage(id(), embed);
    }
    
    /**
     * Send a message to this channel.
     *
     * @param message The message to send.
     *
     * @return A CompletionStage that completes when the message is sent.
     */
    @Nonnull
    @JsonIgnore
    default CompletionStage<Message> sendMessage(@Nonnull final Message message) {
        return catnip().rest().channel().sendMessage(id(), message);
    }
    
    /**
     * Send a message to this channel with the specified options.
     *
     * @param options The options for the message being sent.
     *
     * @return A CompletionStage that completes when the message is sent.
     */
    @Nonnull
    @JsonIgnore
    default CompletionStage<Message> sendMessage(@Nonnull final MessageOptions options) {
        return catnip().rest().channel().sendMessage(id(), options);
    }
    
    /**
     * Edit the message with the given id in this channel to contain the
     * specified content.
     *
     * @param messageId The id of the message to edit.
     * @param content   The new content to set on the message.
     *
     * @return A CompletionStage that completes when the message is edited.
     */
    @Nonnull
    @JsonIgnore
    default CompletionStage<Message> editMessage(@Nonnull final String messageId, @Nonnull final String content) {
        return catnip().rest().channel().editMessage(id(), messageId, content);
    }
    
    /**
     * Edit the message with the given id in this channel to contain the
     * specified embed.
     *
     * @param messageId The id of the message to edit.
     * @param embed     The new embed to be set on the message.
     *
     * @return A CompletionStage that completes when the message is edited.
     */
    @Nonnull
    @JsonIgnore
    default CompletionStage<Message> editMessage(@Nonnull final String messageId, @Nonnull final Embed embed) {
        return catnip().rest().channel().editMessage(id(), messageId, embed);
    }
    
    /**
     * Edit the message with the given id in this channel to contain the content
     * and embed of the given message.
     *
     * @param messageId The id of the message to edit.
     * @param message   The message to set as the new message.
     *
     * @return A CompletionStage that completes when the message is edited.
     */
    @Nonnull
    @JsonIgnore
    default CompletionStage<Message> editMessage(@Nonnull final String messageId, @Nonnull final Message message) {
        return catnip().rest().channel().editMessage(id(), messageId, message);
    }
    
    /**
     * Delete the message with the given id in this channel.
     *
     * @param messageId The id of the message to delete.
     *
     * @return A CompletionStage that completes when the message is deleted.
     */
    @Nonnull
    @JsonIgnore
    default CompletionStage<Void> deleteMessage(@Nonnull final String messageId) {
        return catnip().rest().channel().deleteMessage(id(), messageId);
    }
    
    /**
     * Add a reaction to the message with the given id in this channel.
     *
     * @param messageId The id of the message to add a reaction to.
     * @param emoji     The reaction to add.
     *
     * @return A CompletionStage that completes when the reaction is added.
     */
    @Nonnull
    @JsonIgnore
    default CompletionStage<Void> addReaction(@Nonnull final String messageId, @Nonnull final String emoji) {
        return catnip().rest().channel().addReaction(id(), messageId, emoji);
    }
    
    /**
     * Add a reaction to the message with the given id in this channel.
     *
     * @param messageId The id of the message to add a reaction to.
     * @param emoji     The reaction to add.
     *
     * @return A CompletionStage that completes when the reaction is added.
     */
    @Nonnull
    @JsonIgnore
    default CompletionStage<Void> addReaction(@Nonnull final String messageId, @Nonnull final Emoji emoji) {
        return catnip().rest().channel().addReaction(id(), messageId, emoji);
    }
    
    /**
     * Delete your own reaction on the given message.
     *
     * @param messageId The id of the message to remove a reaction from.
     * @param emoji     The reaction to remove.
     *
     * @return A CompletionStage that completes when the reaction is removed.
     */
    @Nonnull
    @JsonIgnore
    default CompletionStage<Void> deleteOwnReaction(@Nonnull final String messageId, @Nonnull final String emoji) {
        return catnip().rest().channel().deleteOwnReaction(id(), messageId, emoji);
    }
    
    /**
     * Delete your own reaction on the given message.
     *
     * @param messageId The id of the message to remove a reaction from.
     * @param emoji     The reaction to remove.
     *
     * @return A CompletionStage that completes when the reaction is removed.
     */
    @Nonnull
    @JsonIgnore
    default CompletionStage<Void> deleteOwnReaction(@Nonnull final String messageId, @Nonnull final Emoji emoji) {
        return catnip().rest().channel().deleteOwnReaction(id(), messageId, emoji);
    }
    
    /**
     * Trigger the "[user] is typing..." indicator for yourself in this
     * channel.
     *
     * @return A CompletionStage that completes when the typing indicator is
     * triggered.
     */
    @Nonnull
    @JsonIgnore
    default CompletionStage<Void> triggerTypingIndicator() {
        return catnip().rest().channel().triggerTypingIndicator(id());
    }
    
    /**
     * Fetch the message with the given id from this channel.
     *
     * @param messageId The id of the message to fetch.
     *
     * @return A CompletionStage that completes when the message is fetched.
     */
    @Nonnull
    @JsonIgnore
    @CheckReturnValue
    default CompletionStage<Message> fetchMessage(@Nonnull final String messageId) {
        return catnip().rest().channel().getMessage(id(), messageId);
    }
}
