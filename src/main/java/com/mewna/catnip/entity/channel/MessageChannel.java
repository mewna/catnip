package com.mewna.catnip.entity.channel;

import com.mewna.catnip.entity.message.Embed;
import com.mewna.catnip.entity.message.Message;
import com.mewna.catnip.entity.message.MessageOptions;
import com.mewna.catnip.entity.misc.Emoji;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.concurrent.CompletionStage;

@SuppressWarnings("unused")
public interface MessageChannel extends Channel {
    @Nonnull
    default CompletionStage<Message> sendMessage(@Nonnull final String content) {
        return catnip().rest().channel().sendMessage(id(), content);
    }
    
    @Nonnull
    default CompletionStage<Message> sendMessage(@Nonnull final Embed embed) {
        return catnip().rest().channel().sendMessage(id(), embed);
    }
    
    @Nonnull
    default CompletionStage<Message> sendMessage(@Nonnull final Message message) {
        return catnip().rest().channel().sendMessage(id(), message);
    }
    
    @Nonnull
    default CompletionStage<Message> sendMessage(@Nonnull final MessageOptions options) {
        return catnip().rest().channel().sendMessage(id(), options);
    }
    
    @Nonnull
    default CompletionStage<Message> editMessage(@Nonnull final String messageId, @Nonnull final String content) {
        return catnip().rest().channel().editMessage(id(), messageId, content);
    }
    
    @Nonnull
    default CompletionStage<Message> editMessage(@Nonnull final String messageId, @Nonnull final Embed embed) {
        return catnip().rest().channel().editMessage(id(), messageId, embed);
    }
    
    @Nonnull
    default CompletionStage<Message> editMessage(@Nonnull final String messageId, @Nonnull final Message message) {
        return catnip().rest().channel().editMessage(id(), messageId, message);
    }
    
    @Nonnull
    default CompletionStage<Void> deleteMessage(@Nonnull final String messageId) {
        return catnip().rest().channel().deleteMessage(id(), messageId);
    }
    
    @Nonnull
    default CompletionStage<Void> addReaction(@Nonnull final String messageId, @Nonnull final String emoji) {
        return catnip().rest().channel().addReaction(id(), messageId, emoji);
    }
    
    @Nonnull
    default CompletionStage<Void> addReaction(@Nonnull final String messageId, @Nonnull final Emoji emoji) {
        return catnip().rest().channel().addReaction(id(), messageId, emoji);
    }
    
    @Nonnull
    default CompletionStage<Void> deleteOwnReaction(@Nonnull final String messageId, @Nonnull final String emoji) {
        return catnip().rest().channel().deleteOwnReaction(id(), messageId, emoji);
    }
    
    @Nonnull
    default CompletionStage<Void> deleteOwnReaction(@Nonnull final String messageId, @Nonnull final Emoji emoji) {
        return catnip().rest().channel().deleteOwnReaction(id(), messageId, emoji);
    }
    
    @Nonnull
    default CompletionStage<Void> triggerTypingIndicator() {
        return catnip().rest().channel().triggerTypingIndicator(id());
    }
    
    @Nonnull
    @CheckReturnValue
    default CompletionStage<Message> fetchMessage(@Nonnull final String messageId) {
        return catnip().rest().channel().getMessage(id(), messageId);
    }
}
