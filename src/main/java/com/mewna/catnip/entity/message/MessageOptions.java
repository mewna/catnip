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

package com.mewna.catnip.entity.message;

import com.google.common.collect.ImmutableList;
import com.mewna.catnip.entity.impl.MessageImpl;
import io.vertx.core.buffer.Buffer;
import lombok.*;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Options used for creating a new message.
 *
 * @author SamOphis
 * @since 10/10/2018
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@Accessors(fluent = true)
@SuppressWarnings("unused")
public class MessageOptions {
    private String content;
    private Embed embed;
    
    @Setter(AccessLevel.NONE)
    private List<ImmutablePair<String, Buffer>> files;
    
    public MessageOptions(@Nonnull final MessageOptions options) {
        content = options.content;
        embed = options.embed;
        files = options.files;
    }
    
    public MessageOptions(@Nonnull final Message message) {
        content = message.content();
        final List<Embed> embeds = message.embeds();
        if (!embeds.isEmpty()) {
            embed = embeds.get(0);
        }
    }
    
    @CheckReturnValue
    @Nonnull
    public MessageOptions addFile(@Nonnull final File file) {
        return addFile(file.getName(), file);
    }
    
    @CheckReturnValue
    @Nonnull
    @SuppressWarnings("WeakerAccess")
    public MessageOptions addFile(@Nonnull final String name, @Nonnull final File file) {
        if(!file.exists()) {
            throw new IllegalArgumentException("file doesn't exist!");
        }
        if(!file.canRead()) {
            throw new IllegalArgumentException("file cannot be read!");
        }
        try {
            return addFile(name, Files.readAllBytes(file.toPath()));
        } catch(final IOException exc) {
            throw new IllegalArgumentException("cannot read data from file!", exc);
        }
    }
    
    @CheckReturnValue
    @Nonnull
    public MessageOptions addFile(@Nonnull final String name, @Nonnull final InputStream stream) {
        try {
            final ByteArrayOutputStream out = new ByteArrayOutputStream(Math.max(32, stream.available()));
            final byte[] buf = new byte[Math.min(4096, stream.available() > 0 ? stream.available() : 4096)];
            long total = 0;
            while(true) {
                final int r = stream.read(buf);
                if(r == -1) {
                    break;
                }
                out.write(buf, 0, r);
                total += r;
            }
            return addFile(name, out.toByteArray());
        } catch(final IOException exc) {
            throw new IllegalArgumentException("cannot read data from inputstream!", exc);
        }
    }
    
    @CheckReturnValue
    @Nonnull
    @SuppressWarnings("WeakerAccess")
    public MessageOptions addFile(@Nonnull final String name, @Nonnull final byte[] data) {
        if(files == null) {
            files = new ArrayList<>(10);
        }
        if(files.size() == 10) {
            throw new UnsupportedOperationException("maximum limit of 10 attachments!");
        }
        files.add(new ImmutablePair<>(name, Buffer.buffer(data)));
        return this;
    }
    
    @CheckReturnValue
    public boolean hasFiles() {
        return files != null; // because checking via getter creates a new list each time.
    }
    
    @CheckReturnValue
    @Nonnull
    public List<ImmutablePair<String, Buffer>> files() {
        return hasFiles() ? ImmutableList.copyOf(files) : ImmutableList.of();
    }
    
    @CheckReturnValue
    @Nonnull
    public Message buildMessage() {
        final MessageImpl impl = new MessageImpl();
        impl.content(content);
        if (embed != null) {
            impl.embeds(Collections.singletonList(embed));
        } else {
            impl.embeds(Collections.emptyList());
        }
        return impl;
    }
}
