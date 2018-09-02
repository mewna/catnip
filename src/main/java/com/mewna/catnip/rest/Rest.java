package com.mewna.catnip.rest;

import com.google.common.collect.ImmutableMap;
import com.mewna.catnip.Catnip;
import com.mewna.catnip.rest.RestRequester.OutboundRequest;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import lombok.Getter;

/**
 * @author amy
 * @since 9/1/18.
 */
public class Rest {
    @Getter
    private final Catnip catnip;
    
    public Rest(final Catnip catnip) {
        this.catnip = catnip;
    }
    
    public Future<JsonObject> createMessage(final String channelId, final String message) {
        return catnip._requester().queue(new OutboundRequest(Routes.CREATE_MESSAGE.withMajorParam(channelId),
                ImmutableMap.of(), new JsonObject().put("content", message)));
    }
}
