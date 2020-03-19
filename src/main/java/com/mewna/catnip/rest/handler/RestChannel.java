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

package com.mewna.catnip.rest.handler;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.mewna.catnip.entity.channel.Channel;
import com.mewna.catnip.entity.channel.GuildChannel;
import com.mewna.catnip.entity.channel.GuildChannel.ChannelEditFields;
import com.mewna.catnip.entity.channel.Webhook;
import com.mewna.catnip.entity.guild.PermissionOverride;
import com.mewna.catnip.entity.guild.PermissionOverride.OverrideType;
import com.mewna.catnip.entity.message.*;
import com.mewna.catnip.entity.misc.CreatedInvite;
import com.mewna.catnip.entity.misc.Emoji;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.entity.util.Permission;
import com.mewna.catnip.internal.CatnipImpl;
import com.mewna.catnip.rest.ResponsePayload;
import com.mewna.catnip.rest.Routes;
import com.mewna.catnip.rest.invite.InviteCreateOptions;
import com.mewna.catnip.rest.requester.Requester.OutboundRequest;
import com.mewna.catnip.util.QueryStringBuilder;
import com.mewna.catnip.util.pagination.MessagePaginator;
import com.mewna.catnip.util.pagination.ReactionPaginator;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static com.mewna.catnip.util.JsonUtil.mapObjectContents;
import static com.mewna.catnip.util.Utils.encodeUTF8;

/**
 * @author amy
 * @since 9/3/18.
 */
@SuppressWarnings({"unused", "WeakerAccess", "ConstantConditions"})
public class RestChannel extends RestHandler {
    public RestChannel(final CatnipImpl catnip) {
        super(catnip);
    }
    
    @Nonnull
    public Single<Message> sendMessage(@Nonnull final String channelId, @Nonnull final String content) {
        return sendMessage(channelId, new MessageOptions().content(content));
    }
    
    @Nonnull
    public Single<Message> sendMessage(@Nonnull final String channelId, @Nonnull final Embed embed) {
        return sendMessage(channelId, new MessageOptions().embed(embed));
    }
    
    @Nonnull
    public Single<Message> sendMessage(@Nonnull final String channelId, @Nonnull final Message message) {
        return Single.fromObservable(sendMessageRaw(channelId, message).map(entityBuilder()::createMessage));
    }
    
    @Nonnull
    public Single<Message> sendMessage(@Nonnull final String channelId, @Nonnull final MessageOptions options) {
        return Single.fromObservable(sendMessageRaw(channelId, options).map(entityBuilder()::createMessage));
    }
    
    @Nonnull
    public Observable<JsonObject> sendMessageRaw(@Nonnull final String channelId, @Nonnull final Message message) {
        final JsonObject json = new JsonObject();
        if(message.content() != null && !message.content().isEmpty()) {
            json.put("content", message.content());
        }
        final List<Embed> embeds = message.embeds();
        if(embeds != null && !embeds.isEmpty()) {
            json.put("embed", entityBuilder().embedToJson(embeds.get(0)));
        }
        if(json.get("embed") == null && json.get("content") == null && message.attachments().isEmpty()) {
            throw new IllegalArgumentException("Can't build a message with no content, no embeds and no attachments!");
        }
        
        final OutboundRequest request = new OutboundRequest(Routes.CREATE_MESSAGE.withMajorParam(channelId), Map.of(), json);
        return catnip().requester()
                .queue(request)
                .map(ResponsePayload::object);
    }
    
    @Nonnull
    public Observable<JsonObject> sendMessageRaw(@Nonnull final String channelId, @Nonnull final MessageOptions options) {
        final JsonObject json = new JsonObject();
        if(options.content() != null && !options.content().isEmpty()) {
            json.put("content", options.content());
        }
        if(options.embed() != null) {
            json.put("embed", entityBuilder().embedToJson(options.embed()));
        }
        if(json.get("embed") == null && json.get("content") == null && !options.hasFiles()) {
            throw new IllegalArgumentException("Can't build a message with no content, no embeds and no attachments!");
        }
        
        if(options.parseFlags() != null || options.mentionedUsers() != null || options.mentionedRoles() != null) {
            final JsonObject allowedMentions = new JsonObject();
            final EnumSet<MentionParseFlag> parse = options.parseFlags();
            if(parse == null) {
                // These act like a whitelist regardless of parse being present.
                allowedMentions.put("users", options.mentionedUsers());
                allowedMentions.put("roles", options.mentionedRoles());
            } else {
                final JsonArray parseList = new JsonArray();
                for(final MentionParseFlag p : parse) {
                    parseList.add(p.getName());
                }
                allowedMentions.put("parse", parseList);
                //If either list is present along with the respective parse option, validation fails. The contains check avoids this.
                if(!parse.contains(MentionParseFlag.USERS)) {
                    allowedMentions.put("users", options.mentionedUsers());
                }
                if(!parse.contains(MentionParseFlag.ROLES)) {
                    allowedMentions.put("roles", options.mentionedRoles());
                }
            }
            json.put("allowed_mentions", allowedMentions);
        }
        
        final OutboundRequest request = new OutboundRequest(Routes.CREATE_MESSAGE.withMajorParam(channelId), Map.of(), json);
        final List<ImmutablePair<String, byte[]>> buffers = options.files();
        if(buffers != null && !buffers.isEmpty()) {
            request.buffers(buffers);
        }
        return catnip().requester()
                .queue(request)
                .map(ResponsePayload::object);
    }
    
    @Nonnull
    @CheckReturnValue
    public Single<Message> getMessage(@Nonnull final String channelId, @Nonnull final String messageId) {
        return Single.fromObservable(getMessageRaw(channelId, messageId).map(entityBuilder()::createMessage));
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<JsonObject> getMessageRaw(@Nonnull final String channelId, @Nonnull final String messageId) {
        return catnip().requester().queue(
                new OutboundRequest(Routes.GET_CHANNEL_MESSAGE.withMajorParam(channelId),
                        Map.of("message", messageId)))
                .map(ResponsePayload::object);
    }
    
    @Nonnull
    public Single<Message> editMessage(@Nonnull final String channelId, @Nonnull final String messageId,
                                       @Nonnull final String content) {
        return editMessage(channelId, messageId, new MessageOptions().content(content).buildMessage());
    }
    
    @Nonnull
    public Single<Message> editMessage(@Nonnull final String channelId, @Nonnull final String messageId,
                                       @Nonnull final String content, @Nonnull final Set<MessageFlag> flags) {
        return editMessage(channelId, messageId, new MessageOptions().content(content), flags);
    }
    
    @Nonnull
    public Single<Message> editMessage(@Nonnull final String channelId, @Nonnull final String messageId,
                                       @Nonnull final Embed embed) {
        return editMessage(channelId, messageId, new MessageOptions().embed(embed));
    }
    
    @Nonnull
    public Single<Message> editMessage(@Nonnull final String channelId, @Nonnull final String messageId,
                                       @Nonnull final Embed embed, @Nonnull final Set<MessageFlag> flags) {
        return editMessage(channelId, messageId, new MessageOptions().embed(embed), flags);
    }
    
    @Nonnull
    public Single<Message> editMessage(@Nonnull final String channelId, @Nonnull final String messageId,
                                       @Nonnull final MessageOptions message) {
        return editMessage(channelId, messageId, message, Set.of());
    }
    
    @Nonnull
    public Single<Message> editMessage(@Nonnull final String channelId, @Nonnull final String messageId,
                                       @Nonnull final MessageOptions message, @Nonnull final Set<MessageFlag> flags) {
        return Single.fromObservable(editMessageRaw(channelId, messageId, message, flags)
                .map(entityBuilder()::createMessage));
    }
    
    @Nonnull
    public Single<Message> editMessage(@Nonnull final String channelId, @Nonnull final String messageId,
                                       @Nonnull final Message message) {
        return editMessage(channelId, messageId, message, Set.of());
    }
    
    @Nonnull
    public Single<Message> editMessage(@Nonnull final String channelId, @Nonnull final String messageId,
                                       @Nonnull final Message message, @Nonnull final Set<MessageFlag> flags) {
        return Single.fromObservable(editMessageRaw(channelId, messageId, new MessageOptions(message), flags)
                .map(entityBuilder()::createMessage));
    }
    
    @Nonnull
    public Observable<JsonObject> editMessageRaw(@Nonnull final String channelId, @Nonnull final String messageId,
                                                 @Nonnull final MessageOptions messageOptions,
                                                 @SuppressWarnings("TypeMayBeWeakened") @Nonnull final Set<MessageFlag> flags) {
        final JsonObject json = new JsonObject();
        if(messageOptions.embed() == null && (messageOptions.content() == null || messageOptions.content().isEmpty())) {
            throw new IllegalArgumentException("Can't build a message with no content and no embed!");
        }
        json.put("content", messageOptions.content());
        if(messageOptions.embed() != null || messageOptions.override()) {
            if(messageOptions.embed() == null) {
                json.put("embed", null);
            } else {
                json.put("embed", entityBuilder().embedToJson(messageOptions.embed()));
            }
        }
        if(json.get("embed") == null && json.get("content") == null) {
            throw new IllegalArgumentException("Can't build a message with no content and no embed!");
        }
        if(!flags.isEmpty() || messageOptions.override()) {
            json.put("flags", MessageFlag.fromSettable(flags));
        }
        return catnip().requester()
                .queue(new OutboundRequest(Routes.EDIT_MESSAGE.withMajorParam(channelId),
                        Map.of("message", messageId), json))
                .map(ResponsePayload::object);
    }
    
    @Nonnull
    public Completable deleteMessage(@Nonnull final String channelId, @Nonnull final String messageId,
                                     @Nullable final String reason) {
        return catnip().requester().queue(new OutboundRequest(Routes.DELETE_MESSAGE.withMajorParam(channelId),
                Map.of("message", messageId)).reason(reason))
                .ignoreElements();
    }
    
    @Nonnull
    public Completable deleteMessage(@Nonnull final String channelId, @Nonnull final String messageId) {
        return deleteMessage(channelId, messageId, null);
    }
    
    @Nonnull
    public Completable deleteMessages(@Nonnull final String channelId, @Nonnull final List<String> messageIds,
                                      @Nullable final String reason) {
        return catnip().requester()
                .queue(new OutboundRequest(Routes.BULK_DELETE_MESSAGES.withMajorParam(channelId),
                        Map.of(), JsonObject.builder().value("messages", new JsonArray(messageIds)).done(), reason))
                .ignoreElements();
    }
    
    @Nonnull
    public Completable deleteMessages(@Nonnull final String channelId, @Nonnull final List<String> messageIds) {
        return deleteMessages(channelId, messageIds, null);
    }
    
    @Nonnull
    public Completable addReaction(@Nonnull final String channelId, @Nonnull final String messageId,
                                   @Nonnull final String emoji) {
        return catnip().requester().queue(new OutboundRequest(Routes.CREATE_REACTION.withMajorParam(channelId),
                Map.of("message", messageId, "emojis", encodeUTF8(emoji)), new JsonObject()))
                .ignoreElements();
    }
    
    @Nonnull
    public Completable addReaction(@Nonnull final String channelId, @Nonnull final String messageId,
                                   @Nonnull final Emoji emoji) {
        return addReaction(channelId, messageId, emoji.forReaction());
    }
    
    @Nonnull
    public Completable deleteOwnReaction(@Nonnull final String channelId, @Nonnull final String messageId,
                                         @Nonnull final String emoji) {
        return Completable.fromObservable(catnip().requester()
                .queue(new OutboundRequest(Routes.DELETE_OWN_REACTION.withMajorParam(channelId),
                        Map.of("message", messageId, "emojis", encodeUTF8(emoji))).emptyBody(true)));
    }
    
    @Nonnull
    public Completable deleteOwnReaction(@Nonnull final String channelId, @Nonnull final String messageId,
                                         @Nonnull final Emoji emoji) {
        return deleteOwnReaction(channelId, messageId, emoji.forReaction());
    }
    
    @Nonnull
    public Completable deleteUserReaction(@Nonnull final String channelId, @Nonnull final String messageId,
                                          @Nonnull final String userId, @Nonnull final String emoji) {
        return Completable.fromObservable(catnip().requester()
                .queue(new OutboundRequest(Routes.DELETE_USER_REACTION.withMajorParam(channelId),
                        Map.of("message", messageId, "emojis", encodeUTF8(emoji), "user", userId))
                        .emptyBody(true)));
    }
    
    @Nonnull
    public Completable deleteUserReaction(@Nonnull final String channelId, @Nonnull final String messageId,
                                          @Nonnull final String userId, @Nonnull final Emoji emoji) {
        return deleteUserReaction(channelId, messageId, userId, emoji.forReaction());
    }
    
    @Nonnull
    public Completable deleteAllReactions(@Nonnull final String channelId, @Nonnull final String messageId) {
        return Completable.fromObservable(catnip().requester()
                .queue(new OutboundRequest(Routes.DELETE_ALL_REACTIONS.withMajorParam(channelId),
                        Map.of("message", messageId)).emptyBody(true)));
    }
    
    @Nonnull
    @CheckReturnValue
    public ReactionPaginator getReactions(@Nonnull final String channelId, @Nonnull final String messageId,
                                          @Nonnull final String emoji) {
        return new ReactionPaginator(entityBuilder()) {
            @Nonnull
            @CheckReturnValue
            @Override
            protected Observable<JsonArray> fetchNext(@Nonnull final RequestState<User> state, @Nullable final String lastId,
                                                      @Nonnegative final int requestSize) {
                return getReactionsRaw(channelId, messageId, emoji, null, lastId, requestSize);
            }
        };
    }
    
    @Nonnull
    @CheckReturnValue
    public ReactionPaginator getReactions(@Nonnull final String channelId, @Nonnull final String messageId,
                                          @Nonnull final Emoji emoji) {
        return getReactions(channelId, messageId, emoji.forReaction());
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<JsonArray> getReactionsRaw(@Nonnull final String channelId, @Nonnull final String messageId,
                                                 @Nonnull final String emoji, @Nullable final String before,
                                                 @Nullable final String after, @Nonnegative final int limit) {
        
        final QueryStringBuilder builder = new QueryStringBuilder();
        if(limit > 0) {
            builder.append("limit", Integer.toString(limit));
        }
        if(before != null) {
            builder.append("before", before);
        }
        
        if(after != null) {
            builder.append("after", after);
        }
        
        final String query = builder.build();
        return catnip().requester()
                .queue(new OutboundRequest(Routes.GET_REACTIONS.withMajorParam(channelId).withQueryString(query),
                        Map.of("message", messageId, "emojis", encodeUTF8(emoji))))
                .map(ResponsePayload::array);
    }
    
    public Observable<User> getReactions(@Nonnull final String channelId, @Nonnull final String messageId,
                                         @Nonnull final String emoji, @Nullable final String before,
                                         @Nullable final String after, @Nonnegative final int limit) {
        return getReactionsRaw(channelId, messageId, emoji, before, after, limit)
                .map(e -> mapObjectContents(entityBuilder()::createUser).apply(e))
                .flatMapIterable(e -> e)
                ;
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<User> getReactions(@Nonnull final String channelId, @Nonnull final String messageId,
                                         @Nonnull final Emoji emoji, @Nullable final String before,
                                         @Nullable final String after, @Nonnegative final int limit) {
        return getReactions(channelId, messageId, emoji.forReaction(), before, after, limit);
    }
    
    @Nonnull
    @CheckReturnValue
    public MessagePaginator getChannelMessages(@Nonnull final String channelId) {
        return new MessagePaginator(entityBuilder()) {
            @Nonnull
            @CheckReturnValue
            @Override
            protected Observable<JsonArray> fetchNext(@Nonnull final RequestState<Message> state, @Nullable final String lastId,
                                                      @Nonnegative final int requestSize) {
                return getChannelMessagesRaw(channelId, lastId, null, null, requestSize);
            }
        };
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<JsonArray> getChannelMessagesRaw(@Nonnull final String channelId, @Nullable final String before,
                                                       @Nullable final String after, @Nullable final String around,
                                                       @Nonnegative final int limit) {
        final QueryStringBuilder builder = new QueryStringBuilder();
        
        if(limit > 0) {
            builder.append("limit", Integer.toString(limit));
        }
        
        if(after != null) {
            builder.append("after", after);
        }
        
        if(around != null) {
            builder.append("around", around);
        }
        
        if(before != null) {
            builder.append("before", before);
        }
        
        final String query = builder.build();
        return catnip().requester()
                .queue(new OutboundRequest(Routes.GET_CHANNEL_MESSAGES.withMajorParam(channelId).withQueryString(query),
                        Map.of()))
                .map(ResponsePayload::array);
    }
    
    public Observable<Message> getChannelMessages(@Nonnull final String channelId, @Nullable final String before,
                                                  @Nullable final String after, @Nullable final String around,
                                                  @Nonnegative final int limit) {
        return getChannelMessagesRaw(channelId, before, after, around, limit)
                .map(e -> mapObjectContents(entityBuilder()::createMessage).apply(e))
                .flatMapIterable(e -> e);
    }
    
    @Nonnull
    public Completable triggerTypingIndicator(@Nonnull final String channelId) {
        return Completable.fromObservable(catnip().requester()
                .queue(new OutboundRequest(Routes.TRIGGER_TYPING_INDICATOR.withMajorParam(channelId),
                        Map.of()).emptyBody(true)));
    }
    
    @Nonnull
    @CheckReturnValue
    public Single<Channel> getChannelById(@Nonnull final String channelId) {
        return Single.fromObservable(getChannelByIdRaw(channelId).map(entityBuilder()::createChannel));
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<JsonObject> getChannelByIdRaw(@Nonnull final String channelId) {
        return catnip().requester().queue(new OutboundRequest(Routes.GET_CHANNEL.withMajorParam(channelId),
                Map.of()))
                .map(ResponsePayload::object);
    }
    
    @Nonnull
    @CheckReturnValue
    public Single<Channel> deleteChannel(@Nonnull final String channelId, @Nullable final String reason) {
        return Single.fromObservable(deleteChannelRaw(channelId, reason).map(entityBuilder()::createChannel));
    }
    
    @Nonnull
    @CheckReturnValue
    public Single<Channel> deleteChannel(@Nonnull final String channelId) {
        return deleteChannel(channelId, null);
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<JsonObject> deleteChannelRaw(@Nonnull final String channelId, @Nullable final String reason) {
        return catnip().requester().queue(new OutboundRequest(Routes.DELETE_CHANNEL.withMajorParam(channelId),
                Map.of()).reason(reason))
                .map(ResponsePayload::object);
    }
    
    @Nonnull
    @CheckReturnValue
    public Single<CreatedInvite> createInvite(@Nonnull final String channelId,
                                              @Nullable final InviteCreateOptions options,
                                              @Nullable final String reason) {
        return Single.fromObservable(createInviteRaw(channelId, options, reason).map(entityBuilder()::createCreatedInvite));
    }
    
    @Nonnull
    @CheckReturnValue
    public Single<CreatedInvite> createInvite(@Nonnull final String channelId,
                                              @Nullable final InviteCreateOptions options) {
        return createInvite(channelId, options, null);
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<JsonObject> createInviteRaw(@Nonnull final String channelId,
                                                  @Nullable final InviteCreateOptions options,
                                                  @Nullable final String reason) {
        return catnip().requester().queue(new OutboundRequest(Routes.CREATE_CHANNEL_INVITE.withMajorParam(channelId),
                Map.of(), (options == null ? InviteCreateOptions.create() : options).toJson(), reason))
                .map(ResponsePayload::object);
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<CreatedInvite> getChannelInvites(@Nonnull final String channelId) {
        return getChannelInvitesRaw(channelId)
                .map(e -> mapObjectContents(entityBuilder()::createCreatedInvite).apply(e))
                .flatMapIterable(e -> e);
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<JsonArray> getChannelInvitesRaw(@Nonnull final String channelId) {
        return catnip().requester().queue(new OutboundRequest(Routes.GET_CHANNEL_INVITES.withMajorParam(channelId),
                Map.of()))
                .map(ResponsePayload::array);
    }
    
    @Nonnull
    public Single<GuildChannel> modifyChannel(@Nonnull final String channelId,
                                              @Nonnull final ChannelEditFields fields,
                                              @Nullable final String reason) {
        return Single.fromObservable(modifyChannelRaw(channelId, fields, reason).map(entityBuilder()::createGuildChannel));
    }
    
    @Nonnull
    public Single<GuildChannel> modifyChannel(@Nonnull final String channelId,
                                              @Nonnull final ChannelEditFields fields) {
        return modifyChannel(channelId, fields, null);
    }
    
    @Nonnull
    public Observable<JsonObject> modifyChannelRaw(@Nonnull final String channelId,
                                                   @Nonnull final ChannelEditFields fields,
                                                   @Nullable final String reason) {
        return catnip().requester().queue(new OutboundRequest(Routes.MODIFY_CHANNEL.withMajorParam(channelId),
                Map.of(), fields.payload(), reason))
                .map(ResponsePayload::object);
    }
    
    @Nonnull
    public Completable deletePermissionOverride(@Nonnull final String channelId,
                                                @Nonnull final String overwriteId, @Nullable final String reason) {
        return Completable.fromObservable(catnip().requester()
                .queue(new OutboundRequest(Routes.DELETE_CHANNEL_PERMISSION.withMajorParam(channelId),
                        Map.of("overwrite", overwriteId)).reason(reason).emptyBody(true)));
    }
    
    @Nonnull
    public Completable deletePermissionOverride(@Nonnull final String channelId,
                                                @Nonnull final PermissionOverride overwrite,
                                                @Nullable final String reason) {
        return deletePermissionOverride(channelId, overwrite.id(), reason);
    }
    
    @Nonnull
    public Completable deletePermissionOverride(@Nonnull final String channelId,
                                                @Nonnull final PermissionOverride overwrite) {
        return deletePermissionOverride(channelId, overwrite, null);
    }
    
    @Nonnull
    public Completable editPermissionOverride(@Nonnull final String channelId, @Nonnull final String overwriteId,
                                              @Nonnull final Collection<Permission> allowed,
                                              @Nonnull final Collection<Permission> denied,
                                              final boolean isMember, @Nullable final String reason) {
        return Completable.fromObservable(catnip().requester()
                .queue(new OutboundRequest(Routes.EDIT_CHANNEL_PERMISSIONS.withMajorParam(channelId),
                        Map.of("overwrite", overwriteId), JsonObject.builder()
                        .value("allow", Permission.from(allowed))
                        .value("deny", Permission.from(denied))
                        .value("type", isMember ? "member" : "role")
                        .done(),
                        reason
                )));
    }
    
    @Nonnull
    public Completable editPermissionOverride(@Nonnull final String channelId, @Nonnull final String overwriteId,
                                              @Nonnull final Collection<Permission> allowed,
                                              @Nonnull final Collection<Permission> denied,
                                              final boolean isMember) {
        return editPermissionOverride(channelId, overwriteId, allowed, denied, isMember, null);
    }
    
    @Nonnull
    public Completable editPermissionOverride(@Nonnull final String channelId, @Nonnull final PermissionOverride overwrite,
                                              @Nonnull final Collection<Permission> allowed,
                                              @Nonnull final Collection<Permission> denied,
                                              @Nullable final String reason) {
        return editPermissionOverride(channelId,
                overwrite.id(),
                allowed,
                denied,
                overwrite.type() == OverrideType.MEMBER,
                reason
        );
    }
    
    @Nonnull
    public Completable editPermissionOverride(@Nonnull final String channelId, @Nonnull final PermissionOverride overwrite,
                                              @Nonnull final Collection<Permission> allowed,
                                              @Nonnull final Collection<Permission> denied) {
        return editPermissionOverride(channelId, overwrite, allowed, denied, null);
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<Message> getPinnedMessages(@Nonnull final String channelId) {
        return getChannelInvitesRaw(channelId)
                .map(e -> mapObjectContents(entityBuilder()::createMessage).apply(e))
                .flatMapIterable(e -> e);
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<JsonArray> getPinnedMessagesRaw(@Nonnull final String channelId) {
        return catnip().requester().queue(new OutboundRequest(Routes.GET_PINNED_MESSAGES.withMajorParam(channelId),
                Map.of()))
                .map(ResponsePayload::array);
    }
    
    @Nonnull
    public Completable deletePinnedMessage(@Nonnull final String channelId, @Nonnull final String messageId, @Nullable final String reason) {
        return Completable.fromObservable(catnip().requester()
                .queue(new OutboundRequest(Routes.DELETE_PINNED_CHANNEL_MESSAGE.withMajorParam(channelId),
                        Map.of("message", messageId)).reason(reason).emptyBody(true)));
    }
    
    @Nonnull
    public Completable deletePinnedMessage(@Nonnull final Message message, @Nullable final String reason) {
        return deletePinnedMessage(message.channelId(), message.id(), reason);
    }
    
    @Nonnull
    public Completable deletePinnedMessage(@Nonnull final String channelId, @Nonnull final String messageId) {
        return deletePinnedMessage(channelId, messageId, null);
    }
    
    @Nonnull
    public Completable deletePinnedMessage(@Nonnull final Message message) {
        return deletePinnedMessage(message.channelId(), message.id(), null);
    }
    
    @Nonnull
    public Completable addPinnedMessage(@Nonnull final String channelId, @Nonnull final String messageId, @Nullable final String reason) {
        return catnip().requester().queue(new OutboundRequest(Routes.ADD_PINNED_CHANNEL_MESSAGE.withMajorParam(channelId),
                Map.of("message", messageId)).reason("reason").emptyBody(true))
                .ignoreElements();
    }
    
    @Nonnull
    public Completable addPinnedMessage(@Nonnull final Message message, @Nullable final String reason) {
        return addPinnedMessage(message.channelId(), message.id(), reason);
    }
    
    @Nonnull
    public Completable addPinnedMessage(@Nonnull final String channelId, @Nonnull final String messageId) {
        return addPinnedMessage(channelId, messageId, null);
    }
    
    @Nonnull
    public Completable addPinnedMessage(@Nonnull final Message message) {
        return addPinnedMessage(message.channelId(), message.id(), null);
    }
    
    @Nonnull
    public Single<Webhook> createWebhook(@Nonnull final String channelId, @Nonnull final String name,
                                         @Nullable final String avatar, @Nullable final String reason) {
        return Single.fromObservable(createWebhookRaw(channelId, name, avatar, reason)
                .map(entityBuilder()::createWebhook));
    }
    
    @Nonnull
    public Single<Webhook> createWebhook(@Nonnull final String channelId, @Nonnull final String name,
                                         @Nullable final String avatar) {
        return createWebhook(channelId, name, avatar, null);
    }
    
    @Nonnull
    public Observable<JsonObject> createWebhookRaw(@Nonnull final String channelId, @Nonnull final String name,
                                                   @Nullable final String avatar, @Nullable final String reason) {
        return catnip().requester().queue(new OutboundRequest(Routes.CREATE_WEBHOOK.withMajorParam(channelId),
                Map.of(), JsonObject.builder().value("name", name).value("avatar", avatar).done(), reason))
                .map(ResponsePayload::object);
    }
}
