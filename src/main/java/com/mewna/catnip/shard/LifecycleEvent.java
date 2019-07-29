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

package com.mewna.catnip.shard;

import com.mewna.catnip.entity.lifecycle.*;
import com.mewna.catnip.shard.event.EventType;

import static com.mewna.catnip.shard.event.EventTypeImpl.event;

/**
 * @author amy
 * @since 10/17/18.
 */
public interface LifecycleEvent {
    /**
     * Fired when the shard is created and is about to connect to the websocket
     * gateway. The payload is a shard id / total pair.
     */
    EventType<ShardInfo> CONNECTING = event(Raw.CONNECTING_TO_GATEWAY, ShardInfo.class);
    /**
     * Fired when the shard has connected to the websocket gateway, but has not
     * yet sent an IDENTIFY payload. The payload is a shard id / total pair.
     */
    EventType<ShardInfo> CONNECTED = event(Raw.CONNECTED_TO_GATEWAY, ShardInfo.class);
    /**
     * Fired when the shard has disconnected from the websocket gateway, and
     * will (hopefully) be reconnecting. The payload is a shard id / total
     * pair.
     */
    EventType<ShardInfo> DISCONNECTED = event(Raw.DISCONNECTED_FROM_GATEWAY, ShardInfo.class);
    /**
     * Fired when the shard has successfully IDENTIFYd with the websocket
     * gateway. This is effectively the same as listening on
     * {@link DiscordEvent#READY}. The payload is a shard id / total pair.
     */
    EventType<ShardInfo> IDENTIFIED = event(Raw.IDENTIFIED, ShardInfo.class);
    /**
     * Fired when the shard has successfully RESUMEd with the websocket
     * gateway. The payload is a shard id / total pair.
     */
    EventType<ShardInfo> RESUMED = event(Raw.RESUMED, ShardInfo.class);
    /**
     * Fired when the shard's session is invalidated, be it from an OP 9 or
     * other cause.
     */
    EventType<ShardInfo> SESSION_INVALIDATED = event(Raw.SESSION_INVALIDATED, ShardInfo.class);
    
    /**
     * Fired when all guild chunking has been completed.
     */
    EventType<ChunkingDone> CHUNKING_DONE = event(Raw.CHUNKING_DONE, ChunkingDone.class);
    
    /**
     * Fired if manual member chunk re-requesting is enabled.
     */
    EventType<MemberChunkRerequest> MEMBER_CHUNK_REREQUEST = event(Raw.MEMBER_CHUNK_REREQUEST, MemberChunkRerequest.class);
    
    /**
     * Fired when a shard's gateway websocket closes.
     */
    EventType<GatewayClosed> WEBSOCKET_CLOSED = event(Raw.GATEWAY_WEBSOCKET_CLOSED, GatewayClosed.class);
    /**
     * Fired when a shard fails to connect to Discord's websocket gateway.
     */
    EventType<GatewayConnectionFailed> WEBSOCKET_CONNECTION_FAILED
            = event(Raw.GATEWAY_WEBSOCKET_CONNECTION_FAILED, GatewayConnectionFailed.class);
    
    /**
     * Fired whenever a REST route hits a ratelimit (HTTP 429).
     */
    EventType<RestRatelimitHit> REST_RATELIMIT_HIT = event(Raw.REST_RATELIMIT_HIT, RestRatelimitHit.class);
    
    interface Raw {
        // @formatter:off
        String CONNECTING_TO_GATEWAY     = "CONNECTING";
        String CONNECTED_TO_GATEWAY      = "CONNECTED";
        String DISCONNECTED_FROM_GATEWAY = "DISCONNECTED";
        String IDENTIFIED                = "IDENTIFIED";
        String RESUMED                   = "RESUMED";
        String SESSION_INVALIDATED       = "SESSION_INVALIDATED";
        
        String CHUNKING_DONE          = "CHUNKING_DONE";
        String MEMBER_CHUNK_REREQUEST = "MEMBER_CHUNK_REREQUEST";
        
        String GATEWAY_WEBSOCKET_CLOSED            = "GATEWAY_WEBSOCKET_CLOSED";
        String GATEWAY_WEBSOCKET_CONNECTION_FAILED = "GATEWAY_WEBSOCKET_CONNECTION_FAILED";
        String REST_RATELIMIT_HIT                  = "REST_RATELIMIT_HIT";
        // @formatter:on
    }
}
