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

package com.mewna.catnip.entity.impl.user;

import com.grack.nanojson.JsonObject;
import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.RequiresCatnip;
import com.mewna.catnip.entity.misc.Emoji.ActivityEmoji;
import com.mewna.catnip.entity.user.Presence;
import lombok.*;
import lombok.experimental.Accessors;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

/**
 * @author amy
 * @since 9/21/18.
 */
@Getter
@Setter
@Builder
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("WeakerAccess")
public class PresenceImpl implements Presence, RequiresCatnip {
    private transient Catnip catnip;
    
    private OnlineStatus status;
    private List<Activity> activities;
    private OnlineStatus mobileStatus;
    private OnlineStatus webStatus;
    private OnlineStatus desktopStatus;
    
    @Override
    public void catnip(@Nonnull final Catnip catnip) {
        this.catnip = catnip;
    }
    
    @Nonnull
    @CheckReturnValue
    public JsonObject asPresenceUpdateJson() {
        final var builder = JsonObject.builder()
                .value("status", status.asString());
        
        if (status == OnlineStatus.IDLE) {
            builder.value("since", System.currentTimeMillis())
                    .value("afk", true);
        } else {
            builder.nul("since")
                    .value("afk", false);
        }
        
        if (!activities.isEmpty()) {
            final var activity = activities.get(0);
            builder.object("game")
                    .value("name", activity.name())
                    .value("type", activity.type().id());
            
            if (activity.url() != null) {
                builder.value("url", activity.url());
            }
            
            builder.end();
        } else {
            builder.nul("game");
        }
        
        return builder.done();
    }
    
    @Getter
    @Setter
    @Builder
    @Accessors(fluent = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityTimestampsImpl implements ActivityTimestamps {
        private long start;
        private long end;
    }
    
    @Getter
    @Setter
    @Builder
    @Accessors(fluent = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityPartyImpl implements ActivityParty {
        private String id;
        private int currentSize;
        private int maxSize;
    }
    
    @Getter
    @Setter
    @Builder
    @Accessors(fluent = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityAssetsImpl implements ActivityAssets {
        private String largeImage;
        private String largeText;
        private String smallImage;
        private String smallText;
    }
    
    @Getter
    @Setter
    @Builder
    @Accessors(fluent = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivitySecretsImpl implements ActivitySecrets {
        private String join;
        private String spectate;
        private String match;
    }
    
    @Getter
    @Setter
    @Builder
    @Accessors(fluent = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityImpl implements Activity {
        private String name;
        private ActivityType type;
        private String url;
        private ActivityTimestamps timestamps;
        private long applicationIdAsLong;
        private String details;
        private String state;
        private ActivityEmoji emoji;
        private ActivityParty party;
        private ActivityAssets assets;
        private ActivitySecrets secrets;
        private boolean instance;
        private Set<ActivityFlag> flags;
    }
}
