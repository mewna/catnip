/*
 * Copyright (c) 2019 amy, All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *  3. Neither the name of the copyright holder nor the names of its contributors
 *     may be used to endorse or promote products derived from this software without
 *     specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 *  FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 *  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *  CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.mewna.catnip.rest.requester;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.mewna.catnip.Catnip;
import com.mewna.catnip.rest.ResponsePayload;
import com.mewna.catnip.rest.Routes.Route;
import io.reactivex.rxjava3.core.Observable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

/**
 * A REST requester. The requester is responsible for queuing up new requests,
 * and executing them at some point in the future. Once the request has been
 * executed, the observable can then start emitting the response.
 */
public interface Requester {
    String REASON_HEADER = "X-Audit-Log-Reason";
    
    void catnip(@Nonnull Catnip catnip);
    
    /**
     * Queues a new request to be executed.
     * @param r The request to execute.
     * @return An {@link Observable} that emits when the request has completed.
     */
    @Nonnull
    @CheckReturnValue
    Observable<ResponsePayload> queue(@Nonnull OutboundRequest r);
    
    /**
     * POJO that represents an outbound request to Discord's REST API.
     */
    @Getter
    @Accessors(fluent = true)
    @SuppressWarnings("unused")
    final class OutboundRequest {
        private final Route route;
        private final Map<String, String> params;
        @Setter
        private JsonObject object;
        @Setter
        private JsonArray array;
        @Setter
        private boolean needsToken = true;
        @Setter
        private String reason;
        
        @Setter
        private List<ImmutablePair<String, byte[]>> buffers;
        
        @Setter
        private boolean emptyBody;
        
        public OutboundRequest(final Route route, final Map<String, String> params) {
            this.route = route;
            this.params = params;
        }
        
        public OutboundRequest(final Route route, final Map<String, String> params, final JsonObject object) {
            this(route, params);
            this.object = object;
        }
        
        public OutboundRequest(final Route route, final Map<String, String> params, final JsonObject object, final String reason) {
            this(route, params, object);
            this.reason = reason;
        }
        
        public OutboundRequest(final Route route, final Map<String, String> params, final JsonArray array) {
            this(route, params);
            this.array = array;
        }
        
        public OutboundRequest(final Route route, final Map<String, String> params, final JsonArray array, final String reason) {
            this(route, params, array);
            this.reason = reason;
        }
        
        @Override
        public String toString() {
            return String.format("OutboundRequest (%s, %s, object=%s, array=%s, buffers=%s, reason=%s)",
                    route, params, object != null, array != null, buffers != null && !buffers.isEmpty(), reason);
        }
    }
}
