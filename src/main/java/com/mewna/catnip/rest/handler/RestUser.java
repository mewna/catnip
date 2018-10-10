package com.mewna.catnip.rest.handler;

import com.google.common.collect.ImmutableMap;
import com.mewna.catnip.entity.channel.DMChannel;
import com.mewna.catnip.entity.guild.PartialGuild;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.internal.CatnipImpl;
import com.mewna.catnip.rest.ResponsePayload;
import com.mewna.catnip.rest.RestRequester.OutboundRequest;
import com.mewna.catnip.rest.Routes;
import com.mewna.catnip.util.Utils;
import com.mewna.catnip.util.pagination.GuildPaginator;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author amy
 * @since 9/3/18.
 */
@SuppressWarnings("unused")
public class RestUser extends RestHandler {
    public RestUser(final CatnipImpl catnip) {
        super(catnip);
    }
    
    @Nonnull
    @CheckReturnValue
    public CompletableFuture<User> getCurrentUser() {
        return getCatnip().requester().queue(new OutboundRequest(Routes.GET_CURRENT_USER,
                ImmutableMap.of(), null))
                .thenApply(ResponsePayload::object)
                .thenApply(getEntityBuilder()::createUser);
    }
    
    @Nonnull
    @CheckReturnValue
    public CompletableFuture<User> getUser(@Nonnull final String userId) {
        return getCatnip().requester().queue(new OutboundRequest(Routes.GET_USER,
                ImmutableMap.of("user.id", userId), null))
                .thenApply(ResponsePayload::object)
                .thenApply(getEntityBuilder()::createUser);
    }
    
    @Nonnull
    public CompletableFuture<User> modifyCurrentUser(@Nullable final String username, @Nullable final URI avatarData) {
        final JsonObject body = new JsonObject();
        if(avatarData != null) {
            Utils.validateImageUri(avatarData);
            body.put("avatar", avatarData.toString());
        }
        body.put("username", username);
        return getCatnip().requester().queue(new OutboundRequest(Routes.MODIFY_CURRENT_USER,
                ImmutableMap.of(), body))
                .thenApply(ResponsePayload::object)
                .thenApply(getEntityBuilder()::createUser);
    }
    
    @Nonnull
    public CompletableFuture<User> modifyCurrentUser(@Nullable final String username, @Nullable final byte[] avatar) {
        return modifyCurrentUser(username, avatar == null ? null : Utils.asImageDataUri(avatar));
    }
    
    @Nonnull
    @CheckReturnValue
    public GuildPaginator getCurrentUserGuilds() {
        return new GuildPaginator(getEntityBuilder()) {
            @Nonnull
            @Override
            protected CompletionStage<JsonArray> fetchNext(@Nonnull final RequestState<PartialGuild> state, @Nullable final String lastId, final int requestSize) {
                return getCurrentUserGuildsRaw(null, lastId, state.entitiesToFetch());
            }
        };
    }
    
    @Nonnull
    @CheckReturnValue
    public CompletableFuture<List<PartialGuild>> getCurrentUserGuilds(@Nullable final String before, @Nullable final String after,
                                                                      @Nonnegative final int limit) {
        return getCurrentUserGuildsRaw(before, after, limit)
                .thenApply(mapObjectContents(getEntityBuilder()::createPartialGuild));
    }
    
    //TODO make public when we add raw methods for the other routes
    //keeping this private for consistency with the rest of the methods
    @Nonnull
    @CheckReturnValue
    private CompletableFuture<JsonArray> getCurrentUserGuildsRaw(@Nullable final String before, @Nullable final String after,
                                                                 @Nonnegative final int limit) {
        final Collection<String> params = new ArrayList<>();
        if (before != null) {
            params.add("before=" + before);
        }
        if (after != null) {
            params.add("before=" + after);
        }
        if (limit <= 100) {
            params.add("limit=" + limit);
        }
        String query = String.join("&", params);
        if (!query.isEmpty()) {
            query = '?' + query;
        }
        return getCatnip().requester().queue(new OutboundRequest(Routes.GET_CURRENT_USER_GUILDS.withQueryString(query),
                ImmutableMap.of(), null))
                .thenApply(ResponsePayload::array);
    }
    
    @Nonnull
    @CheckReturnValue
    public CompletableFuture<DMChannel> createDM(@Nonnull final String recipientId) {
        return getCatnip().requester().queue(new OutboundRequest(Routes.CREATE_DM,
                ImmutableMap.of(), new JsonObject().put("recipient_id", recipientId)))
                .thenApply(ResponsePayload::object)
                .thenApply(getEntityBuilder()::createUserDM);
    }
    
    @Nonnull
    public CompletableFuture<Void> leaveGuild(@Nonnull final String guildId) {
        return getCatnip().requester().queue(new OutboundRequest(Routes.LEAVE_GUILD.withMajorParam(guildId),
                ImmutableMap.of(), null))
                .thenApply(__ -> null);
    }
}
