package com.mewna.catnip.extension.hook;

import com.mewna.catnip.rest.ResponsePayload;
import com.mewna.catnip.rest.Routes.Route;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A horrible thing that allows you to patch JSON objects and other stuff. This
 * is a horrible idea, but it exists mainly to allow you to hackpatch in things
 * that can't be added / won't be added / just aren't currently in the library.
 * Methods on this class are called <strong>SYNCHRONOUSLY</strong>, and so any
 * implementations of it should take this into consideration.
 * <p/>
 * For example, if you had a message-caching extension, you could use this to
 * inject the cached data into {@code MESSAGE_UPDATE} events, to make the data
 * in the event actually be "complete."
 * <p/>
 * Again, this is a <strong>TERRIBLE</strong> idea. <strong>You should only use
 * this if you have <em>absolutely no other choice</em>.</strong> It is
 * <em>highly</em> recommended that you try to find an alternative to using
 * this.
 *
 * @author amy
 * @since 10/7/18.
 */
public interface CatnipHook {
    /**
     * Called when a payload is received over the websocket. The default
     * behaviour is an identify function.
     *
     * @param json The incoming JSON data.
     *
     * @return A possibly-edited gateway-sent JSON payload.
     */
    default JsonObject rawGatewayReceiveHook(@Nonnull final JsonObject json) {
        return json;
    }
    
    /**
     * Called when a payload is sent over the websocket. The default behaviour
     * is an identify function.
     *
     * @param json The outgoing JSON data.
     *
     * @return A possibly-edited gateway-sent JSON payload.
     */
    default JsonObject rawGatewaySendHook(@Nonnull final JsonObject json) {
        return json;
    }

    /**
     * Called when a payload is received in a REST request response. Note that,
     * due to how the Discord API works, it is possible that the data received
     * may be empty. The default behaviour is an identity function.
     * <p/>
     * <strong>THIS METHOD DOES NOT SUPPORT CHANGING THE ROUTE.</strong>
     *
     * @param response The incoming data.
     *
     * @return A possibly-edited REST-response payload.
     */
    default ResponsePayload rawRestReceiveDataHook(@Nonnull final Route route, @Nonnull final ResponsePayload response) {
        return response;
    }
    
    /**
     * Called when a payload is about to be sent in a REST request response.
     * Note that, due to how the Discord API works, it is possible that the
     * data received may be empty. The default behaviour is an identity
     * function.
     * <p/>
     * <strong>THIS METHOD DOES NOT SUPPORT CHANGING THE ROUTE.</strong>
     *
     * @param json The outgoing JSON data.
     *
     * @return A possibly-edited REST-response JSON payload.
     */
    default JsonObject rawRestSendObjectHook(@Nonnull final Route route, @Nullable final JsonObject json) {
        return json;
    }
}
