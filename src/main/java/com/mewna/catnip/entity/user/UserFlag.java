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

package com.mewna.catnip.entity.user;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.EnumSet;
import java.util.Set;

/**
 * The flags that can be on a user's account. An undocumented subset of these
 * flags is exposed via {@link User#publicFlags()}.
 *
 * @author amy
 * @since 4/18/20.
 */
@Accessors(fluent = true)
public enum UserFlag {
    DISCORD_EMPLOYEE(1),
    DISCORD_PARTNER(1 << 1),
    HYPESQUAD_EVENTS(1 << 2),
    BUG_HUNTER_LEVEL_1(1 << 3),
    HOUSE_BRAVERY(1 << 6),
    HOUSE_BRILLIANCE(1 << 7),
    HOUSE_BALANCE(1 << 8),
    EARLY_SUPPORTER(1 << 9),
    TEAM_USER(1 << 10),
    SYSTEM(1 << 12),
    BUG_HUNTER_LEVEL_2(1 << 14)
    ;
    
    @Getter
    private final int value;
    
    UserFlag(final int value) {
        this.value = value;
    }
    
    public static Set<UserFlag> toSet(final long asLong) {
        final Set<UserFlag> flags = EnumSet.noneOf(UserFlag.class);
        
        for(final UserFlag flag : values()) {
            if((asLong & flag.value) == flag.value) {
                flags.add(flag);
            }
        }
        
        return flags;
    }
}
