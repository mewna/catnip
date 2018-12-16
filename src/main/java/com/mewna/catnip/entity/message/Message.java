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

package com.mewna.catnip.entity.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mewna.catnip.entity.Snowflake;
import com.mewna.catnip.entity.channel.MessageChannel;
import com.mewna.catnip.entity.channel.TextChannel;
import com.mewna.catnip.entity.guild.Guild;
import com.mewna.catnip.entity.guild.Member;
import com.mewna.catnip.entity.impl.MessageImpl;
import com.mewna.catnip.entity.impl.MessageImpl.AttachmentImpl;
import com.mewna.catnip.entity.impl.MessageImpl.ReactionImpl;
import com.mewna.catnip.entity.misc.Emoji;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.entity.util.Permission;
import com.mewna.catnip.util.PermissionUtil;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * A single message in Discord.
 *
 * @author amy
 * @since 9/4/18.
 */
@SuppressWarnings("unused")
@JsonDeserialize(as = MessageImpl.class)
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
     * The channel this message was sent in.
     *
     * @return The {@link MessageChannel} object representing the channel this
     * message was sent in. Should not be null.
     */
    @CheckReturnValue
    default MessageChannel channel() {
        final String guild = guildId();
        if(guild != null) {
            return (TextChannel)catnip().cache().channel(guild, channelId());
        } else {
            return catnip().cache().dmChannel(channelId());
        }
    }
    
    /**
     * Guild-agnostic representation of the author of the message.
     *
     * @return The author of the message. Never null.
     *
     * @see Message#member() Guild-specific representation of the author.
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
     * The guild this message was sent in. May be null.
     *
     * @return The {@link Guild} object for the guild this message was sent in.
     * Will be null if the message is a DM.
     */
    @Nullable
    @CheckReturnValue
    default Guild guild() {
        if(guildId() == null) {
            return null;
        } else {
            //noinspection ConstantConditions
            return catnip().cache().guild(guildId());
        }
    }
    
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
    @JsonIgnore
    default CompletionStage<Void> react(@Nonnull final Emoji emoji) {
        PermissionUtil.checkPermissions(catnip(), guildId(), channelId(),
                Permission.ADD_REACTIONS, Permission.READ_MESSAGE_HISTORY);
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
    @JsonIgnore
    default CompletionStage<Void> react(@Nonnull final String emoji) {
        PermissionUtil.checkPermissions(catnip(), guildId(), channelId(),
                Permission.ADD_REACTIONS, Permission.READ_MESSAGE_HISTORY);
        return catnip().rest().channel().addReaction(channelId(), id(), emoji);
    }
    
    @Nonnull
    @JsonIgnore
    default CompletionStage<Void> delete() {
        final User self = catnip().selfUser();
        if(self != null && !author().id().equals(self.id())) {
            PermissionUtil.checkPermissions(catnip(), guildId(), channelId(),
                    Permission.MANAGE_MESSAGES);
        }
        return catnip().rest().channel().deleteMessage(channelId(), id());
    }
    
    @Nonnull
    @JsonIgnore
    default CompletionStage<Message> edit(@Nonnull final String content) {
        return catnip().rest().channel().editMessage(channelId(), id(), content);
    }
    
    @Nonnull
    @JsonIgnore
    default CompletionStage<Message> edit(@Nonnull final Embed embed) {
        return catnip().rest().channel().editMessage(channelId(), id(), embed);
    }
    
    @Nonnull
    @JsonIgnore
    default CompletionStage<Message> edit(@Nonnull final Message message) {
        return catnip().rest().channel().editMessage(channelId(), id(), message);
    }
    
    @JsonDeserialize(as = AttachmentImpl.class)
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
         * The source URL for the file.
         *
         * @return String representing the source URL. Never null.
         */
        @Nonnull
        @CheckReturnValue
        String url();
        
        /**
         * The proxied URL for the file.
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
    
    @JsonDeserialize(as = ReactionImpl.class)
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
         * The emojis representing this reaction.
         *
         * @return Emoji object of this reaction.
         */
        @Nonnull
        @CheckReturnValue
        Emoji emoji();
    }
}
