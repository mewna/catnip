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

package com.mewna.catnip.rest;

import lombok.Getter;
import lombok.experimental.Accessors;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.mewna.catnip.rest.Routes.HttpMethod.*;

/**
 * @author amy
 * @since 8/31/18.
 */
// This is a necessary suppression, fuck off IJ
@SuppressWarnings({"StaticVariableOfConcreteClass", "WeakerAccess", "unused", "RedundantSuppression"})
public final class Routes {
    // @formatter:off
    public static final Route GET_GATEWAY_BOT                     = new Route(GET,    "/gateway/bot");
    public static final Route DELETE_CHANNEL                      = new Route(DELETE, "/channels/:channel", "channel");
    public static final Route GET_CHANNEL                         = new Route(GET,    "/channels/:channel", "channel");
    public static final Route MODIFY_CHANNEL                      = new Route(PATCH,  "/channels/:channel", "channel");
    public static final Route GET_CHANNEL_INVITES                 = new Route(GET,    "/channels/:channel/invites", "channel");
    public static final Route CREATE_CHANNEL_INVITE               = new Route(POST,   "/channels/:channel/invites", "channel");
    public static final Route GET_CHANNEL_MESSAGES                = new Route(GET,    "/channels/:channel/messages", "channel");
    public static final Route CREATE_MESSAGE                      = new Route(POST,   "/channels/:channel/messages", "channel");
    public static final Route BULK_DELETE_MESSAGES                = new Route(POST,   "/channels/:channel/messages/bulk-delete", "channel");
    public static final Route DELETE_MESSAGE                      = new Route(DELETE, "/channels/:channel/messages/:message", "channel");
    public static final Route GET_CHANNEL_MESSAGE                 = new Route(GET,    "/channels/:channel/messages/:message", "channel");
    public static final Route EDIT_MESSAGE                        = new Route(PATCH,  "/channels/:channel/messages/:message", "channel");
    public static final Route DELETE_ALL_REACTIONS                = new Route(DELETE, "/channels/:channel/messages/:message/reactions", "channel");
    public static final Route DELETE_EMOJI_REACTIONS              = new Route(DELETE, "/channels/:channel/messages/:message/reactions/:emojis", "channel");
    public static final Route GET_REACTIONS                       = new Route(GET,    "/channels/:channel/messages/:message/reactions/:emojis", "channel");
    public static final Route DELETE_OWN_REACTION                 = new Route(DELETE, "/channels/:channel/messages/:message/reactions/:emojis/@me", "channel");
    public static final Route CREATE_REACTION                     = new Route(PUT,    "/channels/:channel/messages/:message/reactions/:emojis/@me", "channel", true);
    public static final Route DELETE_USER_REACTION                = new Route(DELETE, "/channels/:channel/messages/:message/reactions/:emojis/:user", "channel");
    public static final Route CROSSPOST_MESSAGE                   = new Route(POST,   "/channels/:channel/messages/:message/crosspost");
    public static final Route DELETE_CHANNEL_PERMISSION           = new Route(DELETE, "/channels/:channel/permissions/:overwrite", "channel");
    public static final Route EDIT_CHANNEL_PERMISSIONS            = new Route(PUT,    "/channels/:channel/permissions/:overwrite", "channel");
    public static final Route GET_PINNED_MESSAGES                 = new Route(GET,    "/channels/:channel/pins", "channel");
    public static final Route DELETE_PINNED_CHANNEL_MESSAGE       = new Route(DELETE, "/channels/:channel/pins/:message", "channel");
    public static final Route ADD_PINNED_CHANNEL_MESSAGE          = new Route(PUT,    "/channels/:channel/pins/:message", "channel");
    public static final Route TRIGGER_TYPING_INDICATOR            = new Route(POST,   "/channels/:channel/typing", "channel");
    public static final Route GET_CHANNEL_WEBHOOKS                = new Route(GET,    "/channels/:channel/webhooks", "channel");
    public static final Route CREATE_WEBHOOK                      = new Route(POST,   "/channels/:channel/webhooks", "channel");
    public static final Route CREATE_GUILD                        = new Route(POST,   "/guilds");
    public static final Route DELETE_GUILD                        = new Route(DELETE, "/guilds/:guild", "guild");
    public static final Route GET_GUILD                           = new Route(GET,    "/guilds/:guild", "guild");
    public static final Route MODIFY_GUILD                        = new Route(PATCH,  "/guilds/:guild", "guild");
    public static final Route GET_GUILD_AUDIT_LOG                 = new Route(GET,    "/guilds/:guild/audit-logs", "guild");
    public static final Route GET_GUILD_BANS                      = new Route(GET,    "/guilds/:guild/bans", "guild");
    public static final Route GET_GUILD_BAN                       = new Route(GET,    "/guilds/:guild/bans/:user", "guild");
    public static final Route REMOVE_GUILD_BAN                    = new Route(DELETE, "/guilds/:guild/bans/:user", "guild");
    public static final Route CREATE_GUILD_BAN                    = new Route(PUT,    "/guilds/:guild/bans/:user", "guild");
    public static final Route GET_GUILD_CHANNELS                  = new Route(GET,    "/guilds/:guild/channels", "guild");
    public static final Route MODIFY_GUILD_CHANNEL_POSITIONS      = new Route(PATCH,  "/guilds/:guild/channels", "guild");
    public static final Route CREATE_GUILD_CHANNEL                = new Route(POST,   "/guilds/:guild/channels", "guild");
    public static final Route GET_GUILD_EMBED                     = new Route(GET,    "/guilds/:guild/embed", "guild");
    public static final Route MODIFY_GUILD_EMBED                  = new Route(PATCH,  "/guilds/:guild/embed", "guild");
    public static final Route GET_GUILD_VANITY_URL                = new Route(GET,    "/guilds/:guild/vanity-url", "guild");
    public static final Route LIST_GUILD_EMOJIS                   = new Route(GET,    "/guilds/:guild/emojis", "guild");
    public static final Route GET_GUILD_EMOJI                     = new Route(GET,    "/guilds/:guild/emojis/:emojis", "guild");
    public static final Route CREATE_GUILD_EMOJI                  = new Route(POST,   "/guilds/:guild/emojis", "guild");
    public static final Route MODIFY_GUILD_EMOJI                  = new Route(PATCH,  "/guilds/:guild/emojis/:emojis", "guild");
    public static final Route DELETE_GUILD_EMOJI                  = new Route(DELETE, "/guilds/:guild/emojis/:emojis", "guild");
    public static final Route GET_GUILD_INTEGRATIONS              = new Route(GET,    "/guilds/:guild/integrations", "guild");
    public static final Route CREATE_GUILD_INTEGRATION            = new Route(POST,   "/guilds/:guild/integrations", "guild");
    public static final Route DELETE_GUILD_INTEGRATION            = new Route(DELETE, "/guilds/:guild/integrations/:integration", "guild");
    public static final Route MODIFY_GUILD_INTEGRATION            = new Route(PATCH,  "/guilds/:guild/integrations/:integration", "guild");
    public static final Route SYNC_GUILD_INTEGRATION              = new Route(POST,   "/guilds/:guild/integrations/:integration/sync", "guild");
    public static final Route GET_GUILD_INVITES                   = new Route(GET,    "/guilds/:guild/invites", "guild");
    public static final Route LIST_GUILD_MEMBERS                  = new Route(GET,    "/guilds/:guild/members", "guild");
    public static final Route MODIFY_CURRENT_USERS_NICK           = new Route(PATCH,  "/guilds/:guild/members/@me/nick", "guild");
    public static final Route REMOVE_GUILD_MEMBER                 = new Route(DELETE, "/guilds/:guild/members/:user", "guild");
    public static final Route GET_GUILD_MEMBER                    = new Route(GET,    "/guilds/:guild/members/:user", "guild");
    public static final Route MODIFY_GUILD_MEMBER                 = new Route(PATCH,  "/guilds/:guild/members/:user", "guild");
    public static final Route SEARCH_GUILD_MEMBERS                = new Route(GET,    "/guilds/:guild/members/search", "guild");
    public static final Route ADD_GUILD_MEMBER                    = new Route(PUT,    "/guilds/:guild/members/:user", "guild");
    public static final Route REMOVE_GUILD_MEMBER_ROLE            = new Route(DELETE, "/guilds/:guild/members/:user/roles/:role", "guild");
    public static final Route ADD_GUILD_MEMBER_ROLE               = new Route(PUT,    "/guilds/:guild/members/:user/roles/:role", "guild");
    public static final Route GET_GUILD_PRUNE_COUNT               = new Route(GET,    "/guilds/:guild/prune", "guild");
    public static final Route BEGIN_GUILD_PRUNE                   = new Route(POST,   "/guilds/:guild/prune", "guild");
    public static final Route GET_GUILD_VOICE_REGIONS             = new Route(GET,    "/guilds/:guild/regions", "guild");
    public static final Route GET_GUILD_ROLES                     = new Route(GET,    "/guilds/:guild/roles", "guild");
    public static final Route MODIFY_GUILD_ROLE_POSITIONS         = new Route(PATCH,  "/guilds/:guild/roles", "guild");
    public static final Route CREATE_GUILD_ROLE                   = new Route(POST,   "/guilds/:guild/roles", "guild");
    public static final Route DELETE_GUILD_ROLE                   = new Route(DELETE, "/guilds/:guild/roles/:role", "guild");
    public static final Route MODIFY_GUILD_ROLE                   = new Route(PATCH,  "/guilds/:guild/roles/:role", "guild");
    public static final Route GET_GUILD_WEBHOOKS                  = new Route(GET,    "/guilds/:guild/webhooks", "guild");
    public static final Route GET_WEBHOOK                         = new Route(GET,    "/webhooks/:webhook", "webhook");
    public static final Route MODIFY_WEBHOOK                      = new Route(PATCH,  "/webhooks/:webhook", "webhook");
    public static final Route DELETE_WEBHOOK                      = new Route(DELETE, "/webhooks/:webhook", "webhook");
    public static final Route GET_WEBHOOK_TOKEN                   = new Route(GET,    "/webhooks/:webhook/:token", "webhook");
    public static final Route EXECUTE_WEBHOOK                     = new Route(POST,   "/webhooks/:webhook/:token", "webhook");
    public static final Route EDIT_WEBHOOK_MESSAGE                = new Route(PATCH,  "/webhooks/:webhook/:token/messages/:message", "webhook");
    public static final Route DELETE_WEBHOOK_MESSAGE              = new Route(DELETE, "/webhooks/:webhook/:token/messages/:message", "webhook");
    public static final Route DELETE_INVITE                       = new Route(DELETE, "/invites/:invite");
    public static final Route GET_INVITE                          = new Route(GET,    "/invites/:invite");
    public static final Route ACCEPT_INVITE                       = new Route(POST,   "/invites/:invite");
    public static final Route GET_CURRENT_USER                    = new Route(GET,    "/users/@me");
    public static final Route MODIFY_CURRENT_USER                 = new Route(PATCH,  "/users/@me");
    public static final Route GET_USER_DMS                        = new Route(GET,    "/users/@me/channels");
    public static final Route CREATE_DM                           = new Route(POST,   "/users/@me/channels");
    public static final Route GET_CURRENT_USER_GUILDS             = new Route(GET,    "/users/@me/guilds");
    public static final Route LEAVE_GUILD                         = new Route(DELETE, "/users/@me/guilds/:guild");
    public static final Route GET_USER                            = new Route(GET,    "/users/:user");
    public static final Route GET_CURRENT_APPLICATION_INFORMATION = new Route(GET,    "/oauth2/applications/@me");
    public static final Route LIST_VOICE_REGIONS                  = new Route(GET,    "/voice/regions");
    public static final Route CREATE_INTERACTION_INITIAL_RESPONSE = new Route(POST,   "/interactions/:interaction/:interactionToken/callback", "interaction");
    public static final Route EDIT_INTERACTION_INITIAL_RESPONSE   = new Route(PATCH,  "/webhooks/:interaction/:token/messages/@original");
    public static final Route DELETE_INTERACTION_INITIAL_RESPONSE = new Route(DELETE, "/webhooks/:interaction/:token/messages/@original");
    public static final Route CREATE_INTERACTION_FOLLOWUP         = new Route(POST,   "/webhooks/:interaction/:token/messages", "interaction");
    public static final Route EDIT_INTERACTION_FOLLOWUP           = new Route(PATCH,  "/webhooks/:interaction/:token/messages/:message", "interaction");
    public static final Route DELETE_INTERACTION_FOLLOWUP         = new Route(DELETE, "/webhooks/:interaction/:token/messages/:message", "interaction");
    public static final Route GET_GLOBAL_APPLICATION_COMMANDS     = new Route(GET,    "/applications/:application/commands", "application");
    public static final Route CREATE_GLOBAL_APPLICATION_COMMAND   = new Route(POST,   "/applications/:application/commands", "application");
    public static final Route EDIT_GLOBAL_APPLICATION_COMMAND     = new Route(PATCH,  "/applications/:application/commands/:command", "application");
    public static final Route DELETE_GLOBAL_APPLICATION_COMMAND   = new Route(DELETE, "/applications/:application/commands/:command", "application");
    public static final Route GET_GUILD_APPLICATION_COMMANDS      = new Route(GET,    "/applications/:application/guilds/:guild/commands", "application");
    public static final Route CREATE_GUILD_APPLICATION_COMMAND    = new Route(POST,   "/applications/:application/guilds/:guild/commands", "application");
    public static final Route EDIT_GUILD_APPLICATION_COMMAND      = new Route(PATCH,  "/applications/:application/guilds/:guild/commands/:command", "application");
    public static final Route DELETE_GUILD_APPLICATION_COMMAND    = new Route(DELETE, "/applications/:application/guilds/:guild/commands/:command", "application");
    // TODO: Verify these routes
    public static final Route GET_CHANNEL_THREADS                 = new Route(GET,    "/channels/:channel/threads", "channel");
    public static final Route GET_CHANNEL_MESSAGE_THREADS         = new Route(GET,    "/channels/:channel/messages/:message/threads", "channel");
    // @formatter:on
    
    private Routes() {
    }
    
    public enum HttpMethod {
        OPTIONS, GET, HEAD, POST, PUT, DELETE, TRACE, CONNECT, PATCH, OTHER
    }
    
    @Accessors(fluent = true)
    @SuppressWarnings({"WeakerAccess", "unused", "ClassWithTooManyConstructors"})
    public static final class Route {
        @Getter
        private HttpMethod method;
        @Getter
        private String baseRoute;
        @Getter
        private String majorParam;
        @Getter
        private String ratelimitKey;
        // Some routes, specifically reaction-create, require millisecond
        // precision for correct operation. Rather than special-casing this
        // with a String.contains() or similar, we can just mark individual
        // routes as needed, and then the requester can just check
        // Route#requiresMsPrecision()
        @Getter
        private boolean requiresMsPrecision;
        
        public Route() {
        }
        
        public Route(@Nonnull final HttpMethod method, @Nonnull final String baseRoute) {
            this(method, baseRoute, null);
        }
        
        public Route(@Nonnull final HttpMethod method, @Nonnull final String baseRoute, final boolean requiresMsPrecision) {
            this(method, baseRoute, null, requiresMsPrecision);
        }
        
        public Route(@Nonnull final HttpMethod method, @Nonnull final String baseRoute, @Nullable final String majorParam) {
            this(method, baseRoute, majorParam, baseRoute);
        }
        
        public Route(@Nonnull final HttpMethod method, @Nonnull final String baseRoute, @Nullable final String majorParam,
                     final boolean requiresMsPrecision) {
            this(method, baseRoute, majorParam, baseRoute, requiresMsPrecision);
        }
        
        public Route(@Nonnull final HttpMethod method, @Nonnull final String baseRoute, @Nullable final String majorParam,
                     @Nonnull final String ratelimitKey) {
            this(method, baseRoute, majorParam, ratelimitKey, false);
        }
        
        public Route(@Nonnull final HttpMethod method, @Nonnull final String baseRoute, @Nullable final String majorParam,
                     @Nonnull final String ratelimitKey, final boolean requiresMsPrecision) {
            this.method = method;
            this.baseRoute = baseRoute;
            this.majorParam = majorParam;
            this.ratelimitKey = ratelimitKey;
            this.requiresMsPrecision = requiresMsPrecision;
        }
        
        @Nonnull
        @CheckReturnValue
        public Route withMajorParam(@Nonnull final String value) {
            if(majorParam == null) {
                throw new IllegalStateException("This route takes no major params!");
            }
            final String majorParamString = ':' + majorParam;
            return new Route(method, baseRoute.replace(majorParamString, value), null,
                    baseRoute.replace(majorParamString, value), requiresMsPrecision);
        }
        
        @Nonnull
        @CheckReturnValue
        public Route compile(@Nonnull final String param, @Nonnull final String value) {
            if(param.equalsIgnoreCase(majorParam)) {
                return this;
            }
            return new Route(method, baseRoute.replace(':' + param, value), majorParam, ratelimitKey, requiresMsPrecision);
        }
        
        @Nonnull
        @CheckReturnValue
        public Route copy() {
            return new Route(method, baseRoute, majorParam, ratelimitKey);
        }
        
        public Route withQueryString(final String qs) {
            return new Route(method, baseRoute + qs, majorParam, ratelimitKey, requiresMsPrecision);
        }
        
        @Override
        public int hashCode() {
            return baseRoute.hashCode();
        }
        
        @Override
        public boolean equals(final Object o) {
            if(!(o instanceof Route)) {
                return false;
            }
            return baseRoute.equalsIgnoreCase(((Route) o).baseRoute);
        }
        
        @Override
        public String toString() {
            return method + " " + baseRoute;
        }
    }
}
