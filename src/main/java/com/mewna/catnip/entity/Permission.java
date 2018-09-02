package com.mewna.catnip.entity;

/**
 * @author Julia Rogers
 * @since 9/2/18
 */
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
    MEANAGE_EMOJI(0x40000000, false);

    Permission(int value, boolean channel) {
        this.value = value;
        this.channel = channel;
    }

    private final int value;
    private final boolean channel;

    public int getValue() {
        return value;
    }

    public boolean appliesToChannel() {
        return channel;
    }
}
