/*
 * Copyright (c) 2019 amy, All rights reserved.
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

package com.mewna.catnip.entity.message;

import lombok.Getter;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.Set;

/**
 * @author amy
 * @since 8/19/19.
 */
@Accessors(fluent = true)
public enum MessageFlag {
    CROSSPOSTED(1, false),
    IS_CROSSPOST(1 << 1, false),
    SUPPRESS_EMBEDS(1 << 2, true),
    SOURCE_MESSAGE_DELETED(1 << 3, false),
    URGENT(1 << 4, false),
    HAS_THREAD(1 << 5, false),
    EPHEMERAL(1 << 6, true),
    ;
    
    @Getter
    private final int value;
    @Getter
    private final boolean canBeSet;
    
    MessageFlag(final int value, final boolean canBeSet) {
        this.value = value;
        this.canBeSet = canBeSet;
    }
    
    public static Set<MessageFlag> toSet(final long asLong) {
        final Set<MessageFlag> flags = EnumSet.noneOf(MessageFlag.class);
        
        for(final MessageFlag flag : values()) {
            if((asLong & flag.value) == flag.value) {
                flags.add(flag);
            }
        }
        
        return flags;
    }
    
    public static long from(@Nonnull final Iterable<MessageFlag> flags) {
        long result = 0;
        for(final MessageFlag flag : flags) {
            result |= flag.value;
        }
        return result;
    }
    
    public static long fromSettable(@Nonnull final Iterable<MessageFlag> flags) {
        long result = 0;
        for(final MessageFlag flag : flags) {
            if(!flag.canBeSet) {
                throw new IllegalArgumentException("Message flag " + flag.name() + " can't be set!");
            }
            result |= flag.value;
        }
        return result;
    }
}
