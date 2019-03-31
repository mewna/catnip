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

package com.mewna.catnip.entity.message;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mewna.catnip.entity.RequiresCatnip;
import com.mewna.catnip.entity.Snowflake;
import com.mewna.catnip.util.CatnipEntity;
import org.immutables.value.Value;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * @author amy
 * @since 3/30/19.
 */
@Value.Modifiable
@CatnipEntity
@JsonDeserialize(as = AttachmentImpl.class)
public
interface Attachment extends Snowflake, RequiresCatnip<AttachmentImpl> {
    /**
     * The name of the file represented by this attachment.
     *
     * @return String representing the file name. Never null.
     */
    @Nonnull
    @CheckReturnValue
    String fileName();
    
    /**
     * The size of the file represented by this attachment, in bytes.
     *
     * @return Integer representing the file size. Never negative.
     */
    @Nonnegative
    @CheckReturnValue
    int size();
    
    /**
     * The source URL for the file.
     *
     * @return String representing the source URL. Never null.
     */
    @Nonnull
    @CheckReturnValue
    String url();
    
    /**
     * The proxied URL for the file.
     *
     * @return String representing the proxied URL. Never null.
     */
    @Nonnull
    @CheckReturnValue
    String proxyUrl();
    
    /**
     * The height of this attachment, if it's an image.
     *
     * @return Integer representing the height, or -1 if this attachment is not an image.
     */
    @CheckReturnValue
    int height();
    
    /**
     * The width of this attachment, if it's an image.
     *
     * @return Integer representing the width, or -1 if this attachment is not an image.
     */
    @CheckReturnValue
    int width();
    
    /**
     * Whether this attachment is an image.
     *
     * @return True if this attachment is an image, false otherwise.
     */
    @CheckReturnValue
    default boolean image() {
        return height() > 0 && width() > 0;
    }
}
