/*
 * Copyright (c) 2018-2019 amy, All rights reserved.
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

package com.mewna.catnip.entity;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.guild.UnavailableGuild;
import com.mewna.catnip.entity.impl.PresenceImpl;
import com.mewna.catnip.entity.impl.PresenceImpl.ActivityImpl;
import com.mewna.catnip.entity.impl.UnavailableGuildImpl;
import com.mewna.catnip.entity.user.Presence;
import com.mewna.catnip.entity.user.Presence.Activity;
import com.mewna.catnip.entity.user.Presence.ActivityType;
import com.mewna.catnip.entity.user.Presence.OnlineStatus;
import com.mewna.catnip.util.JsonPojoCodec;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by napster on 01.01.19.
 * <p>
 * Ensure that all our entities and the data they carry survives being serialized and deserialized through the eventbus.
 */
class CodecTest {
    
    private <T> void test(final Catnip catnip, final Consumer<T> assertions, final T entity) throws Exception {
        @SuppressWarnings("unchecked")
        final Class<T> entityClass = (Class<T>) entity.getClass();
        final String address = CodecTest.class.getName() + ':' + entityClass.getName();
        
        final CompletableFuture<Void> done = new CompletableFuture<>();
        final Handler<Message<T>> runAssertions = msg -> {
            try {
                final T deserialized = msg.body();
                
                //check per-test assertions
                assertions.accept(deserialized);
                
                //check that our catnip has been set on the entity
                if(deserialized instanceof RequiresCatnip) {
                    assertEquals(catnip, ((RequiresCatnip) deserialized).catnip());
                }
                
                done.complete(null);
            } catch(final Exception e) {
                done.completeExceptionally(e);
            }
        };
        
        final Vertx vertx = Vertx.vertx();
        vertx.eventBus().registerDefaultCodec(entityClass, new JsonPojoCodec<>(catnip, entityClass));
        vertx.eventBus().consumer(address, runAssertions);
        vertx.eventBus().send(address, entity);
        
        done.get(1, TimeUnit.SECONDS);
    }
    
    @Test
    void presence() throws Exception {
        final Catnip mocknip = Mockito.mock(Catnip.class);
        
        final OnlineStatus status = OnlineStatus.DND;
        final OnlineStatus mobileStatus = OnlineStatus.ONLINE;
        final OnlineStatus webStatus = OnlineStatus.OFFLINE;
        final Activity activity = ActivityImpl.builder()
                .name("Waifu Simulator")
                .type(ActivityType.PLAYING)
                .build();
        final Consumer<Presence> assertions = deserialized -> {
            assertEquals(status, deserialized.status());
            assertEquals(activity, deserialized.activity());
            assertEquals(mobileStatus, deserialized.mobileStatus());
            assertEquals(webStatus, deserialized.webStatus());
        };
        
        final Presence entity = PresenceImpl.builder()
                .catnip(mocknip)
                .status(status)
                .activity(activity)
                .mobileStatus(mobileStatus)
                .webStatus(webStatus)
                .build();
        
        test(mocknip, assertions, entity);
    }
    
    @Test
    void unavailableGuild() throws Exception {
        final Catnip mocknip = Mockito.mock(Catnip.class);
        
        final long id = 42L;
        final boolean unavailable = false;
        final Consumer<UnavailableGuild> assertions = deserialized -> {
            assertEquals(id, deserialized.idAsLong());
            assertEquals(unavailable, deserialized.unavailable());
        };
        
        final UnavailableGuild entity = UnavailableGuildImpl.builder()
                .catnip(mocknip)
                .idAsLong(id)
                .unavailable(unavailable)
                .build();
        
        test(mocknip, assertions, entity);
    }
}
