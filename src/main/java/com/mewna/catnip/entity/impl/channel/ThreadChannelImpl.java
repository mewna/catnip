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

package com.mewna.catnip.entity.impl.channel;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.Timestamped;
import com.mewna.catnip.entity.channel.ThreadChannel;
import com.mewna.catnip.entity.guild.PermissionOverride;
import lombok.*;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * @author amy
 * @since 5/1/21.
 */
@Getter
@Setter
@Builder
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public class ThreadChannelImpl implements ThreadChannel {
    private transient Catnip catnip;
    
    private ChannelType type;
    private long idAsLong;
    private String name;
    private long guildIdAsLong;
    private int position;
    private long parentIdAsLong;
    private List<PermissionOverride> overrides;
    private String topic;
    private boolean nsfw;
    private int rateLimitPerUser;
    private int messageCount;
    private int memberCount;
    private ThreadMember member;
    private ThreadMetadata metadata;
    private long ownerIdAsLong;
    
    @Override
    public void catnip(@Nonnull final Catnip catnip) {
        this.catnip = catnip;
    }
    
    @Getter
    @Setter
    @Builder
    @Accessors(fluent = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ThreadMemberImpl implements ThreadMember, Timestamped {
        private transient Catnip catnip;
        
        private long idAsLong;
        private long userIdAsLong;
        private String joinedAt;
        
        @Override
        public void catnip(@Nonnull final Catnip catnip) {
            this.catnip = catnip;
        }
        
        @Nullable
        @Override
        public OffsetDateTime joinedAt() {
            return parseTimestamp(joinedAt);
        }
    }
    
    @Getter
    @Setter
    @Builder
    @Accessors(fluent = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ThreadMetadataImpl implements ThreadMetadata, Timestamped {
        private boolean locked;
        private boolean archived;
        private long archiverIdAsLong;
        private String archiveTimestamp;
        private int autoArchiveDuration;
        
        @Override
        public OffsetDateTime archiveTimestamp() {
            return parseTimestamp(archiveTimestamp);
        }
    }
}
