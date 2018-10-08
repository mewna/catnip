package com.mewna.catnip.entity.guild.audit;

import lombok.Getter;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * @author SamOphis
 * @since 10/07/18
 */
@SuppressWarnings("unused")
public enum ActionType {
    GUILD_UPDATE(1),
    CHANNEL_CREATE(10),
    CHANNEL_UPDATE(11),
    CHANNEL_DELETE(12),
    CHANNEL_OVERWRITE_CREATE(13),
    CHANNEL_OVERWRITE_UPDATE(14),
    CHANNEL_OVERWRITE_DELETE(15),
    MEMBER_KICK(20),
    MEMBER_PRUNE(21),
    MEMBER_BAN_ADD(22),
    MEMBER_BAN_REMOVE(23),
    MEMBER_UPDATE(24),
    MEMBER_ROLE_UPDATE(25),
    ROLE_CREATE(30),
    ROLE_UPDATE(31),
    ROLE_DELETE(32),
    INVITE_CREATE(40),
    INVITE_UPDATE(41),
    INVITE_DELETE(42),
    WEBHOOK_CREATE(50),
    WEBHOOK_UPDATE(51),
    WEBHOOK_DELETE(52),
    EMOJI_CREATE(60),
    EMOJI_UPDATE(61),
    EMOJI_DELETE(62),
    MESSAGE_DELETE(72);
    
    @Getter
    private final int value;
    
    ActionType(final int value) {
        this.value = value;
    }
    
    @Nonnull
    @CheckReturnValue
    public static ActionType byKey(final int value) {
        for (final ActionType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("No action type for value: " + value);
    }
}
