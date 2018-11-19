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

package com.mewna.catnip.entity.util;

import lombok.Getter;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * @author Julia Rogers
 * @since 9/2/18
 */
@Accessors(fluent = true)
public enum Permission {
    CREATE_INSTANT_INVITE(0x00000001, true),
    KICK_MEMBERS(0x00000002, false),
    BAN_MEMBERS(0x00000004, false),
    ADMINISTRATOR(0x00000008, false),
    MANAGE_CHANNELS(0x00000010, true),
    MANAGE_GUILD(0x00000020, false),
    ADD_REACTIONS(0x00000040, true),
    VIEW_AUDIT_LOG(0x00000080, false),
    VIEW_CHANNEL(0x00000400, true),
    SEND_MESSAGES(0x00000800, true),
    SEND_TTS_MESSAGES(0x00001000, true),
    MANAGE_MESSAGES(0x00002000, true),
    EMBED_LINKS(0x00004000, true),
    ATTACH_FILES(0x00008000, true),
    READ_MESSAGE_HISTORY(0x00010000, true),
    MENTION_EVERYONE(0x00020000, true),
    USE_EXTERNAL_EMOJI(0x00040000, true),
    CONNECT(0x00100000, true),
    SPEAK(0x00200000, true),
    MUTE_MEMBERS(0x00400000, true),
    DEAFEN_MEMBERS(0x00800000, true),
    MOVE_MEMBERS(0x01000000, true),
    USE_VAD(0x02000000, true),
    PRIORITY_SPEAKER(0x00000100, true),
    CHANGE_NICKNAME(0x04000000, false),
    MANAGE_NICKNAME(0x08000000, false),
    MANAGE_ROLES(0x10000000, true),
    MANAGE_WEBHOOKS(0x20000000, true),
    MANAGE_EMOJI(0x40000000, false);

    Permission(final int value, final boolean channel) {
        this.value = value;
        this.channel = channel;
    }

    @Getter
    private final int value;
    @Getter
    private final boolean channel;

    /* Static convenience methods */

    public static Set<Permission> toSet(final long asLong) {
        final Set<Permission> perms = EnumSet.noneOf(Permission.class);

        for (final Permission p : values()) {
            if ((asLong & p.value) == p.value) {
                perms.add(p);
            }
        }

        return perms;
    }
    
    public static long from(@Nonnull final Iterable<Permission> permissions) {
        long result = 0;
        for(final Permission permission : permissions) {
            result |= permission.value;
        }
        return result;
    }
    
    public static long from(@Nonnull final Permission... permissions) {
        long result = 0;
        for(final Permission permission : permissions) {
            result |= permission.value;
        }
        return result;
    }
}
