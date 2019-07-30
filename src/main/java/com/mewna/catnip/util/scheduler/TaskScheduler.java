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

package com.mewna.catnip.util.scheduler;

import io.reactivex.Observable;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * A basic task scheduler to replace vert.x's setTimer() and setPeriodic(). The
 * default implementation is {@link RxTaskScheduler}, which is a basic wrapper
 * over {@link Observable#timer(long, TimeUnit)} and
 * {@link Observable#interval(long, TimeUnit)}.
 *
 * @author amy
 * @since 7/29/19.
 */
// If we overflow longs, we have bigger issues.
public interface TaskScheduler {
    /**
     * Schedules a new task to be run in the future.
     *
     * @param ms   Delay, in milliseconds, before the task is run.
     * @param task The task to be run.
     *
     * @return The id of the task to be run. Used for cancellations.
     */
    @Nonnegative
    long setTimer(@Nonnegative long ms, @Nonnull Consumer<Long> task);
    
    /**
     * Schedules a new task to be run on an interval.
     *
     * @param ms   Interval, in millseconds, between task runs.
     * @param task The task to be run.
     *
     * @return The id of the task to be run. Used for cancellations.
     */
    @Nonnegative
    long setInterval(@Nonnegative long ms, @Nonnull Consumer<Long> task);
    
    /**
     * Cancels the task with the supplied id.
     *
     * @param id The id of the task to cancel.
     *
     * @return {@code true} if the task was successfully cancelled,
     * {@code false} otherwise.
     */
    boolean cancel(@Nonnegative long id);
}
