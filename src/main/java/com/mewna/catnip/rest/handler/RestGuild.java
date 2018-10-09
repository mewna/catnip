package com.mewna.catnip.rest.handler;

import com.google.common.collect.ImmutableMap;
import com.mewna.catnip.entity.channel.GuildChannel;
import com.mewna.catnip.entity.channel.Webhook;
import com.mewna.catnip.entity.guild.Guild;
import com.mewna.catnip.entity.guild.Guild.GuildEditFields;
import com.mewna.catnip.entity.guild.GuildBan;
import com.mewna.catnip.entity.guild.Member;
import com.mewna.catnip.entity.guild.Role;
import com.mewna.catnip.entity.guild.audit.ActionType;
import com.mewna.catnip.entity.guild.audit.AuditLogEntry;
import com.mewna.catnip.entity.impl.EntityBuilder;
import com.mewna.catnip.entity.misc.CreatedInvite;
import com.mewna.catnip.entity.misc.VoiceRegion;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.internal.CatnipImpl;
import com.mewna.catnip.rest.ResponsePayload;
import com.mewna.catnip.rest.RestRequester.OutboundRequest;
import com.mewna.catnip.rest.Routes;
import com.mewna.catnip.rest.guild.PartialGuild;
import com.mewna.catnip.util.Paginator;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

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
    
    @Nonnull
    public CompletableFuture<Void> removeGuildMemberRole(@Nonnull final String guildId, @Nonnull final String userId,
                                                         @Nonnull final String roleId) {
        return getCatnip().requester().queue(new OutboundRequest(Routes.REMOVE_GUILD_MEMBER_ROLE.withMajorParam(guildId),
                ImmutableMap.of("user.id", userId, "role.id", roleId), null))
                .thenApply(e -> null);
    }
    
    @Nonnull
    public CompletableFuture<Void> addGuildMemberRole(@Nonnull final String guildId, @Nonnull final String userId,
                                                      @Nonnull final String roleId) {
        return getCatnip().requester().queue(new OutboundRequest(Routes.ADD_GUILD_MEMBER_ROLE.withMajorParam(guildId),
                ImmutableMap.of("user.id", userId, "role.id", roleId), null))
                .thenApply(e -> null);
    }
    
    @Nonnull
    @CheckReturnValue
    public CompletableFuture<Integer> getGuildPruneCount(@Nonnull final String guildId, @Nonnegative final int days) {
        return getCatnip().requester().queue(new OutboundRequest(Routes.GET_GUILD_PRUNE_COUNT.withMajorParam(guildId).withQueryString("?days=" + days),
                ImmutableMap.of(), null))
                .thenApply(ResponsePayload::object)
                .thenApply(e -> e.getInteger("pruned"));
    }
    
    @Nonnull
    @CheckReturnValue
    public CompletableFuture<Integer> beginGuildPrune(@Nonnull final String guildId, @Nonnegative final int days) {
        return getCatnip().requester().queue(new OutboundRequest(Routes.BEGIN_GUILD_PRUNE.withMajorParam(guildId).withQueryString("?days=" + days),
                ImmutableMap.of(), null))
                .thenApply(ResponsePayload::object)
                .thenApply(e -> e.getInteger("pruned"));
    }
    
    @Nonnull
    @CheckReturnValue
    public CompletableFuture<List<VoiceRegion>> getGuildVoiceRegions(@Nonnull final String guildId) {
        return getCatnip().requester().queue(new OutboundRequest(Routes.GET_GUILD_VOICE_REGIONS.withQueryString(guildId),
                ImmutableMap.of(), null))
                .thenApply(ResponsePayload::array)
                .thenApply(mapObjectContents(getEntityBuilder()::createVoiceRegion));
    }
    
    public Paginator<AuditLogEntry> getAuditLog(@Nonnull final String guildId) {
        //discord provides audit logs in a stupid format so we have to do this
        return new Paginator<AuditLogEntry>(
                __ -> {
                    throw new UnsupportedOperationException("Should not get called since fetch() is overriden");
                },
                (__, ___) -> {
                    throw new UnsupportedOperationException("Should not get called since fetch() is overriden");
                },
                __ -> {
                    throw new UnsupportedOperationException("Should not get called since fetch() is overriden");
                },
                100
        ) {
            @Nonnull
            @Override
            protected CompletionStage<Void> fetch(@Nullable final String id, @Nonnull final AtomicInteger fetched, @Nonnull final Consumer<AuditLogEntry> action, final int limit, final int requestSize) {
                return getAuditLogRaw(guildId, null, null, null, Math.min(requestSize, limit - fetched.get())).thenCompose(logs -> {
                    AuditLogEntry last = null;
                    
                    //inlined EntityBuilder.immutableListOf and EntityBuilder.createAuditLog
                    //this is done so we can do less allocations and only parse the entries
                    //we need to.
                    final EntityBuilder builder = getEntityBuilder();
                    final Map<String, Webhook> webhooks = EntityBuilder.immutableMapOf(logs.getJsonArray("webhooks"), x -> x.getString("id"), builder::createWebhook);
                    final Map<String, User> users = EntityBuilder.immutableMapOf(logs.getJsonArray("users"), x -> x.getString("id"), builder::createUser);
                    final JsonArray entries = logs.getJsonArray("audit_log_entries");
                    
                    for(final Object object : entries) {
                        if(!(object instanceof JsonObject)) {
                            throw new IllegalArgumentException("Expected all values to be JsonObjects, but found " +
                                    (object == null ? "null" : object.getClass()));
                        }
                        last = builder.createAuditLogEntry((JsonObject)object, webhooks, users);
                        action.accept(last);
                        if(fetched.incrementAndGet() == limit) {
                            return CompletableFuture.completedFuture(null);
                        }
                    }
                    if(entries.size() < requestSize || last == null) {
                        return CompletableFuture.completedFuture(null);
                    }
                    return fetch(last.id(), fetched, action, limit, requestSize);
                });
            }
        };
    }
    
    @Nonnull
    @CheckReturnValue
    public CompletableFuture<List<AuditLogEntry>> getAuditLog(@Nonnull final String guildId, @Nullable final String userId,
                                                         @Nullable final String beforeEntryId, @Nullable final ActionType type,
                                                         @Nonnegative final int limit) {
        return getAuditLogRaw(guildId, userId, beforeEntryId, type, limit)
                .thenApply(getEntityBuilder()::createAuditLog);
    }
    
    //TODO make public when we add raw methods for the other routes
    //keeping this private for consistency with the rest of the methods
    @Nonnull
    @CheckReturnValue
    private CompletableFuture<JsonObject> getAuditLogRaw(@Nonnull final String guildId, @Nullable final String userId,
                                                         @Nullable final String beforeEntryId, @Nullable final ActionType type,
                                                         @Nonnegative final int limit) {
        final Collection<String> params = new ArrayList<>();
        if (userId != null) {
            params.add("user_id=" + userId);
        }
        if (beforeEntryId != null) {
            params.add("before=" + beforeEntryId);
        }
        if (limit <= 100) {
            params.add("limit=" + limit);
        }
        if (type != null) {
            params.add("action_type=" + type);
        }
        String query = String.join("&", params);
        if (!query.isEmpty()) {
            query = '?' + query;
        }
        return getCatnip().requester().queue(new OutboundRequest(Routes.GET_GUILD_AUDIT_LOG.withMajorParam(guildId).withQueryString(query),
                ImmutableMap.of(), null))
                .thenApply(ResponsePayload::object);
    }
}
