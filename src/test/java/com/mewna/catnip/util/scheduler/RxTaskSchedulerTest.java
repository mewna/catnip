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

import com.mewna.catnip.Catnip;
import com.mewna.catnip.util.rx.RxHelpers;
import io.reactivex.rxjava3.core.Scheduler;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * @author amy
 * @since 7/29/19.
 */
class RxTaskSchedulerTest {
    private final Scheduler scheduler = RxHelpers.FORK_JOIN_SCHEDULER;
    
    private Catnip mockNip() {
        return Mockito.mock(Catnip.class);
    }
    
    @Test
    void setTimer() throws InterruptedException {
        final var mock = mockNip();
        when(mock.rxScheduler()).thenReturn(scheduler);
        final TaskScheduler tasker = new RxTaskScheduler();
        tasker.catnip(mock);
        final int[] test = {0};
        tasker.setTimer(50L, __ -> test[0] += 1);
        Thread.sleep(100L);
        assertEquals(1, test[0]);
        
        final long taskId = tasker.setTimer(100L, __ -> test[0] += 1);
        Thread.sleep(50L);
        tasker.cancel(taskId);
        assertEquals(1, test[0]);
    }
    
    @Test
    void setInterval() throws InterruptedException {
        final var mock = mockNip();
        when(mock.rxScheduler()).thenReturn(scheduler);
        final TaskScheduler tasker = new RxTaskScheduler();
        tasker.catnip(mock);
        final int[] test = {0};
        final long taskId = tasker.setInterval(100L, __ -> test[0] += 1);
        Thread.sleep(350L);
        tasker.cancel(taskId);
        assertEquals(3, test[0]);
    }
    
    @Test
    void cancel() throws InterruptedException {
        final var mock = mockNip();
        when(mock.rxScheduler()).thenReturn(scheduler);
        final TaskScheduler tasker = new RxTaskScheduler();
        tasker.catnip(mock);
        final int[] test = {0};
        final var task1 = tasker.setInterval(100L, __ -> test[0] += 1);
        final var task2 = tasker.setInterval(110L, __ -> test[0] += 2);
        Thread.sleep(250L);
        tasker.cancel(task2);
        assertEquals(6, test[0]);
        Thread.sleep(130L);
        tasker.cancel(task1);
        assertEquals(7, test[0]);
    }
    
    @Test
    void taskIdsIncrement() throws InterruptedException {
        final var mock = mockNip();
        when(mock.rxScheduler()).thenReturn(scheduler);
        final TaskScheduler tasker = new RxTaskScheduler();
        tasker.catnip(mock);
        final var id = tasker.setTimer(50L, __ -> {});
        assertEquals(0, id);
        Thread.sleep(100L);
        
        final long id2 = tasker.setTimer(100L, __ -> {});
        assertEquals(1, id2);
        Thread.sleep(50L);
    }
}