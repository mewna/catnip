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

package com.mewna.catnip.entity.builder.command;

import com.mewna.catnip.entity.impl.interaction.InteractionApplicationCommandCallbackDataImpl;
import com.mewna.catnip.entity.interaction.InteractionApplicationCommandCallbackData;
import com.mewna.catnip.entity.message.Embed;
import com.mewna.catnip.entity.message.MentionParseFlag;
import com.mewna.catnip.entity.message.MessageFlag;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author amy
 * @since 12/12/20.
 */
@SuppressWarnings("unused")
public class InteractionApplicationCommandCallbackDataBuilder {
    private final List<Embed> embeds = new LinkedList<>();
    @SuppressWarnings("SetReplaceableByEnumSet")
    private final Set<MessageFlag> flags = new HashSet<>();
    @SuppressWarnings("SetReplaceableByEnumSet")
    private final Set<MentionParseFlag> allowedMentions = new HashSet<>();
    private boolean tts;
    private String content;
    
    public InteractionApplicationCommandCallbackDataBuilder tts(final boolean tts) {
        this.tts = tts;
        return this;
    }
    
    public InteractionApplicationCommandCallbackDataBuilder content(@Nullable final String content) {
        this.content = content;
        return this;
    }
    
    public InteractionApplicationCommandCallbackDataBuilder addEmbed(@Nonnull final Embed embed) {
        embeds.add(embed);
        return this;
    }
    
    public InteractionApplicationCommandCallbackDataBuilder flags(@SuppressWarnings("TypeMayBeWeakened")
                                                                  @Nonnull final Set<MessageFlag> flags) {
        this.flags.clear();
        this.flags.addAll(flags);
        return this;
    }
    
    public InteractionApplicationCommandCallbackDataBuilder allowedMentions(@SuppressWarnings("TypeMayBeWeakened")
                                                                            @Nonnull final Set<MentionParseFlag> allowedMentions) {
        this.allowedMentions.clear();
        this.allowedMentions.addAll(allowedMentions);
        return this;
    }
    
    public InteractionApplicationCommandCallbackData build() {
        return InteractionApplicationCommandCallbackDataImpl.builder()
                .tts(tts)
                .content(content)
                .flags(flags)
                .embeds(embeds)
                .allowedMentions(allowedMentions)
                .build();
    }
}
