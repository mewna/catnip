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

import com.mewna.catnip.entity.channel.GuildChannel;
import com.mewna.catnip.entity.guild.*;
import com.mewna.catnip.entity.guild.Guild.GuildEditFields;
import com.mewna.catnip.entity.guild.audit.ActionType;
import com.mewna.catnip.entity.guild.audit.AuditLogEntry;
import com.mewna.catnip.entity.misc.CreatedInvite;
import com.mewna.catnip.entity.misc.VoiceRegion;
import com.mewna.catnip.internal.CatnipImpl;
import com.mewna.catnip.rest.ResponsePayload;
import com.mewna.catnip.rest.Routes;
import com.mewna.catnip.rest.guild.*;
import com.mewna.catnip.rest.requester.Requester.OutboundRequest;
import com.mewna.catnip.util.QueryStringBuilder;
import com.mewna.catnip.util.pagination.AuditLogPaginator;
import com.mewna.catnip.util.pagination.MemberPaginator;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.mewna.catnip.util.JsonUtil.mapObjectContents;

/**
 * @author amy
 * @since 9/3/18.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class RestGuild extends RestHandler {
    public RestGuild(final CatnipImpl catnip) {
        super(catnip);
    }
    
    @Nonnull
    @CheckReturnValue
    public Completable modifyGuildMember(@Nonnull final String guildId, @Nonnull final String memberId,
                                         @Nonnull final MemberData data, @Nullable final String reason) {
        return Completable.fromObservable(catnip().requester()
                .queue(new OutboundRequest(Routes.MODIFY_GUILD_MEMBER.withMajorParam(guildId),
                        Map.of("user.id", memberId), data.toJson(), reason)));
    }
    
    @Nonnull
    @CheckReturnValue
    public Completable modifyGuildMember(@Nonnull final String guildId, @Nonnull final String memberId,
                                         @Nonnull final MemberData data) {
        return modifyGuildMember(guildId, memberId, data, null);
    }
    
    @Nonnull
    @CheckReturnValue
    public Completable modifyGuildChannelPositions(@Nonnull final PositionUpdater updater,
                                                   @Nullable final String reason) {
        final JsonArray array = new JsonArray();
        updater.entries()
                .stream()
                .map(x -> new JsonObject().put("id", x.getKey()).put("position", x.getValue()))
                .forEach(array::add);
        return Completable.fromObservable(catnip().requester()
                .queue(new OutboundRequest(Routes.MODIFY_GUILD_CHANNEL_POSITIONS.withMajorParam(updater.guildId()),
                        Map.of(), array, reason)));
    }
    
    @Nonnull
    @CheckReturnValue
    public Completable modifyGuildChannelPositions(@Nonnull final PositionUpdater updater) {
        return modifyGuildChannelPositions(updater, null);
    }
    
    @Nonnull
    @CheckReturnValue
    public Completable modifyGuildRolePositions(@Nonnull final PositionUpdater updater,
                                                @Nullable final String reason) {
        final JsonArray array = new JsonArray();
        updater.entries()
                .stream()
                .map(x -> new JsonObject().put("id", x.getKey()).put("position", x.getValue()))
                .forEach(array::add);
        return Completable.fromObservable(catnip().requester()
                .queue(new OutboundRequest(Routes.MODIFY_GUILD_ROLE_POSITIONS.withMajorParam(updater.guildId()),
                        Map.of(), array, reason)));
    }
    
    @Nonnull
    @CheckReturnValue
    public Completable modifyGuildRolePositions(@Nonnull final PositionUpdater updater) {
        return modifyGuildRolePositions(updater, null);
    }
    
    @Nonnull
    @CheckReturnValue
    public Single<GuildChannel> createGuildChannel(@Nonnull final String guildId, @Nonnull final ChannelData data,
                                                   @Nullable final String reason) {
        return Single.fromObservable(createGuildChannelRaw(guildId, data, reason)
                .map(entityBuilder()::createGuildChannel));
    }
    
    @Nonnull
    @CheckReturnValue
    public Single<GuildChannel> createGuildChannel(@Nonnull final String guildId, @Nonnull final ChannelData data) {
        return createGuildChannel(guildId, data, null);
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<JsonObject> createGuildChannelRaw(@Nonnull final String guildId, @Nonnull final ChannelData data,
                                                        @Nullable final String reason) {
        return catnip().requester()
                .queue(new OutboundRequest(Routes.CREATE_GUILD_CHANNEL.withMajorParam(guildId),
                        Map.of(), data.toJson(), reason))
                .map(ResponsePayload::object);
    }
    
    @Nonnull
    @CheckReturnValue
    public Single<GuildEmbed> getGuildEmbed(@Nonnull final String guildId) {
        return Single.fromObservable(getGuildEmbedRaw(guildId).map(entityBuilder()::createGuildEmbed));
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<JsonObject> getGuildEmbedRaw(@Nonnull final String guildId) {
        return catnip().requester()
                .queue(new OutboundRequest(Routes.GET_GUILD_EMBED.withMajorParam(guildId),
                        Map.of()))
                .map(ResponsePayload::object);
    }
    
    @Nonnull
    @CheckReturnValue
    public Single<GuildEmbed> modifyGuildEmbed(@Nonnull final String guildId, @Nullable final String channelId,
                                               final boolean enabled, @Nullable final String reason) {
        return Single.fromObservable(modifyGuildEmbedRaw(guildId, channelId, enabled, reason)
                .map(entityBuilder()::createGuildEmbed));
    }
    
    @Nonnull
    @CheckReturnValue
    public Single<GuildEmbed> modifyGuildEmbed(@Nonnull final String guildId, @Nullable final String channelId,
                                               final boolean enabled) {
        return modifyGuildEmbed(guildId, channelId, enabled, null);
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<JsonObject> modifyGuildEmbedRaw(@Nonnull final String guildId, @Nullable final String channelId,
                                                      final boolean enabled, @Nullable final String reason) {
        return catnip().requester()
                .queue(new OutboundRequest(Routes.MODIFY_GUILD_EMBED.withMajorParam(guildId),
                        Map.of(), new JsonObject().put("channel_id", channelId).put("enabled", enabled), reason))
                .map(ResponsePayload::object);
    }
    
    @Nonnull
    @CheckReturnValue
    public Single<Role> createGuildRole(@Nonnull final String guildId, @Nonnull final RoleData roleData,
                                        @Nullable final String reason) {
        return Single.fromObservable(createGuildRoleRaw(guildId, roleData, reason)
                .map(obj -> entityBuilder().createRole(guildId, obj)));
    }
    
    @Nonnull
    @CheckReturnValue
    public Single<Role> createGuildRole(@Nonnull final String guildId, @Nonnull final RoleData roleData) {
        return createGuildRole(guildId, roleData, null);
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<JsonObject> createGuildRoleRaw(@Nonnull final String guildId, @Nonnull final RoleData roleData,
                                                     @Nullable final String reason) {
        return catnip().requester()
                .queue(new OutboundRequest(Routes.CREATE_GUILD_ROLE.withMajorParam(guildId),
                        Map.of(), roleData.toJson(), reason))
                .map(ResponsePayload::object);
    }
    
    @Nonnull
    @CheckReturnValue
    public Single<Role> modifyGuildRole(@Nonnull final String guildId, @Nonnull final String roleId,
                                        @Nonnull final RoleData roleData, @Nullable final String reason) {
        return Single.fromObservable(modifyGuildRoleRaw(guildId, roleId, roleData, reason)
                .map(obj -> entityBuilder().createRole(guildId, obj)));
    }
    
    public Single<Role> modifyGuildRole(@Nonnull final String guildId, @Nonnull final String roleId,
                                        @Nonnull final RoleData roleData) {
        return modifyGuildRole(guildId, roleId, roleData, null);
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<JsonObject> modifyGuildRoleRaw(@Nonnull final String guildId, @Nonnull final String roleId,
                                                     @Nonnull final RoleData roleData, @Nullable final String reason) {
        return catnip().requester()
                .queue(new OutboundRequest(Routes.MODIFY_GUILD_ROLE.withMajorParam(guildId),
                        Map.of("role.id", roleId), roleData.toJson(), reason))
                .map(ResponsePayload::object);
    }
    
    @Nonnull
    @CheckReturnValue
    public Completable deleteGuildRole(@Nonnull final String guildId, @Nonnull final String roleId,
                                       @Nullable final String reason) {
        return Completable.fromObservable(catnip().requester()
                .queue(new OutboundRequest(Routes.DELETE_GUILD_ROLE.withMajorParam(guildId),
                        Map.of("role.id", roleId)).reason(reason)));
    }
    
    @Nonnull
    @CheckReturnValue
    public Completable deleteGuildRole(@Nonnull final String guildId, @Nonnull final String roleId) {
        return deleteGuildRole(guildId, roleId, null);
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<Role> getGuildRoles(@Nonnull final String guildId) {
        return getGuildRolesRaw(guildId)
                .map(f -> mapObjectContents(e -> entityBuilder().createRole(guildId, e)).apply(f))
                .flatMapIterable(e -> e);
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<JsonArray> getGuildRolesRaw(@Nonnull final String guildId) {
        return catnip().requester()
                .queue(new OutboundRequest(Routes.GET_GUILD_ROLES.withMajorParam(guildId),
                        Map.of()))
                .map(ResponsePayload::array);
    }
    
    @Nonnull
    @CheckReturnValue
    public Single<Guild> getGuild(@Nonnull final String guildId) {
        return Single.fromObservable(getGuildRaw(guildId).map(entityBuilder()::createGuild));
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<JsonObject> getGuildRaw(@Nonnull final String guildId) {
        return catnip().requester()
                .queue(new OutboundRequest(Routes.GET_GUILD.withMajorParam(guildId),
                        Map.of()))
                .map(ResponsePayload::object);
    }
    
    @Nonnull
    @CheckReturnValue
    public Single<Guild> createGuild(@Nonnull final GuildData guild) {
        return Single.fromObservable(createGuildRaw(guild).map(entityBuilder()::createGuild));
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<JsonObject> createGuildRaw(@Nonnull final GuildData guild) {
        return catnip().requester()
                .queue(new OutboundRequest(Routes.CREATE_GUILD,
                        Map.of(), guild.toJson()))
                .map(ResponsePayload::object);
    }
    
    @Nonnull
    public Completable deleteGuild(@Nonnull final String guildId) {
        return Completable.fromObservable(catnip().requester()
                .queue(new OutboundRequest(Routes.DELETE_GUILD.withMajorParam(guildId),
                        Map.of())));
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<GuildChannel> getGuildChannels(@Nonnull final String guildId) {
        return getGuildChannelsRaw(guildId)
                .map(e -> mapObjectContents(entityBuilder()::createChannel).apply(e))
                .flatMapIterable(e -> e)
                // All elements are guaranteed to be instances of GuildChannel,
                // so it's safe to do a cast, plus this way we avoid copying the list.
                .map(RestHandler::uncheckedCast);
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<JsonArray> getGuildChannelsRaw(@Nonnull final String guildId) {
        return catnip().requester()
                .queue(new OutboundRequest(Routes.GET_GUILD_CHANNELS.withMajorParam(guildId),
                        Map.of()))
                .map(ResponsePayload::array);
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<CreatedInvite> getGuildInvites(@Nonnull final String guildId) {
        return getGuildInvitesRaw(guildId)
                .map(e -> mapObjectContents(entityBuilder()::createCreatedInvite).apply(e))
                .flatMapIterable(e -> e);
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<JsonArray> getGuildInvitesRaw(@Nonnull final String guildId) {
        return catnip().requester().queue(new OutboundRequest(Routes.GET_GUILD_INVITES.withMajorParam(guildId),
                Map.of()))
                .map(ResponsePayload::array);
    }
    
    @Nonnull
    public Single<Guild> modifyGuild(@Nonnull final String guildId, @Nonnull final GuildEditFields fields, @Nullable final String reason) {
        return Single.fromObservable(modifyGuildRaw(guildId, fields, reason).map(entityBuilder()::createGuild));
    }
    
    public Single<Guild> modifyGuild(@Nonnull final String guildId, @Nonnull final GuildEditFields fields) {
        return modifyGuild(guildId, fields, null);
    }
    
    @Nonnull
    public Observable<JsonObject> modifyGuildRaw(@Nonnull final String guildId, @Nonnull final GuildEditFields fields,
                                                 @Nullable final String reason) {
        return catnip().requester().queue(new OutboundRequest(Routes.MODIFY_GUILD.withMajorParam(guildId),
                Map.of(), fields.payload(), reason))
                .map(ResponsePayload::object);
    }
    
    @Nonnull
    @CheckReturnValue
    public MemberPaginator listGuildMembers(@Nonnull final String guildId) {
        return new MemberPaginator(entityBuilder(), guildId) {
            @Nonnull
            @Override
            protected Observable<JsonArray> fetchNext(@Nonnull final RequestState<Member> state, @Nullable final String lastId, final int requestSize) {
                return listGuildMembersRaw(guildId, state.entitiesToFetch(), lastId);
            }
        };
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<Member> listGuildMembers(@Nonnull final String guildId, @Nonnegative final int limit,
                                               @Nullable final String after) {
        return listGuildMembersRaw(guildId, limit, after)
                .map(e -> mapObjectContents(o -> entityBuilder().createMember(guildId, o)).apply(e))
                .flatMapIterable(e -> e);
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<JsonArray> listGuildMembersRaw(@Nonnull final String guildId, @Nonnegative final int limit,
                                                     @Nullable final String after) {
        
        final QueryStringBuilder builder = new QueryStringBuilder();
        
        if(limit > 0) {
            builder.append("limit", Integer.toString(limit));
        }
        if(after != null && !after.isEmpty()) {
            builder.append("after", after);
        }
        
        final String query = builder.build();
        
        return catnip().requester().queue(new OutboundRequest(Routes.LIST_GUILD_MEMBERS.withMajorParam(guildId).withQueryString(query),
                Map.of()))
                .map(ResponsePayload::array);
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<GuildBan> getGuildBans(@Nonnull final String guildId) {
        return getGuildBansRaw(guildId)
                .map(e -> mapObjectContents(entityBuilder()::createGuildBan).apply(e))
                .flatMapIterable(e -> e);
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<JsonArray> getGuildBansRaw(@Nonnull final String guildId) {
        return catnip().requester().queue(new OutboundRequest(Routes.GET_GUILD_BANS.withMajorParam(guildId),
                Map.of()))
                .map(ResponsePayload::array);
    }
    
    @Nonnull
    @CheckReturnValue
    public Single<GuildBan> getGuildBan(@Nonnull final String guildId, @Nonnull final String userId) {
        return Single.fromObservable(getGuildBanRaw(guildId, userId).map(entityBuilder()::createGuildBan));
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<JsonObject> getGuildBanRaw(@Nonnull final String guildId, @Nonnull final String userId) {
        return catnip().requester().queue(new OutboundRequest(Routes.GET_GUILD_BAN.withMajorParam(guildId),
                Map.of("user.id", userId)))
                .map(ResponsePayload::object);
    }
    
    @Nonnull
    public Completable createGuildBan(@Nonnull final String guildId, @Nonnull final String userId,
                                      @Nullable final String reason,
                                      @Nonnegative final int deleteMessageDays) {
        if(deleteMessageDays > 7) {
            final CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalArgumentException("deleteMessageDays can't be above 7"));
            return Completable.fromFuture(future);
        }
        
        final QueryStringBuilder builder = new QueryStringBuilder();
        builder.append("reason", reason == null ? "" : reason);
        builder.append("delete-message-days", String.valueOf(deleteMessageDays));
        final String query = builder.build();
        return Completable.fromObservable(catnip().requester()
                .queue(new OutboundRequest(Routes.CREATE_GUILD_BAN.withMajorParam(guildId).withQueryString(query),
                        Map.of("user.id", userId)).reason(reason)));
    }
    
    @Nonnull
    public Completable createGuildBan(@Nonnull final String guildId, @Nonnull final String userId,
                                      @Nonnegative final int deleteMessageDays) {
        return createGuildBan(guildId, userId, null, deleteMessageDays);
    }
    
    @Nonnull
    public Completable removeGuildBan(@Nonnull final String guildId, @Nonnull final String userId,
                                      @Nullable final String reason) {
        return Completable.fromObservable(catnip().requester()
                .queue(new OutboundRequest(Routes.REMOVE_GUILD_BAN.withMajorParam(guildId),
                        Map.of("user.id", userId)).reason(reason)));
    }
    
    @Nonnull
    public Completable removeGuildBan(@Nonnull final String guildId, @Nonnull final String userId) {
        return removeGuildBan(guildId, userId, null);
    }
    
    @Nonnull
    public Single<String> modifyCurrentUsersNick(@Nonnull final String guildId, @Nullable final String nick,
                                                 @Nullable final String reason) {
        return Single.fromObservable(catnip().requester()
                .queue(new OutboundRequest(Routes.MODIFY_CURRENT_USERS_NICK.withQueryString(guildId),
                        Map.of(), new JsonObject().put("nick", nick), reason))
                .map(ResponsePayload::string));
    }
    
    @Nonnull
    public Single<String> modifyCurrentUsersNick(@Nonnull final String guildId, @Nullable final String nick) {
        return modifyCurrentUsersNick(guildId, nick, null);
    }
    
    @Nonnull
    public Completable removeGuildMember(@Nonnull final String guildId, @Nonnull final String userId,
                                         @Nullable final String reason) {
        return Completable.fromObservable(catnip().requester()
                .queue(new OutboundRequest(Routes.REMOVE_GUILD_MEMBER.withMajorParam(guildId),
                        Map.of("user.id", userId)).reason(reason)));
    }
    
    @Nonnull
    public Completable removeGuildMember(@Nonnull final String guildId, @Nonnull final String userId) {
        return removeGuildMember(guildId, userId, null);
    }
    
    @Nonnull
    @CheckReturnValue
    public Single<Member> getGuildMember(@Nonnull final String guildId, @Nonnull final String userId) {
        return Single.fromObservable(getGuildMemberRaw(guildId, userId)
                .map(e -> entityBuilder().createMember(guildId, e)));
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<JsonObject> getGuildMemberRaw(@Nonnull final String guildId, @Nonnull final String userId) {
        return catnip().requester().queue(new OutboundRequest(Routes.GET_GUILD_MEMBER.withMajorParam(guildId),
                Map.of("user.id", userId)))
                .map(ResponsePayload::object);
    }
    
    @Nonnull
    public Completable removeGuildMemberRole(@Nonnull final String guildId, @Nonnull final String userId,
                                             @Nonnull final String roleId, @Nullable final String reason) {
        return Completable.fromObservable(catnip().requester()
                .queue(new OutboundRequest(Routes.REMOVE_GUILD_MEMBER_ROLE.withMajorParam(guildId),
                        Map.of("user.id", userId, "role.id", roleId)).reason(reason)));
    }
    
    @Nonnull
    public Completable removeGuildMemberRole(@Nonnull final String guildId, @Nonnull final String userId,
                                             @Nonnull final String roleId) {
        return removeGuildMemberRole(guildId, userId, roleId, null);
    }
    
    @Nonnull
    public Completable addGuildMemberRole(@Nonnull final String guildId, @Nonnull final String userId,
                                          @Nonnull final String roleId, @Nullable final String reason) {
        return Completable.fromObservable(catnip().requester()
                .queue(new OutboundRequest(Routes.ADD_GUILD_MEMBER_ROLE.withMajorParam(guildId),
                        Map.of("user.id", userId, "role.id", roleId)).reason(reason)));
    }
    
    @Nonnull
    public Completable addGuildMemberRole(@Nonnull final String guildId, @Nonnull final String userId,
                                          @Nonnull final String roleId) {
        return addGuildMemberRole(guildId, userId, roleId, null);
    }
    
    @Nonnull
    @CheckReturnValue
    public Single<Integer> getGuildPruneCount(@Nonnull final String guildId, @Nonnegative final int days) {
        return Single.fromObservable(getGuildPruneCountRaw(guildId, days).map(e -> e.getInteger("pruned")));
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<JsonObject> getGuildPruneCountRaw(@Nonnull final String guildId, @Nonnegative final int days) {
        final String query = new QueryStringBuilder().append("days", Integer.toString(days)).build();
        return catnip().requester().queue(new OutboundRequest(Routes.GET_GUILD_PRUNE_COUNT
                .withMajorParam(guildId).withQueryString(query), Map.of()))
                .map(ResponsePayload::object);
    }
    
    @Nonnull
    @CheckReturnValue
    public Single<Integer> beginGuildPrune(@Nonnull final String guildId, @Nonnegative final int days) {
        return Single.fromObservable(beginGuildPruneRaw(guildId, days).map(e -> e.getInteger("pruned")));
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<JsonObject> beginGuildPruneRaw(@Nonnull final String guildId, @Nonnegative final int days) {
        final String query = new QueryStringBuilder().append("days", Integer.toString(days)).build();
        
        return catnip().requester().queue(new OutboundRequest(Routes.BEGIN_GUILD_PRUNE
                .withMajorParam(guildId).withQueryString(query), Map.of()))
                .map(ResponsePayload::object);
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<VoiceRegion> getGuildVoiceRegions(@Nonnull final String guildId) {
        return getGuildVoiceRegionsRaw(guildId)
                .map(e -> mapObjectContents(entityBuilder()::createVoiceRegion).apply(e))
                .flatMapIterable(e -> e);
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<JsonArray> getGuildVoiceRegionsRaw(@Nonnull final String guildId) {
        return catnip().requester().queue(new OutboundRequest(Routes.GET_GUILD_VOICE_REGIONS.withQueryString(guildId),
                Map.of()))
                .map(ResponsePayload::array);
    }
    
    public AuditLogPaginator getGuildAuditLog(@Nonnull final String guildId) {
        return new AuditLogPaginator(entityBuilder()) {
            @Nonnull
            @CheckReturnValue
            @Override
            protected Observable<JsonObject> fetchNext(@Nonnull final RequestState<AuditLogEntry> state, @Nullable final String lastId,
                                                       @Nonnegative final int requestSize) {
                return getGuildAuditLogRaw(guildId, state.extra("user"), lastId, state.extra("type"), requestSize);
            }
        };
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<AuditLogEntry> getGuildAuditLog(@Nonnull final String guildId, @Nullable final String userId,
                                                      @Nullable final String beforeEntryId, @Nullable final ActionType type,
                                                      @Nonnegative final int limit) {
        return getGuildAuditLogRaw(guildId, userId, beforeEntryId, type, limit)
                .map(entityBuilder()::createAuditLog)
                .flatMapIterable(e -> e);
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<JsonObject> getGuildAuditLogRaw(@Nonnull final String guildId, @Nullable final String userId,
                                                      @Nullable final String beforeEntryId, @Nullable final ActionType type,
                                                      @Nonnegative final int limit) {
        
        final QueryStringBuilder builder = new QueryStringBuilder();
        
        if(userId != null) {
            builder.append("user_id", userId);
        }
        
        if(beforeEntryId != null) {
            builder.append("before", beforeEntryId);
        }
        
        if(limit <= 100 && limit >= 1) {
            builder.append("limit", Integer.toString(limit));
        }
        
        if(type != null) {
            builder.append("action_type", type.toString());
        }
        
        final String query = builder.build();
        return catnip().requester().queue(new OutboundRequest(Routes.GET_GUILD_AUDIT_LOG.withMajorParam(guildId).withQueryString(query),
                Map.of()))
                .map(ResponsePayload::object);
    }
}
