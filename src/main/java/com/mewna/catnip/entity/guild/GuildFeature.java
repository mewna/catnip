/*
 * Copyright (c) 2019 amy, All rights reserved.
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

package com.mewna.catnip.entity.guild;

import com.mewna.catnip.Catnip;

/**
 * @author amy
 * @since 8/19/19.
 */
public enum GuildFeature {
    INVITE_SPLASH,
    VIP_REGIONS,
    VANITY_URL,
    VERIFIED,
    PARTNERED,
    @Deprecated
    LURKABLE,
    COMMERCE,
    NEWS,
    DISCOVERABLE,
    @Deprecated
    FEATURABLE,
    ANIMATED_ICON,
    BANNER,
    /**
     * Replaces {@link #LURKABLE}. See https://github.com/discordapp/discord-api-docs/issues/1117#issuecomment-533757618
     */
    PUBLIC,
    /**
     * See https://github.com/mewna/catnip/issues/392
     */
    @Deprecated
    MEMBER_LIST_DISABLED,
    PUBLIC_DISABLED,
    WELCOME_SCREEN_ENABLED,
    ENABLED_DISCOVERABLE_BEFORE,
    
    /**
     * When no other feature matches.
     */
    UNKNOWN_FEATURE,;
    
    public static GuildFeature unknownValueOf(final Catnip catnip, final String value) {
        try {
            return valueOf(value);
        } catch(final Exception ignored) {
            catnip.logAdapter().warn("Unknown guild feature {}, returning {}!", value, UNKNOWN_FEATURE.name());
            return UNKNOWN_FEATURE;
        }
    }
}
