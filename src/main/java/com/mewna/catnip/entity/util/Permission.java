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
import java.util.EnumSet;
import java.util.Set;

/**
 * @author Julia Rogers
 * @since 9/2/18
 */
@Accessors(fluent = true)
public enum Permission {
    CREATE_INSTANT_INVITE(1L << 0, true, "Create Instant Invite"),
    KICK_MEMBERS(1L << 1, false, "Kick Members"),
    BAN_MEMBERS(1L << 2, false, "Ban Members"),
    ADMINISTRATOR(1L << 3, false, "Administrator"),
    MANAGE_CHANNELS(1L << 4, true, "Manage Channels"),
    MANAGE_GUILD(1L << 5, false, "Manage Server"),
    ADD_REACTIONS(1L << 6, true, "Add Reactions"),
    VIEW_AUDIT_LOG(1L << 7, false, "View Audit Log"),
    PRIORITY_SPEAKER(1L << 8, true, "Priority Speaker"),
    STREAM(1L << 9, true, "Video"),
    VIEW_CHANNEL(1L << 10, true, "Read Text Channels & See Voice Channels"),
    SEND_MESSAGES(1L << 11, true, "Send Messages"),
    SEND_TTS_MESSAGES(1L << 12, true, "Send TTS Messages"),
    MANAGE_MESSAGES(1L << 13, true, "Manage Messages"),
    EMBED_LINKS(1L << 14, true, "Embed Links"),
    ATTACH_FILES(1L << 15, true, "Attach Files"),
    READ_MESSAGE_HISTORY(1L << 16, true, "Read History"),
    MENTION_EVERYONE(1L << 17, true, "Mention @everyone, @here and All Roles"),
    USE_EXTERNAL_EMOJIS(1L << 18, true, "Use External Emojis"),
    VIEW_GUILD_INSIGHTS(1L << 19, false, "View Server Insights"),
    CONNECT(1L << 20, true, "Connect"),
    SPEAK(1L << 21, true, "Speak"),
    MUTE_MEMBERS(1L << 22, true, "Mute Members"),
    DEAFEN_MEMBERS(1L << 23, true, "Deafen Members"),
    MOVE_MEMBERS(1L << 24, true, "Move Members"),
    USE_VAD(1L << 25, true, "Use Voice Activity"),
    CHANGE_NICKNAME(1L << 26, false, "Change Nickname"),
    MANAGE_NICKNAMES(1L << 27, false, "Manage Nicknames"),
    MANAGE_ROLES(1L << 28, true, "Manage Roles"),
    MANAGE_WEBHOOKS(1L << 29, true, "Manage Webhooks"),
    MANAGE_EMOJIS_AND_STICKERS(1L << 30, false, "Manage Emojis & Stickers"),
    USE_APPLICATION_COMMANDS(1L << 31, false, "Use Slash Commands"),
    MANAGE_EVENTS(1L << 33, false, "Manage Guild Events"), // This permission is still an experiment, but will come soon
    ;
    
    public static final long ALL = from(values());
    public static final long NONE = 0;
    @Getter
    private final long value;
    @Getter
    private final boolean channel;
    @Getter
    private final String permName;
    
    Permission(final long value, final boolean channel, final String permName) {
        this.value = value;
        this.channel = channel;
        this.permName = permName;
    }
    
    public static Set<Permission> toSet(final long asLong) {
        final Set<Permission> perms = EnumSet.noneOf(Permission.class);
        
        for(final Permission p : values()) {
            if((asLong & p.value) == p.value) {
                perms.add(p);
            }
        }
        
        return perms;
    }
    
    /* Static convenience methods */
    
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
    
    public boolean isPresent(final long permissions) {
        return (permissions & value) == value;
    }
}
