/*
 * Copyright (c) 2020 amy, All rights reserved.
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

package com.mewna.catnip.util;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

/**
 * @author amy
 * @since 12/11/20.
 */
public final class Validators {
    public static void assertStringLength(@Nullable final String toCheck, @Nonnull final String name,
                                          @Nonnegative final int minLen, @Nonnegative final int maxLen) {
        if(toCheck == null) {
            throw new IllegalArgumentException(String.format("%s: must be present", name));
        }
        if(toCheck.length() < minLen || toCheck.length() > maxLen) {
            throw new IllegalArgumentException(String.format("%s: must be between %s and %s characters (was: %s)", name,
                    minLen, maxLen, toCheck.length()));
        }
    }
    
    public static void assertListSize(@Nullable final List<?> toCheck, @Nonnull final String name,
                                      @Nonnegative final int minLen, @Nonnegative final int maxLen) {
        if(toCheck == null) {
            throw new IllegalArgumentException(String.format("%s: must be present", name));
        }
        if(toCheck.size() < minLen || toCheck.size() > maxLen) {
            throw new IllegalArgumentException(String.format("%s: must be between %s and %s items (was: %s)", name,
                    minLen, maxLen, toCheck.size()));
        }
    }
    
    public static <U> void assertType(@Nullable final U actual, @Nonnull final Class<?>[] expected,
                                      @Nonnull final String name) {
        if(actual == null) {
            throw new IllegalArgumentException(String.format("%s: must be present", name));
        }
        if(Arrays.stream(expected).noneMatch(c -> c.isInstance(actual))) {
            throw new IllegalArgumentException(String.format("%s: must be instance of one of: %s (was: %s)", name,
                    String.join(", ", Arrays.stream(expected).map(Class::getName).toArray(String[]::new)),
                    actual.getClass().getName()));
        }
    }
    
    public static <T> T unreachable() {
        throw new RuntimeException("this should be unreachable");
    }
}
