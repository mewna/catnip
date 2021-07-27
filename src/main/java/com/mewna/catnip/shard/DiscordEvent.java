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

package com.mewna.catnip.shard;

import com.mewna.catnip.entity.channel.Channel;
import com.mewna.catnip.entity.channel.ChannelPinsUpdate;
import com.mewna.catnip.entity.channel.ThreadChannel;
import com.mewna.catnip.entity.channel.WebhooksUpdate;
import com.mewna.catnip.entity.guild.*;
import com.mewna.catnip.entity.interaction.Interaction;
import com.mewna.catnip.entity.message.*;
import com.mewna.catnip.entity.misc.Ready;
import com.mewna.catnip.entity.misc.Resumed;
import com.mewna.catnip.entity.user.*;
import com.mewna.catnip.entity.voice.VoiceServerUpdate;
import com.mewna.catnip.shard.event.DoubleEventType;
import com.mewna.catnip.shard.event.EventType;

import static com.mewna.catnip.shard.event.DoubleEventTypeImpl.doubleEvent;
import static com.mewna.catnip.shard.event.EventTypeImpl.event;

/**
 * See the Discord docs for more information about each kind of event.
 *
 * @author amy
 * @since 9/2/18.
 */
@SuppressWarnings("unused")
public interface DiscordEvent {
    // @formatter:off
    EventType<Channel>                            CHANNEL_CREATE                = event(Raw.CHANNEL_CREATE, Channel.class);
    DoubleEventType<Channel, Channel>             CHANNEL_UPDATE                = doubleEvent(Raw.CHANNEL_UPDATE, Channel.class, Channel.class);
    EventType<Channel>                            CHANNEL_DELETE                = event(Raw.CHANNEL_DELETE, Channel.class);
    EventType<ChannelPinsUpdate>                  CHANNEL_PINS_UPDATE           = event(Raw.CHANNEL_PINS_UPDATE, ChannelPinsUpdate.class);
    EventType<WebhooksUpdate>                     WEBHOOKS_UPDATE               = event(Raw.WEBHOOKS_UPDATE, WebhooksUpdate.class);
    EventType<Guild>                              GUILD_CREATE                  = event(Raw.GUILD_CREATE, Guild.class);
    EventType<Guild>                              GUILD_AVAILABLE               = event(Raw.GUILD_AVAILABLE, Guild.class);
    EventType<Guild>                              GUILD_DELETE                  = event(Raw.GUILD_DELETE, Guild.class);
    EventType<UnavailableGuild>                   GUILD_UNAVAILABLE             = event(Raw.GUILD_UNAVAILABLE, UnavailableGuild.class);
    DoubleEventType<Guild, Guild>                 GUILD_UPDATE                  = doubleEvent(Raw.GUILD_UPDATE, Guild.class, Guild.class);
    EventType<EmojiUpdate>                        GUILD_EMOJIS_UPDATE           = event(Raw.GUILD_EMOJIS_UPDATE, EmojiUpdate.class);
    EventType<Member>                             GUILD_MEMBER_ADD              = event(Raw.GUILD_MEMBER_ADD, Member.class);
    EventType<Member>                             GUILD_MEMBER_REMOVE           = event(Raw.GUILD_MEMBER_REMOVE, Member.class);
    DoubleEventType<Member, PartialMember>        GUILD_MEMBER_UPDATE           = doubleEvent(Raw.GUILD_MEMBER_UPDATE, Member.class, PartialMember.class);
    EventType<Role>                               GUILD_ROLE_CREATE             = event(Raw.GUILD_ROLE_CREATE, Role.class);
    DoubleEventType<Role, Role>                   GUILD_ROLE_UPDATE             = doubleEvent(Raw.GUILD_ROLE_UPDATE, Role.class, Role.class);
    EventType<PartialRole>                        GUILD_ROLE_DELETE             = event(Raw.GUILD_ROLE_DELETE, PartialRole.class);
    DoubleEventType<User, User>                   USER_UPDATE                   = doubleEvent(Raw.USER_UPDATE, User.class, User.class);
    EventType<VoiceServerUpdate>                  VOICE_SERVER_UPDATE           = event(Raw.VOICE_SERVER_UPDATE, VoiceServerUpdate.class);
    EventType<Invite>                             INVITE_CREATE                 = event(Raw.INVITE_CREATE, Invite.class);
    EventType<Invite>                             INVITE_DELETE                 = event(Raw.INVITE_CREATE, Invite.class);
    EventType<Message>                            MESSAGE_CREATE                = event(Raw.MESSAGE_CREATE, Message.class);
    EventType<Message>                            MESSAGE_UPDATE                = event(Raw.MESSAGE_UPDATE, Message.class);
    EventType<MessageEmbedUpdate>                 MESSAGE_EMBEDS_UPDATE         = event(Raw.MESSAGE_EMBEDS_UPDATE, MessageEmbedUpdate.class);
    EventType<DeletedMessage>                     MESSAGE_DELETE                = event(Raw.MESSAGE_DELETE, DeletedMessage.class);
    EventType<BulkDeletedMessages>                MESSAGE_DELETE_BULK           = event(Raw.MESSAGE_DELETE_BULK, BulkDeletedMessages.class);
    EventType<String>                             GUILD_INTEGRATIONS_UPDATE     = event(Raw.GUILD_INTEGRATIONS_UPDATE, String.class);
    EventType<GatewayGuildBan>                    GUILD_BAN_ADD                 = event(Raw.GUILD_BAN_ADD, GatewayGuildBan.class);
    EventType<GatewayGuildBan>                    GUILD_BAN_REMOVE              = event(Raw.GUILD_BAN_REMOVE, GatewayGuildBan.class);
    EventType<ReactionUpdate>                     MESSAGE_REACTION_ADD          = event(Raw.MESSAGE_REACTION_ADD, ReactionUpdate.class);
    EventType<ReactionUpdate>                     MESSAGE_REACTION_REMOVE       = event(Raw.MESSAGE_REACTION_REMOVE, ReactionUpdate.class);
    EventType<ReactionUpdate>                     MESSAGE_REACTION_REMOVE_EMOJI = event(Raw.MESSAGE_REACTION_REMOVE_EMOJI, ReactionUpdate.class);
    EventType<BulkRemovedReactions>               MESSAGE_REACTION_REMOVE_ALL   = event(Raw.MESSAGE_REACTION_REMOVE_ALL, BulkRemovedReactions.class);
    DoubleEventType<Presence, PresenceUpdate>     PRESENCE_UPDATE               = doubleEvent(Raw.PRESENCE_UPDATE, Presence.class, PresenceUpdate.class);
    EventType<Ready>                              READY                         = event(Raw.READY, Ready.class);
    EventType<Resumed>                            RESUMED                       = event(Raw.RESUMED, Resumed.class);
    EventType<TypingUser>                         TYPING_START                  = event(Raw.TYPING_START, TypingUser.class);
    EventType<VoiceState>                         VOICE_STATE_UPDATE            = event(Raw.VOICE_STATE_UPDATE, VoiceState.class);
    EventType<Interaction>                        INTERACTION_CREATE            = event(Raw.INTERACTION_CREATE, Interaction.class);
    EventType<ThreadChannel>                      THREAD_CREATE                 = event(Raw.THREAD_CREATE, ThreadChannel.class);
    DoubleEventType<ThreadChannel, ThreadChannel> THREAD_UPDATE                 = doubleEvent(Raw.THREAD_UPDATE, ThreadChannel.class, ThreadChannel.class);
    EventType<ThreadChannel>                      THREAD_DELETE                 = event(Raw.THREAD_DELETE, ThreadChannel.class);
    // @formatter:on
    
    /**
     * Raw string values for the gateway events
     *
     * @author natanbc
     * @since 10/6/18.
     */
    interface Raw {
        // @formatter:off
        String CHANNEL_CREATE                = "CHANNEL_CREATE";
        String CHANNEL_DELETE                = "CHANNEL_DELETE";
        String CHANNEL_UPDATE                = "CHANNEL_UPDATE";
        String CHANNEL_PINS_UPDATE           = "CHANNEL_PINS_UPDATE";
        String WEBHOOKS_UPDATE               = "WEBHOOKS_UPDATE";
        String GUILD_CREATE                  = "GUILD_CREATE";
        String GUILD_AVAILABLE               = "GUILD_AVAILABLE";
        String GUILD_DELETE                  = "GUILD_DELETE";
        String GUILD_UNAVAILABLE             = "GUILD_UNAVAILABLE";
        String GUILD_UPDATE                  = "GUILD_UPDATE";
        String GUILD_EMOJIS_UPDATE           = "GUILD_EMOJIS_UPDATE";
        String GUILD_MEMBER_ADD              = "GUILD_MEMBER_ADD";
        String GUILD_MEMBER_REMOVE           = "GUILD_MEMBER_REMOVE";
        String GUILD_MEMBER_UPDATE           = "GUILD_MEMBER_UPDATE";
        String GUILD_MEMBERS_CHUNK           = "GUILD_MEMBERS_CHUNK";
        String GUILD_ROLE_CREATE             = "GUILD_ROLE_CREATE";
        String GUILD_ROLE_DELETE             = "GUILD_ROLE_DELETE";
        String GUILD_ROLE_UPDATE             = "GUILD_ROLE_UPDATE";
        String GUILD_INTEGRATIONS_UPDATE     = "GUILD_INTEGRATIONS_UPDATE";
        String USER_UPDATE                   = "USER_UPDATE";
        String VOICE_SERVER_UPDATE           = "VOICE_SERVER_UPDATE";
        String INVITE_CREATE                 = "INVITE_CREATE";
        String INVITE_DELETE                 = "INVITE_DELETE";
        String MESSAGE_CREATE                = "MESSAGE_CREATE";
        String MESSAGE_DELETE                = "MESSAGE_DELETE";
        String MESSAGE_DELETE_BULK           = "MESSAGE_DELETE_BULK";
        String MESSAGE_UPDATE                = "MESSAGE_UPDATE";
        /**
         * This is a special case of MESSAGE_UPDATE. The gateway will send a
         * partial MESSAGE_UPDATE in the case of resolving message embeds,
         * which cannot be filled by {@link Message} without a ton of extra
         * work and doing ugly things like marking message author as nullable
         * when it almost-always won't be null.
         */
        String MESSAGE_EMBEDS_UPDATE         = "MESSAGE_EMBEDS_UPDATE";
        String GUILD_BAN_ADD                 = "GUILD_BAN_ADD";
        String GUILD_BAN_REMOVE              = "GUILD_BAN_REMOVE";
        String MESSAGE_REACTION_ADD          = "MESSAGE_REACTION_ADD";
        String MESSAGE_REACTION_REMOVE       = "MESSAGE_REACTION_REMOVE";
        String MESSAGE_REACTION_REMOVE_EMOJI = "MESSAGE_REACTION_REMOVE_EMOJI";
        String MESSAGE_REACTION_REMOVE_ALL   = "MESSAGE_REACTION_REMOVE_ALL";
        String PRESENCE_UPDATE               = "PRESENCE_UPDATE";
        String READY                         = "READY";
        String RESUMED                       = "RESUMED";
        String TYPING_START                  = "TYPING_START";
        String VOICE_STATE_UPDATE            = "VOICE_STATE_UPDATE";
        /**
         * Jake has explicitly said that this is not for bots and will not be
         * documented, so we can safely ignore it. It's here for the sake of
         * completeness.
         *
         * See https://github.com/discordapp/discord-api-docs/issues/803
         */
        String GIFT_CODE_UPDATE              = "GIFT_CODE_UPDATE";
        String INTERACTION_CREATE            = "INTERACTION_CREATE";
        String APPLICATION_COMMAND_CREATE    = "APPLICATION_COMMAND_CREATE";
        String APPLICATION_COMMAND_UPDATE    = "APPLICATION_COMMAND_UPDATE";
        String APPLICATION_COMMAND_DELETE    = "APPLICATION_COMMAND_DELETE";
        String THREAD_CREATE                 = "THREAD_CREATE";
        String THREAD_UPDATE                 = "THREAD_UPDATE";
        String THREAD_DELETE                 = "THREAD_DELETE";
        String THREAD_LIST_SYNC              = "THREAD_LIST_SYNC";
        String THREAD_MEMBER_UPDATE          = "THREAD_MEMBER_UPDATE";
        String THREAD_MEMBERS_UPDATE         = "THREAD_MEMBERS_UPDATE";
        // @formatter:on
    }
}
