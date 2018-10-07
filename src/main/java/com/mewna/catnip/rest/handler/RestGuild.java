package com.mewna.catnip.rest.handler;

import com.google.common.collect.ImmutableMap;
import com.mewna.catnip.entity.channel.GuildChannel;
import com.mewna.catnip.entity.guild.Guild;
import com.mewna.catnip.entity.guild.Guild.GuildEditFields;
import com.mewna.catnip.entity.guild.GuildBan;
import com.mewna.catnip.entity.guild.Member;
import com.mewna.catnip.entity.guild.Role;
import com.mewna.catnip.entity.misc.CreatedInvite;
import com.mewna.catnip.internal.CatnipImpl;
import com.mewna.catnip.rest.ResponsePayload;
import com.mewna.catnip.rest.RestRequester.OutboundRequest;
import com.mewna.catnip.rest.Routes;
import com.mewna.catnip.rest.guild.PartialGuild;
import io.vertx.core.json.JsonObject;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
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
                // All elements are guaranteed to be instances of GuildChannel,
                // so it's safe to do a cast, plus this way we avoid copying the list.
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
    
    @Nonnull
    @CheckReturnValue
    public CompletableFuture<List<Member>> listGuildMembers(@Nonnull final String guildId) {
        return listGuildMembers(guildId, 1, "0");
    }
    
    @Nonnull
    @CheckReturnValue
    public CompletableFuture<List<Member>> listGuildMembers(@Nonnull final String guildId, @Nonnegative final int limit) {
        return listGuildMembers(guildId, limit, "0");
    }
    
    @Nonnull
    @CheckReturnValue
    public CompletableFuture<List<Member>> listGuildMembers(@Nonnull final String guildId, @Nonnull final String after) {
        return listGuildMembers(guildId, 1, after);
    }
    
    @Nonnull
    @CheckReturnValue
    @SuppressWarnings("WeakerAccess")
    public CompletableFuture<List<Member>> listGuildMembers(@Nonnull final String guildId, @Nonnegative final int limit,
                                                            @Nullable final String after) {
        final Collection<String> params = new ArrayList<>();
        if(limit > 0) {
            params.add("limit=" + limit);
        }
        if(after != null && !after.isEmpty()) {
            params.add("after=" + after);
        }
        String query = String.join("&", params);
        if(!query.isEmpty()) {
            query = '?' + query;
        }
        return getCatnip().requester().queue(new OutboundRequest(Routes.LIST_GUILD_MEMBERS.withMajorParam(guildId).withQueryString(query),
                ImmutableMap.of(), null))
                .thenApply(ResponsePayload::array)
                .thenApply(mapObjectContents(o -> getEntityBuilder().createMember(guildId, o)));
    }
    
    @Nonnull
    @CheckReturnValue
    public CompletableFuture<List<GuildBan>> getGuildBans(@Nonnull final String guildId) {
        return getCatnip().requester().queue(new OutboundRequest(Routes.GET_GUILD_BANS.withMajorParam(guildId),
                ImmutableMap.of(), null))
                .thenApply(ResponsePayload::array)
                .thenApply(mapObjectContents(getEntityBuilder()::createGuildBan));
    }
    
    @Nonnull
    @CheckReturnValue
    public CompletableFuture<GuildBan> getGuildBan(@Nonnull final String guildId, @Nonnull final String userId) {
        return getCatnip().requester().queue(new OutboundRequest(Routes.GET_GUILD_BAN.withMajorParam(guildId),
                ImmutableMap.of("user.id", userId), null))
                .thenApply(ResponsePayload::object)
                .thenApply(getEntityBuilder()::createGuildBan);
    }
    
    @Nonnull
    public CompletableFuture<Void> createGuildBan(@Nonnull final String guildId, @Nonnull final String userId,
                                                  @Nullable final String reason,
                                                  @Nonnegative final int deleteMessageDays) {
        final Collection<String> params = new ArrayList<>();
        if(deleteMessageDays <= 7) {
            params.add("delete-message-days=" + deleteMessageDays);
        }
        if(reason != null && !reason.isEmpty()) {
            params.add("reason=" + encodeUTF8(reason));
        }
        String query = String.join("&", params);
        if(!query.isEmpty()) {
            query = '?' + query;
        }
        return getCatnip().requester().queue(new OutboundRequest(Routes.GET_GUILD_BAN.withMajorParam(guildId).withQueryString(query),
                ImmutableMap.of("user.id", userId), null))
                .thenApply(e -> null);
    }
    
    @Nonnull
    public CompletableFuture<Void> removeGuildBan(@Nonnull final String guildId, @Nonnull final String userId) {
        return getCatnip().requester().queue(new OutboundRequest(Routes.GET_GUILD_BAN.withMajorParam(guildId),
                ImmutableMap.of("user.id", userId), null))
                .thenApply(e -> null);
    }
    
    @Nonnull
    public CompletableFuture<String> modifyCurrentUsersNick(@Nonnull final String guildId, @Nullable final String nick) {
        return getCatnip().requester().queue(new OutboundRequest(Routes.MODIFY_CURRENT_USERS_NICK.withQueryString(guildId),
                ImmutableMap.of(), new JsonObject().put("nick", nick)))
                .thenApply(ResponsePayload::string);
    }
    
    @Nonnull
    public CompletableFuture<Void> removeGuildMember(@Nonnull final String guildId, @Nonnull final String userId) {
        return getCatnip().requester().queue(new OutboundRequest(Routes.REMOVE_GUILD_MEMBER.withMajorParam(guildId),
                ImmutableMap.of("user.id", userId), null))
                .thenApply(e -> null);
    }
    
    @Nonnull
    @CheckReturnValue
    public CompletableFuture<Member> getGuildMember(@Nonnull final String guildId, @Nonnull final String userId) {
        return getCatnip().requester().queue(new OutboundRequest(Routes.GET_GUILD_MEMBER.withMajorParam(guildId),
                ImmutableMap.of("user.id", userId), null))
                .thenApply(ResponsePayload::object)
                .thenApply(e -> getEntityBuilder().createMember(guildId, e));
    }
}
