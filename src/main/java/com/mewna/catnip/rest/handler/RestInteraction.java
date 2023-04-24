/*
 * Copyright (c) 2020 amy, All rights reserved.
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
import com.mewna.catnip.entity.interaction.InteractionResponseType;
import com.mewna.catnip.entity.interaction.command.ApplicationCommand;
import com.mewna.catnip.entity.interaction.command.ApplicationCommandOption;
import com.mewna.catnip.entity.interaction.command.ApplicationCommandType;
import com.mewna.catnip.entity.message.MentionParseFlag;
import com.mewna.catnip.entity.message.Message;
import com.mewna.catnip.entity.message.MessageFlag;
import com.mewna.catnip.entity.message.MessageOptions;
import com.mewna.catnip.entity.message.component.MessageComponent;
import com.mewna.catnip.internal.CatnipImpl;
import com.mewna.catnip.rest.ResponsePayload;
import com.mewna.catnip.rest.Routes;
import com.mewna.catnip.rest.requester.Requester.OutboundRequest;
import com.mewna.catnip.util.JsonUtil;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author amy
 * @since 12/10/20.
 */
@SuppressWarnings("unused")
public class RestInteraction extends RestHandler {
    public RestInteraction(final CatnipImpl catnip) {
        super(catnip);
    }
    
    // Initial response
    
    public Completable createInteractionInitialResponse(@Nonnull final InteractionResponseType type,
                                                        @Nonnull final String interactionId,
                                                        @Nonnull final String interactionToken,
                                                        @Nonnull final MessageOptions options) {
        return createInteractionInitialResponse(type, interactionId, interactionToken, null, null, options);
    }
    
    public Completable createInteractionInitialResponse(@Nonnull final InteractionResponseType type,
                                                        @Nonnull final String interactionId,
                                                        @Nonnull final String interactionToken,
                                                        @Nullable final String username,
                                                        @Nonnull final MessageOptions options) {
        return createInteractionInitialResponse(type, interactionId, interactionToken, username, null, options);
    }
    
    public Completable createInteractionInitialResponse(@Nonnull final InteractionResponseType type,
                                                        @Nonnull final String interactionId,
                                                        @Nonnull final String interactionToken,
                                                        @Nullable final String username, @Nullable final String avatarUrl,
                                                        @Nonnull final MessageOptions options) {
        return createInteractionInitialResponseRaw(type, interactionId, interactionToken, username, avatarUrl, options);
    }
    
    public Completable createInteractionInitialResponseRaw(@Nonnull final InteractionResponseType type,
                                                           @Nonnull final String interactionId,
                                                           @Nonnull final String interactionToken,
                                                           @Nullable final String username,
                                                           @Nullable final String avatarUrl,
                                                           @Nonnull final MessageOptions options) {
        final var body = createSendBody(type, username, avatarUrl, options);
        return Completable.fromObservable(catnip().requester().
                queue(new OutboundRequest(Routes.CREATE_INTERACTION_INITIAL_RESPONSE.withMajorParam(interactionId)
                        .withQueryString("?wait=true"), Map.of("token", interactionToken), body).needsToken(false)
                        .buffers(options.files())));
    }
    
    public Single<Message> editInteractionInitialResponse(@Nonnull final String interactionId,
                                                          @Nonnull final String interactionToken,
                                                          @Nonnull final MessageOptions options) {
        return editInteractionInitialResponse(interactionId, interactionToken, null, null, options);
    }
    
    public Single<Message> editInteractionInitialResponse(@Nonnull final String interactionId,
                                                          @Nonnull final String interactionToken,
                                                          @Nullable final String username,
                                                          @Nonnull final MessageOptions options) {
        return editInteractionInitialResponse(interactionId, interactionToken, username, null, options);
    }
    
    public Single<Message> editInteractionInitialResponse(@Nonnull final String interactionId,
                                                          @Nonnull final String interactionToken,
                                                          @Nullable final String username, @Nullable final String avatarUrl,
                                                          @Nonnull final MessageOptions options) {
        return Single.fromObservable(editInteractionInitialResponseRaw(interactionId, interactionToken, username, avatarUrl,
                options).map(entityBuilder()::createMessage));
    }
    
    public Observable<JsonObject> editInteractionInitialResponseRaw(@Nonnull final String interactionId,
                                                                    @Nonnull final String interactionToken,
                                                                    @Nullable final String username,
                                                                    @Nullable final String avatarUrl,
                                                                    @Nonnull final MessageOptions options) {
        final var body = createSendBody(null, username, avatarUrl, options);
        return catnip().requester().
                queue(new OutboundRequest(Routes.EDIT_INTERACTION_INITIAL_RESPONSE.withMajorParam(interactionId)
                        .withQueryString("?wait=true"), Map.of("token", interactionToken), body).buffers(options.files()))
                .map(ResponsePayload::object);
    }
    
    public Completable deleteInteractionInitialResponse(@Nonnull final String interactionId,
                                                        @Nonnull final String interactionToken) {
        return Completable.fromObservable(catnip().requester().
                queue(new OutboundRequest(Routes.DELETE_INTERACTION_INITIAL_RESPONSE.withMajorParam(interactionId)
                        .withQueryString("?wait=true"), Map.of("token", interactionToken)).emptyBody(true)));
    }
    
    // Follow-ups
    
    public Single<Message> createInteractionFollowup(@Nonnull final InteractionResponseType type,
                                                     @Nonnull final String interactionId,
                                                     @Nonnull final String interactionToken,
                                                     @Nonnull final MessageOptions options) {
        return createInteractionFollowup(type, interactionId, interactionToken, null, null, options);
    }
    
    public Single<Message> createInteractionFollowup(@Nonnull final InteractionResponseType type,
                                                     @Nonnull final String interactionId,
                                                     @Nonnull final String interactionToken,
                                                     @Nullable final String username,
                                                     @Nonnull final MessageOptions options) {
        return createInteractionFollowup(type, interactionId, interactionToken, username, null, options);
    }
    
    public Single<Message> createInteractionFollowup(@Nonnull final InteractionResponseType type,
                                                     @Nonnull final String interactionId,
                                                     @Nonnull final String interactionToken,
                                                     @Nullable final String username, @Nullable final String avatarUrl,
                                                     @Nonnull final MessageOptions options) {
        return Single.fromObservable(createInteractionFollowupRaw(type, interactionId, interactionToken, username, avatarUrl, options)
                .map(entityBuilder()::createMessage));
    }
    
    public Observable<JsonObject> createInteractionFollowupRaw(@Nonnull final InteractionResponseType type,
                                                               @Nonnull final String interactionId,
                                                               @Nonnull final String interactionToken,
                                                               @Nullable final String username,
                                                               @Nullable final String avatarUrl,
                                                               @Nonnull final MessageOptions options) {
        final var body = createSendBody(type, username, avatarUrl, options);
        return catnip().requester().
                queue(new OutboundRequest(Routes.CREATE_INTERACTION_FOLLOWUP.withMajorParam(interactionId)
                        .withQueryString("?wait=true"), Map.of("interaction", interactionId, "token", interactionToken), body).needsToken(false)
                        .buffers(options.files()))
                .map(ResponsePayload::object);
    }
    
    public Single<Message> editInteractionFollowup(@Nonnull final String interactionId, @Nonnull final String interactionToken,
                                                   @Nonnull final String messageId, @Nonnull final MessageOptions options) {
        return editInteractionFollowup(interactionId, interactionToken, messageId, null, null, options);
    }
    
    public Single<Message> editInteractionFollowup(@Nonnull final String interactionId, @Nonnull final String interactionToken,
                                                   @Nonnull final String messageId, @Nullable final String username,
                                                   @Nonnull final MessageOptions options) {
        return editInteractionFollowup(interactionId, interactionToken, messageId, username, null, options);
    }
    
    public Single<Message> editInteractionFollowup(@Nonnull final String interactionId, @Nonnull final String interactionToken,
                                                   @Nonnull final String messageId, @Nullable final String username,
                                                   @Nullable final String avatarUrl, @Nonnull final MessageOptions options) {
        return Single.fromObservable(editInteractionFollowupRaw(interactionId, interactionToken, messageId, username,
                avatarUrl, options).map(entityBuilder()::createMessage));
    }
    
    public Observable<JsonObject> editInteractionFollowupRaw(@Nonnull final String interactionId,
                                                             @Nonnull final String interactionToken,
                                                             @Nonnull final String messageId, @Nullable final String username,
                                                             @Nullable final String avatarUrl,
                                                             @Nonnull final MessageOptions options) {
        final var body = createSendBody(null, username, avatarUrl, options);
        return catnip().requester().
                queue(new OutboundRequest(Routes.EDIT_INTERACTION_FOLLOWUP.withMajorParam(interactionId)
                        .withQueryString("?wait=true"), Map.of("token", interactionToken, "message", messageId), body)
                        .buffers(options.files()))
                .map(ResponsePayload::object);
    }
    
    public Completable deleteInteractionFollowup(@Nonnull final String interactionId, @Nonnull final String interactionToken,
                                                 @Nonnull final String messageId) {
        return Completable.fromObservable(catnip().requester().
                queue(new OutboundRequest(Routes.DELETE_INTERACTION_FOLLOWUP.withMajorParam(interactionId)
                        .withQueryString("?wait=true"), Map.of("token", interactionToken, "message", messageId))
                        .emptyBody(true)));
    }
    
    // Global commands
    
//    public Observable<ApplicationCommand> getGlobalApplicationCommands() {
//        return getGlobalApplicationCommandsRaw()
//                .map(e -> JsonUtil.mapObjectContents(entityBuilder()::createApplicationCommand).apply(e))
//                .flatMapIterable(e -> e);
//    }
    
    public Observable<JsonArray> getGlobalApplicationCommandsRaw() {
        return catnip().requester().queue(new OutboundRequest(Routes.GET_GLOBAL_APPLICATION_COMMANDS.withMajorParam(catnip().clientId()),
                        Map.of()))
                .map(ResponsePayload::array);
    }
    
//    public Single<ApplicationCommand> createGlobalApplicationCommand(@Nonnull final ApplicationCommandType type,
//                                                                     @Nonnull final String name, @Nullable final String description,
//                                                                     @Nonnull final Collection<ApplicationCommandOption> options) {
//        return Single.fromObservable(createGlobalApplicationCommandRaw(type, name, description, options)
//                .map(entityBuilder()::createApplicationCommand));
//    }
    
//    public Observable<JsonObject> createGlobalApplicationCommandRaw(@Nonnull final ApplicationCommandType type,
//                                                                    @Nonnull final String name, @Nullable final String description,
//                                                                    @Nonnull final Collection<ApplicationCommandOption> options) {
//        final JsonObject body = createCommandBody(type, name, description, options);
//        return catnip().requester().queue(new OutboundRequest(Routes.CREATE_GLOBAL_APPLICATION_COMMAND
//                .withMajorParam(catnip().clientId()), Map.of()).object(body)).map(ResponsePayload::object);
//    }
//
//    public Single<ApplicationCommand> editGlobalApplicationCommand(@Nonnull final ApplicationCommandType type,
//                                                                   @Nonnull final String name, @Nullable final String description,
//                                                                   @Nonnull final String commandId,
//                                                                   @Nonnull final Collection<ApplicationCommandOption> options) {
//        return Single.fromObservable(editGlobalApplicationCommandRaw(type, name, description, commandId, options)
//                .map(entityBuilder()::createApplicationCommand));
//    }
    
    public Observable<JsonObject> editGlobalApplicationCommandRaw(@Nonnull final ApplicationCommandType type,
                                                                  @Nonnull final String name, @Nullable final String description,
                                                                  @Nonnull final String commandId,
                                                                  @Nonnull final Collection<ApplicationCommandOption> options) {
        final var body = createCommandBody(type, name, description, options);
        return catnip().requester().queue(new OutboundRequest(Routes.EDIT_GLOBAL_APPLICATION_COMMAND
                .withMajorParam(catnip().clientId()), Map.of("command", commandId)).object(body)).map(ResponsePayload::object);
    }
    
    public Completable deleteGlobalApplicationCommand(@Nonnull final String commandId) {
        return Completable.fromObservable(catnip().requester().queue(
                new OutboundRequest(Routes.DELETE_GLOBAL_APPLICATION_COMMAND.withMajorParam(catnip().clientId()),
                        Map.of("command", commandId))));
    }
    
    // Guild commands
    public Observable<ApplicationCommand> getGuildApplicationCommands(@Nonnull final String guildId) {
        return getGuildApplicationCommandsRaw(guildId)
                .map(e -> JsonUtil.mapObjectContents(entityBuilder()::createApplicationCommand).apply(e))
                .flatMapIterable(e -> e);
    }
    
    public Observable<JsonArray> getGuildApplicationCommandsRaw(@Nonnull final String guildId) {
        return catnip().requester().queue(new OutboundRequest(Routes.GET_GUILD_APPLICATION_COMMANDS.withMajorParam(catnip().clientId()),
                        Map.of("guild", guildId)))
                .map(ResponsePayload::array);
    }
    
    public Single<ApplicationCommand> createGuildApplicationCommand(@Nonnull final ApplicationCommandType type,
                                                                    @Nonnull final String guildId, @Nonnull final String name,
                                                                    @Nullable final String description,
                                                                    @Nonnull final Collection<ApplicationCommandOption> options) {
        return Single.fromObservable(createGuildApplicationCommandRaw(type, guildId, name, description, options)
                .map(entityBuilder()::createApplicationCommand));
    }
    
    public Observable<JsonObject> createGuildApplicationCommandRaw(@Nonnull final ApplicationCommandType type,
                                                                   @Nonnull final String guildId, @Nonnull final String name,
                                                                   @Nullable final String description,
                                                                   @Nonnull final Collection<ApplicationCommandOption> options) {
        final var body = createCommandBody(type, name, description, options);
        return catnip().requester().queue(new OutboundRequest(Routes.CREATE_GUILD_APPLICATION_COMMAND
                .withMajorParam(catnip().clientId()), Map.of("guild", guildId)).object(body)).map(ResponsePayload::object);
    }
    
    public Single<ApplicationCommand> editGuildApplicationCommand(@Nonnull final ApplicationCommandType type,
                                                                  @Nonnull final String guildId, @Nonnull final String name,
                                                                  @Nullable final String description,
                                                                  @Nonnull final String commandId,
                                                                  @Nonnull final Collection<ApplicationCommandOption> options) {
        return Single.fromObservable(editGuildApplicationCommandRaw(type, guildId, name, description, commandId, options)
                .map(entityBuilder()::createApplicationCommand));
    }
    
    public Observable<JsonObject> editGuildApplicationCommandRaw(@Nonnull final ApplicationCommandType type,
                                                                 @Nonnull final String guildId, @Nonnull final String name,
                                                                 @Nullable final String description,
                                                                 @Nonnull final String commandId,
                                                                 @Nonnull final Collection<ApplicationCommandOption> options) {
        final var body = createCommandBody(type, name, description, options);
        return catnip().requester().queue(new OutboundRequest(Routes.EDIT_GUILD_APPLICATION_COMMAND
                        .withMajorParam(catnip().clientId()), Map.of("guild", guildId, "command", commandId)).object(body))
                .map(ResponsePayload::object);
    }
    
    public Completable deleteGuildApplicationCommand(@Nonnull final String guildId, @Nonnull final String commandId) {
        return Completable.fromObservable(catnip().requester().queue(
                new OutboundRequest(Routes.DELETE_GUILD_APPLICATION_COMMAND.withMajorParam(catnip().clientId()),
                        Map.of("guild", guildId, "command", commandId))));
    }
    
    private JsonObject createSendBody(@Nullable final InteractionResponseType type, @Nullable final String username,
                                      @Nullable final String avatarUrl, @Nonnull final MessageOptions options) {
        final var builder = JsonObject.builder();
        if(type != null) {
            builder.value("type", type.key());
        }
        
        {
            final var innerBuilder = JsonObject.builder();
            if(options.content() != null && !options.content().isEmpty()) {
                innerBuilder.value("content", options.content());
            }
            if(!options.embeds().isEmpty()) {
                innerBuilder.array("embeds");
                options.embeds().stream().map(e -> entityBuilder().embedToJson(e)).forEach(innerBuilder::value);
                innerBuilder.end();
            }
            if(username != null && !username.isEmpty()) {
                innerBuilder.value("username", username);
            }
            if(avatarUrl != null && !avatarUrl.isEmpty()) {
                innerBuilder.value("avatar_url", avatarUrl);
            }
            
            final JsonObject body = innerBuilder.done();
            
            if(body.get("embeds") == null && body.get("content") == null
                    && !options.hasFiles()) {
                throw new IllegalArgumentException("Can't build a message with no content, no embeds and no files!");
            }
            if(!options.flags().isEmpty() || options.override()) {
                innerBuilder.value("flags", MessageFlag.fromSettable(options.flags()));
            }
            final JsonObject allowedMentions = new JsonObject();
            if(options.parseFlags() != null || options.mentionedUsers() != null || options.mentionedRoles() != null) {
                final EnumSet<MentionParseFlag> parse = options.parseFlags();
                if(parse == null) {
                    // These act like a whitelist regardless of parse being present.
                    allowedMentions.put("users", options.mentionedUsers());
                    allowedMentions.put("roles", options.mentionedRoles());
                } else {
                    final JsonArray parseList = new JsonArray();
                    for(final MentionParseFlag p : parse) {
                        parseList.add(p.flagName());
                    }
                    allowedMentions.put("parse", parseList);
                    // If either list is present along with the respective parse option, validation fails.
                    // The contains check avoids this.
                    if(!parse.contains(MentionParseFlag.USERS)) {
                        allowedMentions.put("users", options.mentionedUsers());
                    }
                    if(!parse.contains(MentionParseFlag.ROLES)) {
                        allowedMentions.put("roles", options.mentionedRoles());
                    }
                }
            }
            if(options.reference() != null) {
                allowedMentions.put("replied_user", options.pingReply());
            }
            if(!allowedMentions.isEmpty()) {
                innerBuilder.value("allowed_mentions", allowedMentions);
            }
            if(!options.components().isEmpty()) {
                innerBuilder.value("components", new JsonArray(options.components().stream().map(MessageComponent::toJson).collect(Collectors.toList())));
            }
            
            builder.value("data", innerBuilder.done());
        }
        
        return builder.done();
    }
    
    JsonObject createCommandBody(@Nonnull final ApplicationCommandType type, @Nonnull final String name,
                                 @Nullable final String description, @Nonnull final Collection<ApplicationCommandOption> options) {
        final var builder = JsonObject.builder();
        if(type == ApplicationCommandType.CHAT_INPUT) {
            if(!name.matches("^[\\w-]{1,32}$") || !name.toLowerCase(Locale.ROOT).equals(name)) {
                throw new IllegalArgumentException("Name must match ^[\\w-]{1,32}$ and must be lowercase!");
            }
        }
        if(type == ApplicationCommandType.USER || type == ApplicationCommandType.MESSAGE) {
            if(description != null) {
                throw new IllegalArgumentException("Context menu commands must not have a description");
            }
        }
        builder.value("name", name);
        builder.value("description", description);
        builder.value("type", type.id());
        if(!options.isEmpty()) {
            final List<JsonObject> optionJson = options.stream()
                    .map(ApplicationCommandOption::toJson)
                    .collect(Collectors.toList());
            builder.value("options", optionJson);
        }
        return builder.done();
    }
}
