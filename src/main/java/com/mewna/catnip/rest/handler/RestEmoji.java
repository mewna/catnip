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

import com.google.common.collect.ImmutableMap;
import com.mewna.catnip.entity.misc.Emoji.CustomEmoji;
import com.mewna.catnip.internal.CatnipImpl;
import com.mewna.catnip.rest.ResponsePayload;
import com.mewna.catnip.rest.RestRequester.OutboundRequest;
import com.mewna.catnip.rest.Routes;
import com.mewna.catnip.util.Utils;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * @author natanbc
 * @since 9/5/18.
 */
public class RestEmoji extends RestHandler {
    public RestEmoji(final CatnipImpl catnip) {
        super(catnip);
    }
    
    @Nonnull
    public CompletionStage<List<CustomEmoji>> listGuildEmojis(@Nonnull final String guildId) {
        return getCatnip().requester().queue(
                new OutboundRequest(
                        Routes.LIST_GUILD_EMOJIS.withMajorParam(guildId),
                        ImmutableMap.of()))
                .thenApply(ResponsePayload::array)
                .thenApply(mapObjectContents(e -> getEntityBuilder().createCustomEmoji(guildId, e)))
                .thenApply(Collections::unmodifiableList);
    }
    
    @Nonnull
    public CompletionStage<CustomEmoji> getGuildEmoji(@Nonnull final String guildId, @Nonnull final String emojiId) {
        return getCatnip().requester().queue(
                new OutboundRequest(
                        Routes.GET_GUILD_EMOJI.withMajorParam(guildId),
                        ImmutableMap.of("emojis.id", emojiId)))
                .thenApply(ResponsePayload::object)
                .thenApply(e -> getEntityBuilder().createCustomEmoji(guildId, e));
    }
    
    @Nonnull
    public CompletionStage<CustomEmoji> createGuildEmoji(@Nonnull final String guildId, @Nonnull final String name,
                                                         @Nonnull final URI imageData, @Nonnull final Collection<String> roles) {
        Utils.validateImageUri(imageData);
        final JsonArray rolesArray;
        if(roles.isEmpty()) {
            rolesArray = null;
        } else {
            rolesArray = new JsonArray();
            roles.forEach(rolesArray::add);
        }
        return getCatnip().requester().queue(
                new OutboundRequest(
                        Routes.CREATE_GUILD_EMOJI.withMajorParam(guildId),
                        ImmutableMap.of(),
                        new JsonObject()
                                .put("name", name)
                                .put("image", imageData.toString())
                                .put("roles", rolesArray)
                ))
                .thenApply(ResponsePayload::object)
                .thenApply(e -> getEntityBuilder().createEmoji(guildId, e))
                .thenApply(CustomEmoji.class::cast);
    }
    
    @Nonnull
    public CompletionStage<CustomEmoji> createGuildEmoji(@Nonnull final String guildId, @Nonnull final String name,
                                                         @Nonnull final byte[] image, @Nonnull final Collection<String> roles) {
        return createGuildEmoji(guildId, name, Utils.asImageDataUri(image), roles);
    }
    
    @Nonnull
    public CompletionStage<CustomEmoji> modifyGuildEmoji(@Nonnull final String guildId, @Nonnull final String emojiId,
                                                         @Nonnull final String name, @Nonnull final Collection<String> roles) {
        final JsonArray rolesArray;
        if(roles.isEmpty()) {
            rolesArray = null;
        } else {
            rolesArray = new JsonArray();
            roles.forEach(rolesArray::add);
        }
        return getCatnip().requester().queue(
                new OutboundRequest(
                        Routes.MODIFY_GUILD_EMOJI.withMajorParam(guildId),
                        ImmutableMap.of("emojis.id", emojiId),
                        new JsonObject()
                                .put("name", name)
                                .put("roles", rolesArray)
                ))
                .thenApply(ResponsePayload::object)
                .thenApply(e -> getEntityBuilder().createEmoji(guildId, e))
                .thenApply(CustomEmoji.class::cast);
    }
    
    @Nonnull
    public CompletionStage<Void> deleteGuildEmoji(@Nonnull final String guildId, @Nonnull final String emojiId) {
        return getCatnip().requester().queue(
                new OutboundRequest(
                        Routes.DELETE_GUILD_EMOJI.withMajorParam(guildId),
                        ImmutableMap.of("emojis.id", emojiId)))
                .thenApply(__ -> null);
    }
}
