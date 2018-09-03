package com.mewna.catnip.rest.handler;

import com.google.common.collect.ImmutableMap;
import com.mewna.catnip.entity.User;
import com.mewna.catnip.internal.CatnipImpl;
import com.mewna.catnip.rest.ResponsePayload;
import com.mewna.catnip.rest.RestRequester.OutboundRequest;
import com.mewna.catnip.rest.Routes;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

/**
 * @author amy
 * @since 9/3/18.
 */
@SuppressWarnings("unused")
public class RestUser extends RestHandler {
    private final CatnipImpl catnip;
    
    public RestUser(final CatnipImpl catnip) {
        this.catnip = catnip;
    }
    
    @Nonnull
    @CheckReturnValue
    public CompletableFuture<User> getUser(@Nonnull final String userId) {
        return catnip.requester().queue(new OutboundRequest(Routes.GET_USER,
                ImmutableMap.of("user.id", userId), null))
                .thenApply(ResponsePayload::object)
                .thenApply(getEntityBuilder()::createUser);
    }
}
