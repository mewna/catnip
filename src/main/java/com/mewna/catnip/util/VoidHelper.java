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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * A horrible, ugly, no-good, very-bad hack, but it's necessary. In the
 * pagination classes (used for ex. batch message pagination), we have a
 * situation where we compose observables in a chain, then terminate by
 * returning an empty Observable&lt;Void&gt;. The problem with this is that we
 * can't just shove a {@code null} into it, as that's illegal in Rx2 operators
 * because reasons. Returning a Completable isn't an option either because it's
 * really a {@code Observable<T> | Observable<Void>} return type, for reasons
 * I'd rather not try and work out since I didn't write that code. To get
 * around this issue, we do the horrible, awful hack of instantiating a Void
 * and storing it for use in those cases.
 *
 * @author amy
 * @since 3/8/20.
 */
public final class VoidHelper {
    public static final Void VOID;
    
    static {
        try {
            final Constructor<Void> constructor = Void.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            VOID = constructor.newInstance();
        } catch(final NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    
    private VoidHelper() {
    }
}
