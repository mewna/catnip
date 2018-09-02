package com.mewna.mew.shard;

/**
 * @author amy
 * @since 9/2/18.
 */
public interface DiscordEvent {
    // @formatter:off
    String CHANNEL_CREATE               = "CHANNEL_CREATE";
    String CHANNEL_DELETE               = "CHANNEL_DELETE";
    String CHANNEL_UPDATE               = "CHANNEL_UPDATE";
    String GUILD_CREATE                 = "GUILD_CREATE";
    String GUILD_DELETE                 = "GUILD_DELETE";
    String GUILD_UPDATE                 = "GUILD_UPDATE";
    String GUILD_EMOJIS_UPDATE          = "GUILD_EMOJIS_UPDATE";
    String GUILD_MEMBER_ADD             = "GUILD_MEMBER_ADD";
    String GUILD_MEMBER_REMOVE          = "GUILD_MEMBER_REMOVE";
    String GUILD_MEMBER_UPDATE          = "GUILD_MEMBER_UPDATE";
    String GUILD_MEMBERS_CHUNK          = "GUILD_MEMBERS_CHUNK";
    String GUILD_ROLE_CREATE            = "GUILD_ROLE_CREATE";
    String GUILD_ROLE_DELETE            = "GUILD_ROLE_DELETE";
    String GUILD_ROLE_UPDATE            = "GUILD_ROLE_UPDATE";
    String USER_UPDATE                  = "USER_UPDATE";
    String VOICE_SERVER_UPDATE          = "VOICE_SERVER_UPDATE";
    String MESSAGE_CREATE               = "MESSAGE_CREATE";
    String MESSAGE_DELETE               = "MESSAGE_DELETE";
    String MESSAGE_DELETE_BULK          = "MESSAGE_DELETE_BULK";
    String MESSAGE_UPDATE               = "MESSAGE_UPDATE";
    String GUILD_SYNC                   = "GUILD_SYNC";
    String GUILD_BAN_ADD                = "GUILD_BAN_ADD";
    String GUILD_BAN_REMOVE             = "GUILD_BAN_REMOVE";
    String MESSAGE_REACTION_ADD         = "MESSAGE_REACTION_ADD";
    String MESSAGE_REACTION_REMOVE      = "MESSAGE_REACTION_REMOVE";
    String MESSAGE_REACTION_REMOVE_ALL  = "MESSAGE_REACTION_REMOVE_ALL";
    String PRESENCE_UPDATE              = "PRESENCE_UPDATE";
    String READY                        = "READY";
    String TYPING_START                 = "TYPING_START";
    String VOICE_STATE_UPDATE           = "VOICE_STATE_UPDATE";
    // @formatter:on
}
