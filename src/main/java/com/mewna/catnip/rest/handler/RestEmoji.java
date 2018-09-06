package com.mewna.catnip.rest.handler;

import com.google.common.collect.ImmutableMap;
import com.mewna.catnip.entity.Emoji;
import com.mewna.catnip.entity.Emoji.CustomEmoji;
import com.mewna.catnip.internal.CatnipImpl;
import com.mewna.catnip.rest.ResponsePayload;
import com.mewna.catnip.rest.RestRequester.OutboundRequest;
import com.mewna.catnip.rest.Routes;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author natanbc
 * @since 9/5/18.
 */
public class RestEmoji extends RestHandler {
    public RestEmoji(final CatnipImpl catnip) {
        super(catnip);
    }
    
    @Nonnull
    public CompletableFuture<List<Emoji>> listGuildEmojis(@Nonnull final String guildId) {
        return getCatnip().requester().queue(
                new OutboundRequest(
                        Routes.LIST_GUILD_EMOJIS.withMajorParam(guildId),
                        ImmutableMap.of(), null
                ))
                .thenApply(ResponsePayload::array)
                .thenApply(mapObjectContents(getEntityBuilder()::createEmoji));
    }
    
    @Nonnull
    public CompletableFuture<Emoji> getGuildEmoji(@Nonnull final String guildId, @Nonnull final String emojiId) {
        return getCatnip().requester().queue(
                new OutboundRequest(
                        Routes.GET_GUILD_EMOJI.withMajorParam(guildId),
                        ImmutableMap.of("emoji.id", emojiId), null
                ))
                .thenApply(ResponsePayload::object)
                .thenApply(getEntityBuilder()::createEmoji);
    }
    
    @Nonnull
    public CompletableFuture<CustomEmoji> createGuildEmoji(@Nonnull final String guildId, @Nonnull final String name,
                                                     @Nonnull final String base64Image, @Nonnull final Collection<String> roles) {
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
                                //note: even though image/jpeg is hardcoded, png/gifs are still supported
                                .put("image", "data:image/jpeg;base64," + base64Image)
                                .put("roles", rolesArray)
                ))
                .thenApply(ResponsePayload::object)
                .thenApply(getEntityBuilder()::createEmoji)
                .thenApply(CustomEmoji.class::cast);
    }
    
    @Nonnull
    public CompletableFuture<CustomEmoji> createGuildEmoji(@Nonnull final String guildId, @Nonnull final String name,
                                                     @Nonnull final byte[] image, @Nonnull final Collection<String> roles) {
        return createGuildEmoji(guildId, name, Base64.getEncoder().encodeToString(image), roles);
    }
    
    @Nonnull
    public CompletableFuture<CustomEmoji> modifyGuildEmoji(@Nonnull final String guildId, @Nonnull final String emojiId,
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
                        ImmutableMap.of("emoji.id", emojiId),
                        new JsonObject()
                                .put("name", name)
                                .put("roles", rolesArray)
                ))
                .thenApply(ResponsePayload::object)
                .thenApply(getEntityBuilder()::createEmoji)
                .thenApply(CustomEmoji.class::cast);
    }
    
    @Nonnull
    public CompletableFuture<Void> deleteGuildEmoji(@Nonnull final String guildId, @Nonnull final String emojiId) {
        return getCatnip().requester().queue(
                new OutboundRequest(
                        Routes.DELETE_GUILD_EMOJI.withMajorParam(guildId),
                        ImmutableMap.of("emoji.id", emojiId), null
                ))
                .thenApply(__ -> null);
    }
}
