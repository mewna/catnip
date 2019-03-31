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

package com.mewna.catnip.entity.builder;

import com.mewna.catnip.entity.user.ActivityImpl;
import com.mewna.catnip.entity.user.Presence;
import com.mewna.catnip.entity.user.Presence.Activity;
import com.mewna.catnip.entity.user.Presence.ActivityType;
import com.mewna.catnip.entity.user.Presence.OnlineStatus;
import com.mewna.catnip.entity.user.PresenceImpl;

/**
 * @author SamOphis
 * @since 10/12/2018
 */
@SuppressWarnings("unused")
public class PresenceBuilder {
    private OnlineStatus status;
    private ActivityType type;
    private String name;
    private String url;
    
    public PresenceBuilder(final Presence presence) {
        status = presence.status();
        final Activity activity = presence.activity();
        if(activity != null) {
            type = activity.type();
            name = activity.name();
            url = activity.url();
        }
    }
    
    public PresenceBuilder() {
    }
    
    public Presence build() {
        final Activity activity = name != null && type != null
                ? ActivityImpl.create()
                .name(name)
                .type(type)
                .url(url)
                : null;
        return PresenceImpl.create()
                .status(status)
                .activity(activity);
    }
    
    public PresenceBuilder status(final OnlineStatus status) {
        this.status = status;
        return this;
    }
    
    public PresenceBuilder type(final ActivityType type) {
        this.type = type;
        return this;
    }
    
    public PresenceBuilder name(final String name) {
        this.name = name;
        return this;
    }
    
    public PresenceBuilder url(final String url) {
        this.url = url;
        return this;
    }
}
