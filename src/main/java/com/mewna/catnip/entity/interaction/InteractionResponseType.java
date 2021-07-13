/*
 * Copyright (c) 2020 amy, All rights reserved.
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

package com.mewna.catnip.entity.interaction;

import lombok.Getter;

/**
 * @author amy
 * @since 12/10/20.
 */
@Getter
public enum InteractionResponseType {
    /**
     * ACK a {@link InteractionType#PING}.
     */
    PONG(1),
    
    /**
     * ACK a command without sending a message, eating the user's input.
     */
    @Deprecated
    ACKNOWLEDGE(2),
    
    /**
     * Respond with a message, eating the user's input.
     */
    @Deprecated
    CHANNEL_MESSAGE(3),
    
    /**
     * Respond with a message, showing the user's input.
     */
    CHANNEL_MESSAGE_WITH_SOURCE(4),
    
    /**
     * ACK a command without sending a message, showing the user's input.
     */
    ACK_WITH_SOURCE(5),
    
    /**
     * For components, ACK an interaction and edit the original message later; the user does not see a loading state
     */
    DEFERRED_UPDATE_MESSAGE(6),
    
    /**
     * For components, edit the message the component was attached to
     */
    UPDATE_MESSAGE(7),
    ;
    
    private final int key;
    
    InteractionResponseType(final int key) {
        this.key = key;
    }
    
    public static InteractionResponseType byKey(final int key) {
        for(final InteractionResponseType value : values()) {
            if(value.key == key) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown InteractionResponseType: " + key);
    }
}
