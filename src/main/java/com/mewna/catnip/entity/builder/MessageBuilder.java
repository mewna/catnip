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

import com.mewna.catnip.entity.impl.MessageImpl;
import com.mewna.catnip.entity.message.Embed;
import com.mewna.catnip.entity.message.Message;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;

/**
 * Build a new message to be used via the REST API.
 *
 * @author amy
 * @since 9/2/18.
 */
@Setter(onParam_ = @Nullable, onMethod_ = {@CheckReturnValue, @Nonnull})
@NoArgsConstructor
@Accessors(fluent = true, chain = true)
@SuppressWarnings("unused")
public class MessageBuilder {
    private String content;
    private Embed embed;
    
    /**
     * Create a new MessageBuilder from the specified message.
     *
     * @param from The message to copy.
     */
    public MessageBuilder(final Message from) {
        content = from.content();
        embed = !from.embeds().isEmpty() ? from.embeds().get(0) : null;
    }
    
    /**
     * @return A new message containing the contents specified.
     */
    @Nonnull
    @CheckReturnValue
    public Message build() {
        final MessageImpl m = new MessageImpl();
        m.content(content);
        if(embed != null) {
            m.embeds(Collections.singletonList(embed));
        }
        return m;
    }
}
