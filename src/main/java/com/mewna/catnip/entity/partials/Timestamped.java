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

package com.mewna.catnip.entity.partials;

import lombok.Getter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;

/**
 * An entity that has a timestamp.
 *
 * @author amy
 * @since 10/28/18.
 */
public interface Timestamped {
    default OffsetDateTime parseTimestamp(@Nullable final CharSequence raw) {
        return raw == null ? null : OffsetDateTime.parse(raw);
    }
    
    default String asDiscordTimestamp(@Nonnull final OffsetDateTime time, @Nonnull final TimestampStyle style) {
        return String.format("<t:%s:%s>", time.toEpochSecond(), style.code);
    }
    
    enum TimestampStyle {
        /**
         * Example: {@code 16:20}
         */
        SHORT("t"),
        /**
         * Example: {@code 16:20:30}
         */
        LONG("T"),
        /**
         * Example: {@code 20/04/2021}
         */
        SHORT_DATE("d"),
        /**
         * Example: {@code 20 April 2021}
         */
        LONG_DATE("D"),
        /**
         * Example: {@code 20 April 2021 16:20}
         */
        SHORT_DATE_TIME("f"),
        /**
         * Example: {@code Tuesday, 20 April 2021 16:20}
         */
        LONG_DATE_TIME("F"),
        /**
         * Example: {@code 2 months ago}
         */
        RELATIVE("R"),
        ;
        
        @Getter
        private final String code;
    
        TimestampStyle(final String code) {
            this.code = code;
        }
    }
}
