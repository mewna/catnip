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

package com.mewna.catnip.rest;

import com.mewna.catnip.internal.CatnipImpl;
import com.mewna.catnip.rest.handler.*;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * @author amy
 * @since 9/1/18.
 */
@Accessors(fluent = true)
@SuppressWarnings({"WeakerAccess", "unused"})
public class Rest {
    @Getter
    private final RestChannel channel;
    @Getter
    private final RestGuild guild;
    @Getter
    private final RestUser user;
    @Getter
    private final RestEmoji emoji;
    @Getter
    private final RestInvite invite;
    @Getter
    private final RestVoice voice;
    @Getter
    private final RestWebhook webhook;
    @Getter
    private final RestStore store;
    
    public Rest(final CatnipImpl catnip) {
        channel = new RestChannel(catnip);
        guild = new RestGuild(catnip);
        user = new RestUser(catnip);
        emoji = new RestEmoji(catnip);
        invite = new RestInvite(catnip);
        voice = new RestVoice(catnip);
        webhook = new RestWebhook(catnip);
        store = new RestStore(catnip);
    }
}
