package com.mewna.catnip.rest.handler;

import com.google.common.collect.ImmutableMap;
import com.mewna.catnip.entity.misc.VoiceRegion;
import com.mewna.catnip.internal.CatnipImpl;
import com.mewna.catnip.rest.ResponsePayload;
import com.mewna.catnip.rest.RestRequester.OutboundRequest;
import com.mewna.catnip.rest.Routes;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class RestVoice extends RestHandler {
    public RestVoice(final CatnipImpl catnip) {
        super(catnip);
    }
    
    @Nonnull
    @CheckReturnValue
    public CompletionStage<List<VoiceRegion>> listVoiceRegions() {
        return getCatnip().requester().queue(new OutboundRequest(Routes.LIST_VOICE_REGIONS,
                ImmutableMap.of()))
                .thenApply(ResponsePayload::array)
                .thenApply(mapObjectContents(getEntityBuilder()::createVoiceRegion));
    }
}
