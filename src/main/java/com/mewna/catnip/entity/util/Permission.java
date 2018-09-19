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
