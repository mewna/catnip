package com.mewna.catnip.rest.handler;

import com.google.common.collect.ImmutableMap;
import com.mewna.catnip.entity.guild.Guild;
import com.mewna.catnip.entity.guild.Guild.GuildEditFields;
import com.mewna.catnip.entity.channel.GuildChannel;
import com.mewna.catnip.entity.guild.Role;
import com.mewna.catnip.entity.misc.CreatedInvite;
import com.mewna.catnip.internal.CatnipImpl;
import com.mewna.catnip.rest.ResponsePayload;
import com.mewna.catnip.rest.RestRequester.OutboundRequest;
import com.mewna.catnip.rest.Routes;
import com.mewna.catnip.rest.guild.PartialGuild;

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
                .thenApply(mapObjectContents(e -> getEntityBuilder().createRole(guildId, e)))
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
    
    @Nonnull
    @CheckReturnValue
    public CompletableFuture<Guild> createGuild(@Nonnull final PartialGuild guild) {
        return getCatnip().requester()
                .queue(new OutboundRequest(Routes.CREATE_GUILD,
                        ImmutableMap.of(), guild.toJson()))
                .thenApply(ResponsePayload::object)
                .thenApply(getEntityBuilder()::createGuild);
    }
    
    @Nonnull
    public CompletableFuture<Void> deleteGuild(@Nonnull final String guildId) {
        return getCatnip().requester()
                .queue(new OutboundRequest(Routes.DELETE_GUILD.withMajorParam(guildId),
                        ImmutableMap.of(), null))
                .thenApply(__ -> null);
    }
    
    @Nonnull
    @CheckReturnValue
    public CompletableFuture<List<GuildChannel>> getGuildChannels(@Nonnull final String guildId) {
        return getCatnip().requester()
                .queue(new OutboundRequest(Routes.GET_GUILD_CHANNELS.withMajorParam(guildId),
                        ImmutableMap.of(), null))
                .thenApply(ResponsePayload::array)
                .thenApply(mapObjectContents(getEntityBuilder()::createChannel))
                //all elements are guaranteed to be instances of GuildChannel,
                //so it's safe to do a cast, plus this way we avoid copying the list.
                .thenApply(RestHandler::uncheckedCast);
    }
    
    @Nonnull
    @CheckReturnValue
    public CompletableFuture<List<CreatedInvite>> getGuildInvites(@Nonnull final String guildId) {
        return getCatnip().requester().queue(new OutboundRequest(Routes.GET_GUILD_INVITES.withMajorParam(guildId),
                ImmutableMap.of(), null))
                .thenApply(ResponsePayload::array)
                .thenApply(mapObjectContents(getEntityBuilder()::createCreatedInvite));
    }
    
    @Nonnull
    public CompletableFuture<Guild> modifyGuild(@Nonnull final String guildId, @Nonnull final GuildEditFields fields) {
        return getCatnip().requester().queue(new OutboundRequest(Routes.MODIFY_GUILD.withMajorParam(guildId),
                ImmutableMap.of(), fields.payload()))
                .thenApply(ResponsePayload::object)
                .thenApply(getEntityBuilder()::createGuild);
    }
}
