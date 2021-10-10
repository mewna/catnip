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

import com.mewna.catnip.entity.channel.Channel;
import com.mewna.catnip.entity.channel.ChannelMention;
import com.mewna.catnip.entity.channel.MessageChannel;
import com.mewna.catnip.entity.guild.Guild;
import com.mewna.catnip.entity.guild.Member;
import com.mewna.catnip.entity.guild.Role;
import com.mewna.catnip.entity.impl.message.MessageReferenceImpl;
import com.mewna.catnip.entity.message.component.ActionRow;
import com.mewna.catnip.entity.message.component.MessageComponent;
import com.mewna.catnip.entity.misc.Emoji;
import com.mewna.catnip.entity.partials.*;
import com.mewna.catnip.entity.sticker.Sticker;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.entity.util.Permission;
import com.mewna.catnip.util.PermissionUtil;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import org.apache.commons.lang3.Validate;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A single message in Discord.
 *
 * @author amy
 * @since 9/4/18.
 */
@SuppressWarnings({"unused", "RedundantSuppression"})
public interface Message extends Snowflake, HasChannel {
    /**
     * The type of message. Use this to tell normal messages from system messages.
     *
     * @return enum representing the message type. Never null.
     */
    @Nonnull
    @CheckReturnValue
    MessageType type();
    
    /**
     * Whether or not the message was sent using the /TTS command, making clients read
     * <br>the message aloud using their text-to-speech engine.
     *
     * @return True if the message is to be spoken, false otherwise.
     */
    @CheckReturnValue
    boolean tts();
    
    /**
     * When the message was sent.
     *
     * @return {@link OffsetDateTime Date and time} the message was sent.
     */
    @Nonnull
    @CheckReturnValue
    OffsetDateTime timestamp();
    
    /**
     * Whether the message is pinned.
     * <br>This will always be false on new messages.
     *
     * @return True if the message is pinned, false otherwise.
     */
    @CheckReturnValue
    boolean pinned();
    
    /**
     * Whether the message mentions everyone.
     *
     * @return True if the message mentions everyone, false otherwise.
     */
    @CheckReturnValue
    boolean mentionsEveryone();
    
    /**
     * The message's nonce snowflake.
     * <br>Nonces are used to validating messages have been sent, as two identical messages
     * <br>with the same nonce are considered to be re-sent due to network errors.
     *
     * @return unique String nonce for the message. Can be null.
     */
    @Nullable
    @CheckReturnValue
    String nonce();
    
    /**
     * List of users @mentioned by this message.
     *
     * @return List of Users. Never null.
     */
    @Nonnull
    @CheckReturnValue
    List<User> mentionedUsers();
    
    /**
     * A list of members mentioned by this message. Will contain the same users
     * as {@link #mentionedUsers()}. Will always be empty for DMs.
     *
     * @return List of members. Never null.
     */
    @Nonnull
    @CheckReturnValue
    List<Member> mentionedMembers();
    
    /**
     * The message that was referenced by this message. Used for inline
     * replies. If {@code null}, the message was deleted. If present and not
     * {@code null}, it is the message that this message is replying to. If not
     * present and the type is {@link MessageType#REPLY}, the backend couldn't
     * fetch the relevant message.
     *
     * @return A message reference.
     */
    @Nullable
    @CheckReturnValue
    Message referencedMessage();
    
    /**
     * List of roles @mentioned by this message.
     * <br>All users with these roles will also be mentioned
     *
     * @return List of roles. Never null.
     */
    @Nonnull
    @CheckReturnValue
    default Observable<Role> mentionedRoles() {
        if(guildId() == null) {
            return Observable.empty();
        }
        //noinspection ConstantConditions
        return Observable.fromIterable(
                mentionedRoleIds().stream()
                        .map(e -> catnip().cache().role(guildId(), e))
                        .collect(Collectors.toList()))
                .flatMapMaybe(m -> m);
    }
    
    /**
     * List of the ids of all roles @mentioned by this message.<br />
     * All users with at least one of these roles will also be mentioned.
     *
     * @return List of role ids. Never null.
     */
    @Nonnull
    @CheckReturnValue
    List<String> mentionedRoleIds();
    
    /**
     * The author of the message, as a member of the guild.
     * <br>Can be null if the author is no longer in the guild, or if the message was sent
     * <br>by a fake user.
     *
     * @return Member representation of the {@link Message#author() author}. Can be null.
     */
    @Nullable
    @CheckReturnValue
    Member member();
    
    /**
     * List of embeds in the message.
     *
     * @return List of embeds. Never null.
     */
    @Nonnull
    @CheckReturnValue
    List<Embed> embeds();
    
    /**
     * When the message was last edited, if ever.
     * <br>Previous edits are not exposed, only the most recent.
     *
     * @return The {@link OffsetDateTime date and time} the message was last edited. Null if the message was never edited.
     */
    @Nullable
    @CheckReturnValue
    OffsetDateTime editedTimestamp();
    
    /**
     * The message's content. Is just an empty string for embed-only messages.
     *
     * @return String containing the message body. Never null.
     */
    @Nonnull
    @CheckReturnValue
    String content();
    
    /**
     * The channel this message was sent in.
     *
     * @return The {@link MessageChannel} object representing the channel this
     * message was sent in. Should not be null.
     */
    @Nonnull
    @CheckReturnValue
    default Maybe<MessageChannel> channel() {
        final long guild = guildIdAsLong();
        if(guild != 0) {
            return catnip().cache().channel(guild, channelIdAsLong()).map(Channel::asMessageChannel);
        } else {
            return catnip().rest().channel().getChannelById(channelId()).map(Channel::asMessageChannel).toMaybe();
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
    @CheckReturnValue
    User author();
    
    /**
     * List of files sent with the message.
     *
     * @return List of files sent with the message. Never null.
     */
    @Nonnull
    @CheckReturnValue
    List<Attachment> attachments();
    
    /**
     * List of reactions added to the message.
     * <br>This list will <b>not</b> be updated.
     *
     * @return List of reactions added to the message. Never null.
     */
    @Nonnull
    @CheckReturnValue
    List<Reaction> reactions();
    
    /**
     * @return The message's activity. Sent in Rich Presence-related embeds.
     * May be null.
     */
    @Nullable
    @CheckReturnValue
    MessageActivity activity();
    
    /**
     * @return The message's application. Sent in Rich Presence-related embeds.
     * May be null.
     */
    @Nullable
    @CheckReturnValue
    MessageApplication application();
    
    /**
     * @return Reference data sent with crossposted messages.
     */
    @Nullable
    @CheckReturnValue
    MessageReference messageReference();
    
    /**
     * @return Raw bits of flags set on this message.
     */
    @CheckReturnValue
    int flagsRaw();
    
    /**
     * @return The stickers sent with this message.
     */
    @Nonnull
    @CheckReturnValue
    List<Sticker> stickers();
    
    /**
     * @return The set of flags set on this message.
     */
    @Nonnull
    @CheckReturnValue
    default Set<MessageFlag> flags() {
        return MessageFlag.toSet(flagsRaw());
    }
    
    /**
     * @return All channels mentioned in this message. Not all messages will
     * have this, nor will all channels mentioned in a message have a
     * corresponding mention object.
     */
    @Nonnull
    @CheckReturnValue
    List<ChannelMention> mentionedChannels();
    
    /**
     * @return All components attached to this message. Top-level components
     * must be {@link ActionRow}s.
     */
    @Nonnull
    @CheckReturnValue
    List<MessageComponent> components();
    
    /**
     * The snowflake ID of the guild this message was sent in.
     *
     * @return String representing the guild ID. Null if sent in DMs, or if
     * fetched/created via REST API.
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
     * The snowflake ID of the guild this message was sent in.
     *
     * @return Long representing the guild ID. Null if sent in DMs.
     */
    @CheckReturnValue
    long guildIdAsLong();
    
    /**
     * The guild this message was sent in. May be null.
     *
     * @return The {@link Guild} object for the guild this message was sent in.
     * Will be null if the message is a DM.
     */
    @Nonnull
    @CheckReturnValue
    default Maybe<Guild> guild() {
        final long id = guildIdAsLong();
        if(id == 0) {
            return Maybe.empty();
        } else {
            return catnip().cache().guild(id);
        }
    }
    
    /**
     * The snowflake ID of the webhook this message was sent by.
     *
     * @return String representing the webhook ID. Null if not sent by a webhook.
     */
    @Nullable
    @CheckReturnValue
    default String webhookId() {
        final long id = webhookIdAsLong();
        if(id == 0) {
            return null;
        }
        return Long.toUnsignedString(id);
    }
    
    /**
     * The snowflake ID of the webhook this message was sent by.
     *
     * @return Long representing the webhook ID. {@code 0} if not sent by a webhook.
     */
    @CheckReturnValue
    long webhookIdAsLong();
    
    /**
     * @return A reference to this message, usable for ex. quoting.
     */
    default MessageReference asReference() {
        return MessageReferenceImpl.builder()
                .catnip(catnip())
                .channelId(channelId())
                .guildId(guildId())
                .messageId(id())
                .build();
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
    default Completable react(@Nonnull final Emoji emoji) {
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
    default Completable react(@Nonnull final String emoji) {
        PermissionUtil.checkPermissions(catnip(), guildId(), channelId(),
                Permission.ADD_REACTIONS, Permission.READ_MESSAGE_HISTORY);
        return catnip().rest().channel().addReaction(channelId(), id(), emoji);
    }

//    default Completable removeReaction()
    
    @Nonnull
    default Completable delete(@Nullable final String reason) {
        return Completable.fromMaybe(catnip().selfUser()
                .filter(self -> !author().id().equals(self.id()))
                .flatMap(self -> {
                    PermissionUtil.checkPermissions(catnip(), guildId(), channelId(),
                            Permission.MANAGE_MESSAGES);
                    return catnip().rest().channel().deleteMessage(channelId(), id(), reason).toMaybe();
                }));
    }
    
    @Nonnull
    @CheckReturnValue
    default Completable delete() {
        return delete(null);
    }
    
    @Nonnull
    @CheckReturnValue
    default Single<Message> edit(@Nonnull final String content) {
        return catnip().rest().channel().editMessage(channelId(), id(), content);
    }
    
    @Nonnull
    @CheckReturnValue
    default Single<Message> edit(@Nonnull final Embed embed) {
        return catnip().rest().channel().editMessage(channelId(), id(), embed);
    }
    
    @Nonnull
    @CheckReturnValue
    default Single<Message> edit(@Nonnull final Message message) {
        Validate.isTrue(message.attachments().isEmpty(), "attachments cannot be edited into messages");
        return catnip().rest().channel().editMessage(channelId(), id(), message);
    }
    
    @Nonnull
    @CheckReturnValue
    default Single<Message> edit(@Nonnull final MessageOptions options) {
        Validate.isTrue(options.files().isEmpty(), "attachments cannot be edited into messages");
        return catnip().rest().channel().editMessage(channelId(), id(), options);
    }
    
    /**
     * Send a message in the same channel as this message. This does <strong>not</strong>
     * send a reply; see {@link #reply(String, boolean)} for that functionality.
     *
     * @param content The message data to respond with.
     *
     * @return A {@code Single} that completes with the newly-created message.
     */
    @Nonnull
    @CheckReturnValue
    default Single<Message> respond(@Nonnull final String content) {
        return catnip().rest().channel().createMessage(channelId(), content);
    }
    
    /**
     * Send a message in the same channel as this message. This does <strong>not</strong>
     * send a reply; see {@link #reply(Embed, boolean)} for that functionality.
     *
     * @param embed The message data to respond with.
     *
     * @return A {@code Single} that completes with the newly-created message.
     */
    @Nonnull
    @CheckReturnValue
    default Single<Message> respond(@Nonnull final Embed embed) {
        return catnip().rest().channel().createMessage(channelId(), embed);
    }
    
    /**
     * Send a message in the same channel as this message. This does <strong>not</strong>
     * send a reply; see {@link #reply(Message, boolean)} for that functionality.
     *
     * @param message The message data to respond with.
     *
     * @return A {@code Single} that completes with the newly-created message.
     */
    @Nonnull
    @CheckReturnValue
    default Single<Message> respond(@Nonnull final Message message) {
        return catnip().rest().channel().createMessage(channelId(), message);
    }
    
    /**
     * Send a message in the same channel as this message. This does <strong>not</strong>
     * send a reply; see {@link #reply(MessageOptions, boolean)} for that functionality.
     *
     * @param options The message data to respond with.
     *
     * @return A {@code Single} that completes with the newly-created message.
     */
    @Nonnull
    @CheckReturnValue
    default Single<Message> respond(@Nonnull final MessageOptions options) {
        return catnip().rest().channel().createMessage(channelId(), options);
    }
    
    /**
     * Reply to this message. This uses Discord's inline replies feature.
     *
     * @param content The message data to reply with.
     * @param ping    Whether or not the reply should ping the user.
     *
     * @return A {@code Single} that completes with the newly-created message.
     */
    @Nonnull
    @CheckReturnValue
    default Single<Message> reply(@Nonnull final String content, final boolean ping) {
        return reply(new MessageOptions().content(content), ping);
    }
    
    /**
     * Reply to this message. This uses Discord's inline replies feature.
     *
     * @param embed The message data to reply with.
     * @param ping  Whether or not the reply should ping the user.
     *
     * @return A {@code Single} that completes with the newly-created message.
     */
    @Nonnull
    @CheckReturnValue
    default Single<Message> reply(@Nonnull final Embed embed, final boolean ping) {
        return reply(new MessageOptions().addEmbed(embed), ping);
    }
    
    /**
     * Reply to this message. This uses Discord's inline replies feature.
     *
     * @param message The message data to reply with.
     * @param ping    Whether or not the reply should ping the user.
     *
     * @return A {@code Single} that completes with the newly-created message.
     */
    @Nonnull
    @CheckReturnValue
    default Single<Message> reply(@Nonnull final Message message, final boolean ping) {
        return reply(new MessageOptions(message), ping);
    }
    
    /**
     * Reply to this message. This uses Discord's inline replies feature.
     *
     * @param options The message data to reply with.
     *
     * @return A {@code Single} that completes with the newly-created message.
     */
    @Nonnull
    @CheckReturnValue
    default Single<Message> reply(@Nonnull final MessageOptions options, final boolean ping) {
        return catnip().rest().channel().createMessage(channelId(), options.pingReply(ping).referenceMessage(asReference()));
    }
    
    default boolean isRickRoll() {
        return content().contains("https://www.youtube.com/watch?v=dQw4w9WgXcQ") || content().contains("https://youtu.be/dQw4w9WgXcQ");
    }
    
    default boolean isGnome() {
        return content().contains("https://youtube.com/watch?v=6n3pFFPSlW4") || content().contains("https://youtu.be/6n3pFFPSlW4");
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
    
        /**
         * Indicates whether or not this attachment is ephemeral. Ephemeral
         * attachments are automatically removed after a set period of time,
         * the duration of which is unfortunately not documented. Ephemeral
         * attachments are guaranteed to exist for at least as long as their
         * containing message exists.
         *
         * @return Whether this attachment is ephemeral.
         */
        boolean ephemeral();
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
         * The emojis representing this reaction.
         *
         * @return Emoji object of this reaction.
         */
        @Nonnull
        @CheckReturnValue
        Emoji emoji();
    }
    
    interface MessageActivity {
        /**
         * @return The type of the message activity.
         */
        @Nonnull
        @CheckReturnValue
        MessageActivityType type();
        
        /**
         * @return The Rich Presence party id. May be null.
         */
        @Nullable
        @CheckReturnValue
        String partyId();
    }
    
    interface MessageApplication extends HasName, HasIcon, HasDescription {
        /**
         * @return The application's id.
         */
        @Nonnull
        @CheckReturnValue
        String id();
        
        /**
         * @return The application's cover image. Shown in embeds. May be null.
         */
        @Nullable
        @CheckReturnValue
        String coverImage();
    }
}
