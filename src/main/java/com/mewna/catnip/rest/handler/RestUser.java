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

import com.mewna.catnip.entity.channel.DMChannel;
import com.mewna.catnip.entity.guild.PartialGuild;
import com.mewna.catnip.entity.misc.ApplicationInfo;
import com.mewna.catnip.entity.misc.GatewayInfo;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.internal.CatnipImpl;
import com.mewna.catnip.rest.ResponsePayload;
import com.mewna.catnip.rest.Routes;
import com.mewna.catnip.rest.requester.Requester.OutboundRequest;
import com.mewna.catnip.util.QueryStringBuilder;
import com.mewna.catnip.util.Utils;
import com.mewna.catnip.util.pagination.GuildPaginator;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URI;
import java.util.Map;

import static com.mewna.catnip.util.JsonUtil.mapObjectContents;

/**
 * @author amy
 * @since 9/3/18.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class RestUser extends RestHandler {
    public RestUser(final CatnipImpl catnip) {
        super(catnip);
    }
    
    @Nonnull
    @CheckReturnValue
    public Single<User> getCurrentUser() {
        return Single.fromObservable(getCurrentUserRaw().map(entityBuilder()::createUser));
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<JsonObject> getCurrentUserRaw() {
        return catnip().requester().queue(new OutboundRequest(Routes.GET_CURRENT_USER, Map.of()))
                .map(ResponsePayload::object);
    }
    
    @Nonnull
    @CheckReturnValue
    public Single<User> getUser(@Nonnull final String userId) {
        return Single.fromObservable(getUserRaw(userId).map(entityBuilder()::createUser));
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<JsonObject> getUserRaw(@Nonnull final String userId) {
        return catnip().requester().queue(new OutboundRequest(Routes.GET_USER,
                Map.of("user.id", userId)))
                .map(ResponsePayload::object);
    }
    
    @Nonnull
    public Single<User> modifyCurrentUser(@Nullable final String username, @Nullable final URI avatarData) {
        return Single.fromObservable(modifyCurrentUserRaw(username, avatarData).map(entityBuilder()::createUser));
    }
    
    @Nonnull
    public Observable<JsonObject> modifyCurrentUserRaw(@Nullable final String username, @Nullable final URI avatarData) {
        final JsonObject body = new JsonObject();
        if(avatarData != null) {
            Utils.validateImageUri(avatarData);
            body.put("avatar", avatarData.toString());
        }
        body.put("username", username);
        
        return catnip().requester().queue(new OutboundRequest(Routes.MODIFY_CURRENT_USER,
                Map.of(), body))
                .map(ResponsePayload::object);
    }
    
    @Nonnull
    public Single<User> modifyCurrentUser(@Nullable final String username, @Nullable final byte[] avatar) {
        return modifyCurrentUser(username, avatar == null ? null : Utils.asImageDataUri(avatar));
    }
    
    @Nonnull
    @CheckReturnValue
    public GuildPaginator getCurrentUserGuilds() {
        return new GuildPaginator(entityBuilder()) {
            @Nonnull
            @Override
            protected Observable<JsonArray> fetchNext(@Nonnull final RequestState<PartialGuild> state, @Nullable final String lastId, final int requestSize) {
                return getCurrentUserGuildsRaw(null, lastId, state.entitiesToFetch());
            }
        };
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<PartialGuild> getCurrentUserGuilds(@Nullable final String before, @Nullable final String after,
                                                         @Nonnegative final int limit) {
        return getCurrentUserGuildsRaw(before, after, limit)
                .map(e -> mapObjectContents(entityBuilder()::createPartialGuild).apply(e))
                .flatMapIterable(e -> e);
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<JsonArray> getCurrentUserGuildsRaw(@Nullable final String before, @Nullable final String after,
                                                         @Nonnegative final int limit) {
        final QueryStringBuilder builder = new QueryStringBuilder();
        
        if(before != null) {
            builder.append("before", before);
        }
        
        if(after != null) {
            builder.append("before", after);
        }
        
        if(limit <= 100) {
            builder.append("limit", Integer.toString(limit));
        }
        final String query = builder.build();
        
        return catnip().requester().queue(new OutboundRequest(Routes.GET_CURRENT_USER_GUILDS.withQueryString(query),
                Map.of()))
                .map(ResponsePayload::array);
    }
    
    @Nonnull
    @CheckReturnValue
    public Single<DMChannel> createDM(@Nonnull final String recipientId) {
        return Single.fromObservable(createDMRaw(recipientId)
                .map(entityBuilder()::createUserDM));
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<JsonObject> createDMRaw(@Nonnull final String recipientId) {
        return catnip().requester().queue(new OutboundRequest(Routes.CREATE_DM, Map.of(),
                new JsonObject().put("recipient_id", recipientId)))
                .map(ResponsePayload::object);
    }
    
    @Nonnull
    public Completable leaveGuild(@Nonnull final String guildId) {
        return Completable.fromObservable(catnip().requester()
                .queue(new OutboundRequest(Routes.LEAVE_GUILD,
                        Map.of("guild.id", guildId))));
    }
    
    @Nonnull
    @CheckReturnValue
    public Single<ApplicationInfo> getCurrentApplicationInformation() {
        return Single.fromObservable(getCurrentApplicationInformationRaw().map(entityBuilder()::createApplicationInfo));
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<JsonObject> getCurrentApplicationInformationRaw() {
        return catnip().requester().queue(new OutboundRequest(Routes.GET_CURRENT_APPLICATION_INFORMATION,
                Map.of()))
                .map(ResponsePayload::object);
    }
    
    @Nonnull
    @CheckReturnValue
    public Single<GatewayInfo> getGatewayBot() {
        return Single.fromObservable(getGatewayBotRaw().map(e -> entityBuilder().createGatewayInfo(e)));
    }
    
    @Nonnull
    @CheckReturnValue
    public Observable<JsonObject> getGatewayBotRaw() {
        return catnip().requester().queue(new OutboundRequest(Routes.GET_GATEWAY_BOT, Map.of()))
                .map(ResponsePayload::object);
    }
}
