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

package com.mewna.catnip.entity.misc;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.EnumSet;
import java.util.Set;

/**
 * The flags that can be on a application.
 *
 * @author lulalaby
 * @since 6/22/21.
 */
@Accessors(fluent = true)
public enum ApplicationFlag {
    MANAGED_EMOJI(1 << 2),
    GROUP_DM_CREATE(1 << 5),
    RPC_HAS_CONNECTED(1 << 11),
    GATEWAY_PRESENCE(1 << 12),
    GATEWAY_PRESENCE_LIMITED(1 << 13),
    GATEWAY_GUILD_MEMBERS(1 << 14),
    GATEWAY_GUILD_MEMBERS_LIMITED(1 << 15),
    VERIFICATION_PENDING_GUILD_LIMIT(1 << 16),
    EMBEDDED(1 << 17),
    GATEWAY_MESSAGE_CONTENT(1 << 18),
    GATEWAY_MESSAGE_CONTENT_LIMITED(1 << 19),
    ;
    
    @Getter
    private final int value;
    
    ApplicationFlag(final int value) {
        this.value = value;
    }
    
    public static Set<ApplicationFlag> toSet(final long asLong) {
        final Set<ApplicationFlag> flags = EnumSet.noneOf(ApplicationFlag.class);
        
        for(final ApplicationFlag flag : values()) {
            if((asLong & flag.value) == flag.value) {
                flags.add(flag);
            }
        }
        
        return flags;
    }
}
