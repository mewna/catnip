/*
 * Copyright (c) 2019 amy, All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *  3. Neither the name of the copyright holder nor the names of its contributors
 *     may be used to endorse or promote products derived from this software without
 *     specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 *  FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 *  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *  CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.mewna.catnip.rest.ratelimit;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.rest.Routes.Route;
import io.reactivex.rxjava3.core.Completable;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Ratelimiting for the REST API. The REST ratelimiter handles checking and
 * updating ratelimits based on the bucket that the requested route is
 * attempting to execute on.
 */
public interface RateLimiter {
    void catnip(@Nonnull Catnip catnip);
    
    /**
     * Requests execution for a specific route. The route cannot be executed on
     * until the bucket has requests available again, so the ratelimiter must
     * handle this correctly.
     *
     * @param route The route to execute.
     *
     * @return A {@link Completable} that completes when the route can execute
     * again.
     */
    @Nonnull
    @CheckReturnValue
    Completable requestExecution(@Nonnull Route route);
    
    void updateRemaining(@Nonnull Route route, int remaining);
    
    void updateLimit(@Nonnull Route route, int limit);
    
    void updateReset(@Nonnull Route route, long resetTimestamp);
    
    //called after above 3 to signal no further updates will be done
    void updateDone(@Nonnull Route route);
    
    void updateGlobalRateLimit(long resetTimestamp);
}
