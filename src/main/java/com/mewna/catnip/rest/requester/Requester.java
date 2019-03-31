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

import com.mewna.catnip.Catnip;
import com.mewna.catnip.rest.ResponsePayload;
import com.mewna.catnip.rest.Routes.Route;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

public interface Requester {
    String API_HOST = "https://discordapp.com";
    int API_VERSION = 6;
    String API_BASE = "/api/v" + API_VERSION;
    String REASON_HEADER = "X-Audit-Log-Reason";
    
    void catnip(@Nonnull Catnip catnip);
    
    @Nonnull
    @CheckReturnValue
    CompletionStage<ResponsePayload> queue(@Nonnull OutboundRequest r);
    
    @SuppressWarnings("unused")
    final class OutboundRequest {
        private final Route route;
        private final Map<String, String> params;
        private JsonObject object;
        private JsonArray array;
        private boolean needsToken = true;
        private String reason;
        
        private List<ImmutablePair<String, Buffer>> buffers;
        
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
        
        public Route route() {
            return route;
        }
        
        public Map<String, String> params() {
            return params;
        }
        
        public JsonObject object() {
            return object;
        }
        
        public JsonArray array() {
            return array;
        }
        
        public boolean needsToken() {
            return needsToken;
        }
        
        public String reason() {
            return reason;
        }
        
        public List<ImmutablePair<String, Buffer>> buffers() {
            return buffers;
        }
        
        public boolean emptyBody() {
            return emptyBody;
        }
        
        public OutboundRequest object(final JsonObject object) {
            this.object = object;
            return this;
        }
        
        public OutboundRequest array(final JsonArray array) {
            this.array = array;
            return this;
        }
        
        public OutboundRequest needsToken(final boolean needsToken) {
            this.needsToken = needsToken;
            return this;
        }
        
        public OutboundRequest reason(final String reason) {
            this.reason = reason;
            return this;
        }
        
        public OutboundRequest buffers(final List<ImmutablePair<String, Buffer>> buffers) {
            this.buffers = buffers;
            return this;
        }
        
        public OutboundRequest emptyBody(final boolean emptyBody) {
            this.emptyBody = emptyBody;
            return this;
        }
    }
}
