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

package com.mewna.catnip.shard;

import lombok.Getter;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * A list of "gateway intents" that tell Discord which events you do and don't
 * want to receive. Events are grouped into intents, which can be thought of as
 * high-level categories of events. Some intents can be privileged. Privileged
 * intents can only be set by bots in <=100 guilds; bots will be prevented from
 * joining more guilds when attempting to use privileged intents unless they
 * are specifically whitelisted by Discord.
 *
 * @author amy
 * @since 1/16/20.
 */
@Accessors(fluent = true)
public enum GatewayIntent {
    /**
     * Encapsulates the following events:
     * <ul>
     *     <li>{@link DiscordEvent#GUILD_CREATE}</li>
     *     <li>{@link DiscordEvent#GUILD_DELETE}</li>
     *     <li>{@link DiscordEvent#GUILD_ROLE_CREATE}</li>
     *     <li>{@link DiscordEvent#GUILD_ROLE_UPDATE}</li>
     *     <li>{@link DiscordEvent#GUILD_ROLE_DELETE}</li>
     *     <li>{@link DiscordEvent#CHANNEL_CREATE}</li>
     *     <li>{@link DiscordEvent#CHANNEL_UPDATE}</li>
     *     <li>{@link DiscordEvent#CHANNEL_DELETE}</li>
     *     <li>{@link DiscordEvent#CHANNEL_PINS_UPDATE}</li>
     * </ul>
     */
    @SuppressWarnings("PointlessBitwiseExpression")
    GUILDS(1 << 0, false),
    
    /**
     * <ul>
     *     <li>{@link DiscordEvent#GUILD_MEMBER_ADD}</li>
     *     <li>{@link DiscordEvent#GUILD_MEMBER_UPDATE}</li>
     *     <li>{@link DiscordEvent#GUILD_MEMBER_REMOVE}</li>
     * </ul>
     */
    GUILD_MEMBERS(1 << 1, false),
    
    /**
     * <ul>
     *     <li>{@link DiscordEvent#GUILD_BAN_ADD}</li>
     *     <li>{@link DiscordEvent#GUILD_BAN_REMOVE}</li>
     * </ul>
     */
    GUILD_BANS(1 << 2, false),
    
    /**
     * <ul>
     *     <li>{@link DiscordEvent#GUILD_EMOJIS_UPDATE}</li>
     * </ul>
     */
    GUILD_EMOJIS(1 << 3, false),
    
    /**
     * <ul>
     *     <li>{@link DiscordEvent#GUILD_INTEGRATIONS_UPDATE}</li>
     * </ul>
     */
    GUILD_INTEGRATIONS(1 << 4, false),
    
    /**
     * <ul>
     *     <li>{@link DiscordEvent#WEBHOOKS_UPDATE}</li>
     * </ul>
     */
    GUILD_WEBHOOKS(1 << 5, false),
    
    /**
     * <ul>
     *     <li><code>INVITE_CREATE</code></li>
     *     <li><code>INVITE_DELETE</code></li>
     * </ul>
     * Note that these events are currently undocumented!
     */
    GUILD_INVITES(1 << 6, false),
    
    /**
     * <ul>
     *     <li>{@link DiscordEvent#VOICE_STATE_UPDATE}</li>
     * </ul>
     */
    GUILD_VOICE_STATES(1 << 7, false),
    
    /**
     * <ul>
     *     <li>{@link DiscordEvent#PRESENCE_UPDATE}</li>
     * </ul>
     * Note that bots cannot receive presence updates in DMs.<br />
     * <strong>This is a privileged intent.</strong>
     */
    GUILD_PRESENCES(1 << 8, true),
    
    /**
     * <ul>
     *     <li>{@link DiscordEvent#MESSAGE_CREATE}</li>
     *     <li>{@link DiscordEvent#MESSAGE_UPDATE}</li>
     *     <li>{@link DiscordEvent#MESSAGE_DELETE}</li>
     * </ul>
     */
    GUILD_MESSAGES(1 << 9, false),
    
    /**
     * <ul>
     *     <li>{@link DiscordEvent#MESSAGE_REACTION_ADD}</li>
     *     <li>{@link DiscordEvent#MESSAGE_REACTION_REMOVE}</li>
     *     <li>{@link DiscordEvent#MESSAGE_REACTION_REMOVE_ALL}</li>
     *     <li><code>MESSAGE_REACTION_REMOVE_EMOJI</code></li>
     * </ul>
     * Note that <code>MESSAGE_REACTION_REMOVE_EMOJI</code> is currently
     * undocumented pending release of the corresponding REST endpoint.
     */
    GUILD_MESSAGE_REACTIONS(1 << 10, false),
    
    /**
     * <ul>
     *     <li>{@link DiscordEvent#TYPING_START}</li>
     * </ul>
     * Note that this toggles <code>TYPING_START</code> events <strong>in
     * guilds only.</strong>
     */
    GUILD_MESSAGE_TYPING(1 << 11, false),
    
    /**
     * <ul>
     *     <li>{@link DiscordEvent#CHANNEL_CREATE}</li>
     *     <li>{@link DiscordEvent#MESSAGE_CREATE}</li>
     *     <li>{@link DiscordEvent#MESSAGE_UPDATE}</li>
     *     <li>{@link DiscordEvent#MESSAGE_DELETE}</li>
     * </ul>
     */
    DIRECT_MESSAGES(1 << 12, false),
    
    /**
     * <ul>
     *     <li>{@link DiscordEvent#MESSAGE_REACTION_ADD}</li>
     *     <li>{@link DiscordEvent#MESSAGE_REACTION_REMOVE}</li>
     *     <li>{@link DiscordEvent#MESSAGE_REACTION_REMOVE_ALL}</li>
     *     <li><code>MESSAGE_REACTION_REMOVE_EMOJI</code></li>
     * </ul>
     * Note that <code>MESSAGE_REACTION_REMOVE_EMOJI</code> is currently
     * undocumented pending release of the corresponding REST endpoint.
     */
    DIRECT_MESSAGE_REACTIONS(1 << 13, false),
    
    /**
     * <ul>
     *     <li>{@link DiscordEvent#TYPING_START}</li>
     * </ul>
     * Note that this toggles <code>TYPING_START</code> events <strong>in
     * direct messages only.</strong>
     */
    DIRECT_MESSAGE_TYPING(1 << 14, false),
    ;
    public static final Set<GatewayIntent> ALL_INTENTS = Set.of(values());
    @Getter
    private final int value;
    @Getter
    private final boolean privileged;
    
    GatewayIntent(final int value, final boolean privileged) {
        this.value = value;
        this.privileged = privileged;
    }
    
    public static long from(@Nonnull final Iterable<GatewayIntent> intents) {
        long result = 0;
        for(final GatewayIntent intent : intents) {
            result |= intent.value;
        }
        return result;
    }
}
