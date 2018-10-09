package com.mewna.catnip.shard;

import com.mewna.catnip.entity.channel.Channel;
import com.mewna.catnip.entity.guild.*;
import com.mewna.catnip.entity.message.*;
import com.mewna.catnip.entity.misc.Ready;
import com.mewna.catnip.entity.user.Presence;
import com.mewna.catnip.entity.user.TypingUser;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.entity.user.VoiceState;
import com.mewna.catnip.entity.voice.VoiceServerUpdate;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

import static com.mewna.catnip.shard.EventTypeImpl.event;
import static com.mewna.catnip.shard.EventTypeImpl.notFired;

/**
 * Question: "WHY IS THIS NOT ENUM AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
 * Answer: "Vert.x uses strings for event consumption"
 *
 * @author amy
 * @since 9/2/18.
 */
public interface DiscordEvent {
    // @formatter:off
    EventType<Channel> CHANNEL_CREATE                            = event(Raw.CHANNEL_CREATE, Channel.class);
    EventType<Channel> CHANNEL_UPDATE                            = event(Raw.CHANNEL_UPDATE, Channel.class);
    EventType<Channel> CHANNEL_DELETE                            = event(Raw.CHANNEL_DELETE, Channel.class);
    EventType<Guild> GUILD_CREATE                                = event(Raw.GUILD_CREATE, Guild.class);
    EventType<Guild> GUILD_UPDATE                                = event(Raw.GUILD_UPDATE, Guild.class);
    EventType<UnavailableGuild> GUILD_DELETE                     = event(Raw.GUILD_DELETE, UnavailableGuild.class);
    //TODO: change type when the event is implemented
    EventType<Void> GUILD_EMOJIS_UPDATE                          = notFired(Raw.GUILD_EMOJIS_UPDATE);
    EventType<Member> GUILD_MEMBER_ADD                           = event(Raw.GUILD_MEMBER_ADD, Member.class);
    EventType<Member> GUILD_MEMBER_REMOVE                        = event(Raw.GUILD_MEMBER_REMOVE, Member.class);
    EventType<PartialMember> GUILD_MEMBER_UPDATE                 = event(Raw.GUILD_MEMBER_UPDATE, PartialMember.class);
    EventType<Role> GUILD_ROLE_CREATE                            = event(Raw.GUILD_ROLE_CREATE, Role.class);
    EventType<Role> GUILD_ROLE_UPDATE                            = event(Raw.GUILD_ROLE_UPDATE, Role.class);
    EventType<PartialRole> GUILD_ROLE_DELETE                     = event(Raw.GUILD_ROLE_DELETE, PartialRole.class);
    EventType<User> USER_UPDATE                                  = event(Raw.USER_UPDATE, User.class);
    EventType<VoiceServerUpdate> VOICE_SERVER_UPDATE             = event(Raw.VOICE_SERVER_UPDATE, VoiceServerUpdate.class);
    EventType<Message> MESSAGE_CREATE                            = event(Raw.MESSAGE_CREATE, Message.class);
    EventType<Message> MESSAGE_UPDATE                            = event(Raw.MESSAGE_UPDATE, Message.class);
    EventType<MessageEmbedUpdate> MESSAGE_EMBEDS_UPDATE          = event(Raw.MESSAGE_EMBEDS_UPDATE, MessageEmbedUpdate.class);
    EventType<DeletedMessage> MESSAGE_DELETE                     = event(Raw.MESSAGE_DELETE, DeletedMessage.class);
    EventType<BulkDeletedMessages> MESSAGE_DELETE_BULK           = event(Raw.MESSAGE_DELETE_BULK, BulkDeletedMessages.class);
    //TODO add this event (or remove this field)
    EventType<Void> GUILD_SYNC                                   = notFired(Raw.GUILD_SYNC);
    EventType<GatewayGuildBan> GUILD_BAN_ADD                     = event(Raw.GUILD_BAN_ADD, GatewayGuildBan.class);
    EventType<GatewayGuildBan> GUILD_BAN_REMOVE                  = event(Raw.GUILD_BAN_REMOVE, GatewayGuildBan.class);
    EventType<ReactionUpdate> MESSAGE_REACTION_ADD               = event(Raw.MESSAGE_REACTION_ADD, ReactionUpdate.class);
    EventType<ReactionUpdate> MESSAGE_REACTION_REMOVE            = event(Raw.MESSAGE_REACTION_REMOVE, ReactionUpdate.class);
    EventType<BulkRemovedReactions> MESSAGE_REACTION_REMOVE_ALL  = event(Raw.MESSAGE_REACTION_REMOVE_ALL, BulkRemovedReactions.class);
    EventType<Presence> PRESENCE_UPDATE                          = event(Raw.PRESENCE_UPDATE, Presence.class);
    EventType<Ready> READY                                       = event(Raw.READY, Ready.class);
    EventType<TypingUser> TYPING_START                           = event(Raw.TYPING_START, TypingUser.class);
    EventType<VoiceState> VOICE_STATE_UPDATE                     = event(Raw.VOICE_STATE_UPDATE, VoiceState.class);
    /**
     * Raw string values for the gateway events
     *
     * @author natanbc
     * @since 10/6/18.
     */
    interface Raw {
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
        /**
         * This is a special case of MESSAGE_UPDATE. The gateway will send a
         * partial MESSAGE_UPDATE in the case of resolving message embeds,
         * which cannot be filled by {@link Message} without a ton of extra
         * work and doing ugly things like marking message author as nullable
         * when it almost-always won't be null.
         */
        String MESSAGE_EMBEDS_UPDATE        = "MESSAGE_EMBEDS_UPDATE";
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
    // @formatter:on
    
    /**
     * Marker for statically validating event types on handlers.
     *
     * @param <T> Type of the event fired.
     *
     * @author natanbc
     * @since 10/6/18.
     */
    interface EventType<T> {
        /**
         * Key used in the event bus.
         *
         * @return Key where this event is fired in the bus.
         */
        @Nonnull
        @CheckReturnValue
        String key();
        
        /**
         * Class of the event payload.
         *
         * @return Class of the payload fired for this event.
         */
        @Nonnull
        @CheckReturnValue
        Class<T> payloadClass();
    }
}
