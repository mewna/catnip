package com.mewna.catnip.rest.handler;

import com.google.common.collect.ImmutableMap;
import com.mewna.catnip.entity.DMChannel;
import com.mewna.catnip.entity.User;
import com.mewna.catnip.internal.CatnipImpl;
import com.mewna.catnip.rest.ResponsePayload;
import com.mewna.catnip.rest.RestRequester.OutboundRequest;
import com.mewna.catnip.rest.Routes;
import io.vertx.core.json.JsonObject;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

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
