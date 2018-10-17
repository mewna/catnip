package com.mewna.catnip.rest.handler;

import com.google.common.collect.ImmutableMap;
import com.mewna.catnip.entity.guild.Invite;
import com.mewna.catnip.internal.CatnipImpl;
import com.mewna.catnip.rest.ResponsePayload;
import com.mewna.catnip.rest.RestRequester.OutboundRequest;
import com.mewna.catnip.rest.Routes;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.concurrent.CompletionStage;

/**
 * @author natanbc
 * @since 9/14/18
 */
public class RestInvite extends RestHandler {
    public RestInvite(final CatnipImpl catnip) {
        super(catnip);
    }
    
    @Nonnull
    @CheckReturnValue
    public CompletionStage<Invite> getInvite(@Nonnull final String code) {
        return getCatnip().requester().queue(new OutboundRequest(Routes.GET_INVITE,
                ImmutableMap.of("invite.code", code), null))
                .thenApply(ResponsePayload::object)
                .thenApply(getEntityBuilder()::createInvite);
    }
    
    @Nonnull
    @CheckReturnValue
    public CompletionStage<Invite> deleteInvite(@Nonnull final String code) {
        return getCatnip().requester().queue(new OutboundRequest(Routes.DELETE_INVITE,
                ImmutableMap.of("invite.code", code), null))
                .thenApply(ResponsePayload::object)
                .thenApply(getEntityBuilder()::createInvite);
    }
}
