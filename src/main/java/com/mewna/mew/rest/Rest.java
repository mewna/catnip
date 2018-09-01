package com.mewna.mew.rest;

import com.google.common.collect.ImmutableMap;
import com.mewna.mew.Mew;
import com.mewna.mew.rest.RestRequester.OutboundRequest;
import com.mewna.mew.rest.Routes.Route;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import lombok.Getter;

/**
 * @author amy
 * @since 9/1/18.
 */
public class Rest {
    @Getter
    private final Mew mew;
    
    public Rest(final Mew mew) {
        this.mew = mew;
    }
    
    public Future<JsonObject> createMessage(final Route route, final String message) {
        return mew.restRequester().queue(new OutboundRequest(route, ImmutableMap.of(), new JsonObject().put("content", message)));
    }
}
