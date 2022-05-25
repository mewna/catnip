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
import com.mewna.catnip.entity.RequiresCatnip;
import com.mewna.catnip.entity.channel.StageChannel;
import com.mewna.catnip.entity.channel.VoiceChannel;
import com.mewna.catnip.entity.channel.Webhook;
import com.mewna.catnip.entity.guild.PermissionOverride;
import com.mewna.catnip.entity.message.Embed;
import com.mewna.catnip.entity.message.Message;
import com.mewna.catnip.entity.message.MessageOptions;
import com.mewna.catnip.entity.misc.Emoji;
import com.mewna.catnip.util.pagination.MessagePaginator;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import lombok.*;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author amy
 * @since 11/26/21.
 */
@Getter
@Setter
@Builder
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public class StageChannelImpl implements StageChannel, RequiresCatnip {
    private final ChannelType type = ChannelType.STAGE;
    
    private transient Catnip catnip;
    
    private long idAsLong;
    private String name;
    private long guildIdAsLong;
    private int position;
    private long parentIdAsLong;
    private List<PermissionOverride> overrides;
    private int bitrate;
    private int userLimit;
    
    @Override
    public void catnip(@Nonnull final Catnip catnip) {
        this.catnip = catnip;
        for(final PermissionOverride override : overrides) {
            if(override instanceof RequiresCatnip) {
                ((RequiresCatnip) override).catnip(catnip);
            }
        }
    }
    
    @Override
    public int hashCode() {
        return Long.hashCode(idAsLong);
    }
    
    @Override
    public boolean equals(final Object obj) {
        return obj instanceof VoiceChannel && ((VoiceChannel) obj).idAsLong() == idAsLong;
    }
    
    @Override
    public String toString() {
        return String.format("StageChannel (%s)", name);
    }
    
    @Nonnull
    @Override
    public Single<Message> sendMessage(@Nonnull final String content) {
        throw new UnsupportedOperationException("Not available on stage channels.");
    }
    
    @Nonnull
    @Override
    public Single<Message> sendMessage(@Nonnull final Embed embed) {
        throw new UnsupportedOperationException("Not available on stage channels.");
    }
    
    @Nonnull
    @Override
    public Single<Message> sendMessage(@Nonnull final Message message) {
        throw new UnsupportedOperationException("Not available on stage channels.");
    }
    
    @Nonnull
    @Override
    public Single<Message> sendMessage(@Nonnull final MessageOptions options) {
        throw new UnsupportedOperationException("Not available on stage channels.");
    }
    
    @Nonnull
    @Override
    public Single<Message> editMessage(@Nonnull final String messageId, @Nonnull final String content) {
        throw new UnsupportedOperationException("Not available on stage channels.");
    }
    
    @Nonnull
    @Override
    public Single<Message> editMessage(@Nonnull final String messageId, @Nonnull final Embed embed) {
        throw new UnsupportedOperationException("Not available on stage channels.");
    }
    
    @Nonnull
    @Override
    public Single<Message> editMessage(@Nonnull final String messageId, @Nonnull final Message message) {
        throw new UnsupportedOperationException("Not available on stage channels.");
    }
    
    @Nonnull
    @Override
    public Completable deleteMessage(@Nonnull final String messageId, @Nullable final String reason) {
        throw new UnsupportedOperationException("Not available on stage channels.");
    }
    
    @Nonnull
    @Override
    public Completable deleteMessage(@Nonnull final String messageId) {
        throw new UnsupportedOperationException("Not available on stage channels.");
    }
    
    @Nonnull
    @Override
    public Completable addReaction(@Nonnull final String messageId, @Nonnull final String emoji) {
        throw new UnsupportedOperationException("Not available on stage channels.");
    }
    
    @Nonnull
    @Override
    public Completable addReaction(@Nonnull final String messageId, @Nonnull final Emoji emoji) {
        throw new UnsupportedOperationException("Not available on stage channels.");
    }
    
    @Nonnull
    @Override
    public Completable deleteOwnReaction(@Nonnull final String messageId, @Nonnull final String emoji) {
        throw new UnsupportedOperationException("Not available on stage channels.");
    }
    
    @Nonnull
    @Override
    public Completable deleteOwnReaction(@Nonnull final String messageId, @Nonnull final Emoji emoji) {
        throw new UnsupportedOperationException("Not available on stage channels.");
    }
    
    @Nonnull
    @Override
    public Completable deleteUserReaction(@Nonnull final String messageId, @Nonnull final String userId, @Nonnull final String emoji) {
        throw new UnsupportedOperationException("Not available on stage channels.");
    }
    
    @Nonnull
    @Override
    public Completable deleteUserReaction(@Nonnull final String messageId, @Nonnull final String userId, @Nonnull final Emoji emoji) {
        throw new UnsupportedOperationException("Not available on stage channels.");
    }
    
    @Nonnull
    @Override
    public Completable bulkRemoveReaction(@Nonnull final String messageId) {
        throw new UnsupportedOperationException("Not available on stage channels.");
    }
    
    @Nonnull
    @Override
    public Completable triggerTypingIndicator() {
        throw new UnsupportedOperationException("Not available on stage channels.");
    }
    
    @Nonnull
    @Override
    public Single<Message> fetchMessage(@Nonnull final String messageId) {
        throw new UnsupportedOperationException("Not available on stage channels.");
    }
    
    @Nonnull
    @Override
    public MessagePaginator fetchMessages() {
        throw new UnsupportedOperationException("Not available on stage channels.");
    }
    
    @Nonnull
    @Override
    public Observable<Webhook> fetchWebhooks() {
        throw new UnsupportedOperationException("Not available on stage channels.");
    }
    
    @Override
    public boolean isGuildMessageChannel() {
        return false;
    }
}
