package com.mewna.catnip.rest.handler;

import com.google.common.collect.ImmutableMap;
import com.mewna.catnip.entity.Guild;
import com.mewna.catnip.entity.Role;
import com.mewna.catnip.internal.CatnipImpl;
import com.mewna.catnip.rest.ResponsePayload;
import com.mewna.catnip.rest.RestRequester.OutboundRequest;
import com.mewna.catnip.rest.Routes;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author amy
 * @since 9/3/18.
 */
@SuppressWarnings("unused")
public class RestGuild extends RestHandler {
    public RestGuild(final CatnipImpl catnip) {
        super(catnip);
    }
    
    @Nonnull
    @CheckReturnValue
    public CompletableFuture<List<Role>> getGuildRoles(@Nonnull final String guildId) {
        return getCatnip().requester()
                .queue(new OutboundRequest(Routes.GET_GUILD_ROLES.withMajorParam(guildId),
                        ImmutableMap.of(), null))
                .thenApply(ResponsePayload::array)
                .thenApply(mapObjectContents(getEntityBuilder()::createRole))
                .thenApply(Collections::unmodifiableList);
    }
    
    @Nonnull
    @CheckReturnValue
    public CompletableFuture<Guild> getGuild(@Nonnull final String guildId) {
        return getCatnip().requester()
                .queue(new OutboundRequest(Routes.GET_GUILD.withMajorParam(guildId),
                        ImmutableMap.of(), null))
                .thenApply(ResponsePayload::object)
                .thenApply(getEntityBuilder()::createGuild);
    }
}
