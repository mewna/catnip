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

package com.mewna.catnip.util;

import com.mewna.catnip.Catnip;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.util.concurrent.*;
import java.util.function.Function;

@SuppressWarnings("WeakerAccess")
public class SafeVertxCompletableFuture<T> extends CompletableFuture<T> {
    private final Executor executor;
    private final Catnip catnip;
    private final Context context;
    
    public SafeVertxCompletableFuture(final Catnip catnip, final Context context) {
        this.catnip = catnip;
        this.context = context;
        executor = r -> context.runOnContext(__ -> r.run());
    }
    
    public SafeVertxCompletableFuture(final Catnip catnip) {
        this(catnip, catnip.vertx().getOrCreateContext());
    }
    
    private SafeVertxCompletableFuture(final Catnip catnip, final Context context, final CompletionStage<T> future) {
        this(catnip, context);
        future.whenComplete((res, err) -> {
            if(err != null) {
                completeExceptionally(err);
            } else {
                complete(res);
            }
        });
    }
    
    public SafeVertxCompletableFuture<T> withContext() {
        final Context context = Vertx.currentContext();
        return withContext(context);
    }
    
    public SafeVertxCompletableFuture<T> withContext(final Context context) {
        final SafeVertxCompletableFuture<T> future = new SafeVertxCompletableFuture<>(catnip, context);
        whenComplete((res, err) -> {
            if(err != null) {
                future.completeExceptionally(err);
            } else {
                future.complete(res);
            }
        });
        return future;
    }
    
    public Context context() {
        return context;
    }
    
    @Override
    public Executor defaultExecutor() {
        return executor;
    }
    
    @Override
    public SafeVertxCompletableFuture<T> toCompletableFuture() {
        return this;
    }
    
    @Override
    public <U> CompletableFuture<U> newIncompleteFuture() {
        return new SafeVertxCompletableFuture<>(catnip, context);
    }
    
    @Override
    public CompletionStage<T> minimalCompletionStage() {
        return copy();
    }
    
    @Override
    public CompletableFuture<T> copy() {
        return thenApply(Function.identity());
    }
    
    @Override
    public T get() throws InterruptedException, ExecutionException {
        checkBlock();
        return super.get();
    }
    
    @Override
    public T get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        checkBlock();
        return super.get(timeout, unit);
    }
    
    @Override
    public T join() {
        checkBlock();
        return super.join();
    }
    
    public static <T> SafeVertxCompletableFuture<T> from(final Catnip catnip, final CompletionStage<T> future) {
        return from(catnip, catnip.vertx().getOrCreateContext(), future);
    }
    
    public static <T> SafeVertxCompletableFuture<T> from(final Catnip catnip, final Future<T> future) {
        return from(catnip, catnip.vertx().getOrCreateContext(), future);
    }
    
    public static <T> SafeVertxCompletableFuture<T> from(final Catnip catnip, final Context context, final CompletionStage<T> future) {
        final SafeVertxCompletableFuture<T> res = new SafeVertxCompletableFuture<>(catnip, context);
        future.whenComplete((result, error) -> {
            if(context == Vertx.currentContext()) {
                res.complete(result, error);
            } else {
                res.context.runOnContext(v -> res.complete(result, error));
            }
        });
        return res;
    }
    
    public static <T> SafeVertxCompletableFuture<T> from(final Catnip catnip, final Context context, final Future<T> future) {
        final SafeVertxCompletableFuture<T> res = new SafeVertxCompletableFuture<>(catnip, context);
        future.setHandler(ar -> {
            if(context == Vertx.currentContext()) {
                res.completeFromAsyncResult(ar);
            } else {
                res.context.runOnContext(v -> res.completeFromAsyncResult(ar));
            }
        });
        return res;
    }
    
    public static SafeVertxCompletableFuture<Void> allOf(final Catnip catnip, final CompletableFuture<?>... futures) {
        final CompletableFuture<Void> all = CompletableFuture.allOf(futures);
        return from(catnip, all);
    }
    
    public static SafeVertxCompletableFuture<Void> allOf(final Catnip catnip, final Context context, final CompletableFuture<?>... futures) {
        final CompletableFuture<Void> all = CompletableFuture.allOf(futures);
        return from(catnip, context, all);
    }
    
    public static SafeVertxCompletableFuture<Object> anyOf(final Catnip catnip, final CompletableFuture<?>... futures) {
        final CompletableFuture<Object> all = CompletableFuture.anyOf(futures);
        return from(catnip, all);
    }
    
    public static SafeVertxCompletableFuture<Object> anyOf(final Catnip catnip, final Context context, final CompletableFuture<?>... futures) {
        final CompletableFuture<Object> all = CompletableFuture.anyOf(futures);
        return from(catnip, context, all);
    }
    
    private void complete(final T result, final Throwable error) {
        if(error == null) {
            super.complete(result);
        } else {
            super.completeExceptionally(error);
        }
    }
    
    private void completeFromAsyncResult(final AsyncResult<T> ar) {
        if(ar.succeeded()) {
            super.complete(ar.result());
        } else {
            super.completeExceptionally(ar.cause());
        }
    }
    
    private void checkBlock() {
        if(isDone() || isCompletedExceptionally()) {
            //if we're done/completed we won't block
            return;
        }
        final Context currentContext = Vertx.currentContext();
        if(currentContext != null && Context.isOnEventLoopThread()) {
            if(currentContext.owner() == catnip.vertx()) {
                throw new IllegalStateException("Possible deadlock detected. Avoid blocking event loop threads");
            } else {
                catnip.logAdapter().warn(
                        "Event loop block detected",
                        new Throwable("Blocking method call location"));
            }
        }
    }
}
