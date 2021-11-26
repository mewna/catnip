/*
 * Copyright (c) 2021 amy, All rights reserved.
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

import com.mewna.catnip.entity.partials.GuildEntity;
import com.mewna.catnip.entity.partials.Snowflake;
import com.mewna.catnip.entity.partials.Timestamped;
import com.mewna.catnip.entity.user.User;
import lombok.Getter;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * @author amy
 * @since 11/25/21.
 */
public interface ScheduledEvent extends Snowflake, GuildEntity, Timestamped {
    @Nullable
    @CheckReturnValue
    default String channelId() {
        return Long.toUnsignedString(channelIdAsLong());
    }
    
    @CheckReturnValue
    long channelIdAsLong();
    
    @Nullable
    @CheckReturnValue
    default String creatorId() {
        return Long.toUnsignedString(creatorIdAsLong());
    }
    
    @CheckReturnValue
    long creatorIdAsLong();
    
    @CheckReturnValue
    String name();
    
    @Nullable
    @CheckReturnValue
    String description();
    
    @Nonnull
    @CheckReturnValue
    default OffsetDateTime scheduledStartTime() {
        return parseTimestamp(scheduledStartTimeRaw());
    }
    
    @Nonnull
    @CheckReturnValue
    String scheduledStartTimeRaw();
    
    @Nullable
    @CheckReturnValue
    default OffsetDateTime scheduledEndTime() {
        return parseTimestamp(scheduledEndTimeRaw());
    }
    
    @Nullable
    @CheckReturnValue
    String scheduledEndTimeRaw();
    
    @Nonnull
    @CheckReturnValue
    PrivacyLevel privacyLevel();
    
    @Nonnull
    @CheckReturnValue
    EventStatus status();
    
    @Nonnull
    @CheckReturnValue
    EventEntityType entityType();
    
    @Nullable
    @CheckReturnValue
    String entityId();
    
    @Nullable
    @CheckReturnValue
    EntityMetadata entityMetadata();
    
    @Nullable
    @CheckReturnValue
    User creator();
    
    @CheckReturnValue
    int userCount();
    
    enum PrivacyLevel {
        PUBLIC(1),
        GUILD_ONLY(2),
        ;
        
        @Getter
        private final int key;
        
        PrivacyLevel(final int key) {
            this.key = key;
        }
        
        @Nonnull
        public static PrivacyLevel byKey(final int key) {
            for(final PrivacyLevel level : values()) {
                if(level.key == key) {
                    return level;
                }
            }
            throw new IllegalArgumentException("No privacy level for key " + key);
        }
    }
    
    enum EventEntityType {
        NONE(0),
        STAGE_INSTANCE(1),
        VOICE(2),
        EXTERNAL(3),
        ;
        
        @Getter
        private final int key;
        
        EventEntityType(final int key) {
            this.key = key;
        }
        
        @Nonnull
        public static EventEntityType byKey(final int key) {
            for(final EventEntityType level : values()) {
                if(level.key == key) {
                    return level;
                }
            }
            throw new IllegalArgumentException("No event entity type for key " + key);
        }
    }
    
    enum EventStatus {
        SCHEDULED(1),
        ACTIVE(2),
        COMPLETED(3),
        CANCELED(4),
        ;
        
        @Getter
        private final int key;
        
        EventStatus(final int key) {
            this.key = key;
        }
        
        @Nonnull
        public static EventStatus byKey(final int key) {
            for(final EventStatus level : values()) {
                if(level.key == key) {
                    return level;
                }
            }
            throw new IllegalArgumentException("No event status for key " + key);
        }
    }
    
    interface EntityMetadata {
        @Nullable
        @CheckReturnValue
        List<String> speakerIds();
        
        @Nullable
        @CheckReturnValue
        String location();
    }
}
