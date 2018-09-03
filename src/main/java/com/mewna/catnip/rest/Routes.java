package com.mewna.catnip.rest;

import io.vertx.core.http.HttpMethod;
import lombok.Getter;
import lombok.experimental.Accessors;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static io.vertx.core.http.HttpMethod.*;

/**
 * @author amy
 * @since 8/31/18.
 */
@SuppressWarnings({"StaticVariableOfConcreteClass", "WeakerAccess"})
public final class Routes {
    private Routes() {
    }
    
    @Accessors(fluent = true)
    @SuppressWarnings({"WeakerAccess", "unused"})
    public static final class Route {
        @Getter
        private HttpMethod method;
        @Getter
        private String baseRoute;
        @Getter
        private String majorParam;
        
        public Route() {
        }
        
        public Route(@Nonnull final HttpMethod method, @Nonnull final String baseRoute) {
            this(method, baseRoute, null);
        }
        
        public Route(@Nonnull final HttpMethod method, @Nonnull final String baseRoute, @Nullable final String majorParam) {
            this.method = method;
            this.baseRoute = baseRoute;
            this.majorParam = majorParam;
        }
        
        @Nonnull
        @CheckReturnValue
        @SuppressWarnings("TypeMayBeWeakened")
        public Route withMajorParam(@Nonnull final String value) {
            if(majorParam == null) {
                throw new IllegalStateException("This route takes no major params!");
            }
            return new Route(method, baseRoute.replace('{' + majorParam + '}', value));
        }
    
        @Nonnull
        @CheckReturnValue
        @SuppressWarnings("TypeMayBeWeakened")
        Route compile(@Nonnull final String param, @Nonnull final String value) {
            return new Route(method, baseRoute.replace('{' + param + '}', value));
        }
    
        @Nonnull
        @CheckReturnValue
        Route copy() {
            return new Route(method, baseRoute, majorParam);
        }
    
        @Override
        public boolean equals(final Object o) {
            if(!(o instanceof Route)) {
                return false;
            }
            return baseRoute.equalsIgnoreCase(((Route) o).baseRoute);
        }
    }

    // @formatter:off
    public static final Route GET_GATEWAY_BOT                     = new Route(GET,    "/gateway/bot");

    public static final Route DELETE_CHANNEL                      = new Route(DELETE, "/channels/{channel.id}", "channel.id");
    public static final Route GET_CHANNEL                         = new Route(GET,    "/channels/{channel.id}", "channel.id");
    public static final Route MODIFY_CHANNEL                      = new Route(PATCH,  "/channels/{channel.id}", "channel.id");
    public static final Route GET_CHANNEL_INVITES                 = new Route(GET,    "/channels/{channel.id}/invites", "channel.id");
    public static final Route CREATE_CHANNEL_INVITE               = new Route(POST,   "/channels/{channel.id}/invites", "channel.id");
    public static final Route GET_CHANNEL_MESSAGES                = new Route(GET,    "/channels/{channel.id}/messages", "channel.id");
    public static final Route CREATE_MESSAGE                      = new Route(POST,   "/channels/{channel.id}/messages", "channel.id");
    public static final Route BULK_DELETE_MESSAGES                = new Route(POST,   "/channels/{channel.id}/messages/bulk-delete", "channel.id");
    public static final Route DELETE_MESSAGE                      = new Route(DELETE, "/channels/{channel.id}/messages/{message.id}", "channel.id");
    public static final Route GET_CHANNEL_MESSAGE                 = new Route(GET,    "/channels/{channel.id}/messages/{message.id}", "channel.id");
    public static final Route EDIT_MESSAGE                        = new Route(PATCH,  "/channels/{channel.id}/messages/{message.id}", "channel.id");
    public static final Route DELETE_ALL_REACTIONS                = new Route(DELETE, "/channels/{channel.id}/messages/{message.id}/reactions", "channel.id");
    public static final Route GET_REACTIONS                       = new Route(GET,    "/channels/{channel.id}/messages/{message.id}/reactions/{emoji}", "channel.id");
    public static final Route DELETE_OWN_REACTION                 = new Route(DELETE, "/channels/{channel.id}/messages/{message.id}/reactions/{emoji}/@me", "channel.id");
    public static final Route CREATE_REACTION                     = new Route(PUT,    "/channels/{channel.id}/messages/{message.id}/reactions/{emoji}/@me", "channel.id");
    public static final Route DELETE_USER_REACTION                = new Route(DELETE, "/channels/{channel.id}/messages/{message.id}/reactions/{emoji}/{user.id}", "channel.id");
    public static final Route DELETE_CHANNEL_PERMISSION           = new Route(DELETE, "/channels/{channel.id}/permissions/{overwrite.id}", "channel.id");
    public static final Route EDIT_CHANNEL_PERMISSIONS            = new Route(PUT,    "/channels/{channel.id}/permissions/{overwrite.id}", "channel.id");
    public static final Route GET_PINNED_MESSAGES                 = new Route(GET,    "/channels/{channel.id}/pins", "channel.id");
    public static final Route DELETE_PINNED_CHANNEL_MESSAGE       = new Route(DELETE, "/channels/{channel.id}/pins/{message.id}", "channel.id");
    public static final Route ADD_PINNED_CHANNEL_MESSAGE          = new Route(PUT,    "/channels/{channel.id}/pins/{message.id}", "channel.id");
    public static final Route TRIGGER_TYPING_INDICATOR            = new Route(POST,   "/channels/{channel.id}/typing", "channel.id");
    public static final Route GET_CHANNEL_WEBHOOKS                = new Route(GET,    "/channels/{channel.id}/webhooks", "channel.id");
    public static final Route CREATE_WEBHOOK                      = new Route(POST,   "/channels/{channel.id}/webhooks", "channel.id");

    public static final Route CREATE_GUILD                        = new Route(POST,   "/guilds");
    public static final Route DELETE_GUILD                        = new Route(DELETE, "/guilds/{guild.id}", "guild.id");
    public static final Route GET_GUILD                           = new Route(GET,    "/guilds/{guild.id}", "guild.id");
    public static final Route MODIFY_GUILD                        = new Route(PATCH,  "/guilds/{guild.id}", "guild.id");
    public static final Route GET_GUILD_AUDIT_LOG                 = new Route(GET,    "/guilds/{guild.id}/audit-log", "guild.id");
    public static final Route GET_GUILD_BANS                      = new Route(GET,    "/guilds/{guild.id}/bans", "guild.id");
    public static final Route REMOVE_GUILD_BAN                    = new Route(DELETE, "/guilds/{guild.id}/bans/{user.id}", "guild.id");
    public static final Route CREATE_GUILD_BAN                    = new Route(PUT,    "/guilds/{guild.id}/bans/{user.id}", "guild.id");
    public static final Route GET_GUILD_CHANNELS                  = new Route(GET,    "/guilds/{guild.id}/channels", "guild.id");
    public static final Route MODIFY_GUILD_CHANNEL_POSITIONS      = new Route(PATCH,  "/guilds/{guild.id}/channels", "guild.id");
    public static final Route CREATE_GUILD_CHANNEL                = new Route(POST,   "/guilds/{guild.id}/channels", "guild.id");
    public static final Route GET_GUILD_EMBED                     = new Route(GET,    "/guilds/{guild.id}/embed", "guild.id");
    public static final Route MODIFY_GUILD_EMBED                  = new Route(PATCH,  "/guilds/{guild.id}/embed", "guild.id");
    public static final Route GET_GUILD_INTEGRATIONS              = new Route(GET,    "/guilds/{guild.id}/integrations", "guild.id");
    public static final Route CREATE_GUILD_INTEGRATION            = new Route(POST,   "/guilds/{guild.id}/integrations", "guild.id");
    public static final Route DELETE_GUILD_INTEGRATION            = new Route(DELETE, "/guilds/{guild.id}/integrations/{integration.id}", "guild.id");
    public static final Route MODIFY_GUILD_INTEGRATION            = new Route(PATCH,  "/guilds/{guild.id}/integrations/{integration.id}", "guild.id");
    public static final Route SYNC_GUILD_INTEGRATION              = new Route(POST,   "/guilds/{guild.id}/integrations/{integration.id}/sync", "guild.id");
    public static final Route GET_GUILD_INVITES                   = new Route(GET,    "/guilds/{guild.id}/invites", "guild.id");
    public static final Route LIST_GUILD_MEMBERS                  = new Route(GET,    "/guilds/{guild.id}/members", "guild.id");
    public static final Route MODIFY_CURRENT_USERS_NICK           = new Route(PATCH,  "/guilds/{guild.id}/members/@me/nick", "guild.id");
    public static final Route REMOVE_GUILD_MEMBER                 = new Route(DELETE, "/guilds/{guild.id}/members/{user.id}", "guild.id");
    public static final Route GET_GUILD_MEMBER                    = new Route(GET,    "/guilds/{guild.id}/members/{user.id}", "guild.id");
    public static final Route MODIFY_GUILD_MEMBER                 = new Route(PATCH,  "/guilds/{guild.id}/members/{user.id}", "guild.id");
    public static final Route ADD_GUILD_MEMBER                    = new Route(PUT,    "/guilds/{guild.id}/members/{user.id}", "guild.id");
    public static final Route REMOVE_GUILD_MEMBER_ROLE            = new Route(DELETE, "/guilds/{guild.id}/members/{user.id}/roles/{role.id}", "guild.id");
    public static final Route ADD_GUILD_MEMBER_ROLE               = new Route(PUT,    "/guilds/{guild.id}/members/{user.id}/roles/{role.id}", "guild.id");
    public static final Route GET_GUILD_PRUNE_COUNT               = new Route(GET,    "/guilds/{guild.id}/prune", "guild.id");
    public static final Route BEGIN_GUILD_PRUNE                   = new Route(POST,   "/guilds/{guild.id}/prune", "guild.id");
    public static final Route GET_GUILD_VOICE_REGIONS             = new Route(GET,    "/guilds/{guild.id}/regions", "guild.id");
    public static final Route GET_GUILD_ROLES                     = new Route(GET,    "/guilds/{guild.id}/roles", "guild.id");
    public static final Route MODIFY_GUILD_ROLE_POSITIONS         = new Route(PATCH,  "/guilds/{guild.id}/roles", "guild.id");
    public static final Route CREATE_GUILD_ROLE                   = new Route(POST,   "/guilds/{guild.id}/roles", "guild.id");
    public static final Route DELETE_GUILD_ROLE                   = new Route(DELETE, "/guilds/{guild.id}/roles/{role.id}", "guild.id");
    public static final Route MODIFY_GUILD_ROLE                   = new Route(PATCH,  "/guilds/{guild.id}/roles/{role.id}", "guild.id");
    public static final Route GET_GUILD_WEBHOOKS                  = new Route(GET,    "/guilds/{guild.id}/webhooks", "guild.id");

    public static final Route DELETE_INVITE                       = new Route(DELETE, "/invites/{invite.code}");
    public static final Route GET_INVITE                          = new Route(GET,    "/invites/{invite.code}");
    public static final Route ACCEPT_INVITE                       = new Route(POST,   "/invites/{invite.code}");

    public static final Route GET_CURRENT_USER                    = new Route(GET,    "/users/@me");
    public static final Route MODIFY_CURRENT_USER                 = new Route(PATCH,  "/users/@me");
    public static final Route GET_USER_DMS                        = new Route(GET,    "/users/@me/channels");
    public static final Route CREATE_DM                           = new Route(POST,   "/users/@me/channels");
    public static final Route LEAVE_GUILD                         = new Route(DELETE, "/users/@me/guilds/{guild.id}");
    public static final Route GET_USER                            = new Route(GET,    "/users/{user.id}");

    public static final Route GET_CURRENT_APPLICATION_INFORMATION = new Route(GET,    "/oauth2/applications/@me");
    public static final Route LIST_VOICE_REGIONS                  = new Route(GET,    "/voice/regions");
    // @formatter:on
}
