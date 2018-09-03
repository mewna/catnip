package com.mewna.catnip.rest.handler;

import com.google.common.collect.ImmutableMap;
import com.mewna.catnip.entity.Role;
import com.mewna.catnip.internal.CatnipImpl;
import com.mewna.catnip.rest.ResponsePayload;
import com.mewna.catnip.rest.RestRequester.OutboundRequest;
import com.mewna.catnip.rest.Routes;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author amy
 * @since 9/3/18.
 */
@SuppressWarnings("unused")
public class RestGuild extends RestHandler {
    private final CatnipImpl catnip;
    
    public RestGuild(final CatnipImpl catnip) {
        this.catnip = catnip;
    }
    
    @Nonnull
    @CheckReturnValue
    public CompletableFuture<List<Role>> getGuildRoles(@Nonnull final String guildId) {
        return catnip.requester()
                .queue(new OutboundRequest(Routes.GET_GUILD_ROLES.withMajorParam(guildId),
                        ImmutableMap.of(), null))
                .thenApply(ResponsePayload::array)
                .thenApply(mapObjectContents(getEntityBuilder()::createRole));
    }
}
