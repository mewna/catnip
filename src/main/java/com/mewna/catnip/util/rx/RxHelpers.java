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

package com.mewna.catnip.util.rx;

import com.mewna.catnip.Catnip;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.javatuples.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

/**
 * A collection of utility methods to help make using Rx not quite so potato
 * when trying to convert between various things.
 *
 * @author amy
 * @since 5/18/19.
 */
@SuppressWarnings("unused")
public final class RxHelpers {
    public static final ExecutorService FORK_JOIN_POOL = ForkJoinPool.commonPool();
    public static final Scheduler FORK_JOIN_SCHEDULER = Schedulers.from(FORK_JOIN_POOL);
    
    private RxHelpers() {
    }
    
    public static <T> Observable<T> futureToObservable(final CompletableFuture<T> future) {
        return Observable.create(subscriber ->
                future.whenComplete((result, error) -> {
                    if(error != null) {
                        subscriber.onError(error);
                    } else {
                        subscriber.onNext(result);
                        subscriber.onComplete();
                    }
                }));
    }
    
    public static Completable completedCompletable(@Nonnull final Catnip catnip) {
        return Completable.fromFuture(CompletableFuture.completedFuture(null))
                .subscribeOn(catnip.rxScheduler())
                .observeOn(catnip.rxScheduler());
    }
    
    public static <T> Maybe<T> nullableToMaybe(@Nullable final T nullable) {
        if(nullable == null) {
            return Maybe.empty();
        } else {
            return Maybe.just(nullable);
        }
    }
    
    // Maybe helpers //
    
    public static <A, B> Single<Pair<A, B>> resolveMany(@Nonnull final Maybe<A> a,
                                                        @Nonnull final Maybe<B> b) {
        return a.flatMap(aa ->
                b.flatMap(bb ->
                        Maybe.just(Pair.with(
                                aa,
                                bb
                        )))).toSingle();
    }
    
    public static <A, B, C> Single<Triplet<A, B, C>> resolveMany(@Nonnull final Maybe<A> a,
                                                                @Nonnull final Maybe<B> b,
                                                                @Nonnull final Maybe<C> c) {
        return a.flatMap(aa ->
                b.flatMap(bb ->
                        c.flatMap(cc ->
                                Maybe.just(Triplet.with(
                                        aa,
                                        bb,
                                        cc
                                ))))).toSingle();
    }
    
    public static <A, B, C, D> Single<Quartet<A, B, C, D>> resolveMany(@Nonnull final Maybe<A> a,
                                                                       @Nonnull final Maybe<B> b,
                                                                       @Nonnull final Maybe<C> c,
                                                                       @Nonnull final Maybe<D> d) {
        return a.flatMap(aa ->
                b.flatMap(bb ->
                        c.flatMap(cc ->
                                d.flatMap(dd ->
                                        Maybe.just(Quartet.with(
                                                aa,
                                                bb,
                                                cc,
                                                dd)
                                        ))))).toSingle();
    }
    
    public static <A, B, C, D, E> Single<Quintet<A, B, C, D, E>> resolveMany(@Nonnull final Maybe<A> a,
                                                                             @Nonnull final Maybe<B> b,
                                                                             @Nonnull final Maybe<C> c,
                                                                             @Nonnull final Maybe<D> d,
                                                                             @Nonnull final Maybe<E> e) {
        return a.flatMap(aa ->
                b.flatMap(bb ->
                        c.flatMap(cc ->
                                d.flatMap(dd ->
                                        e.flatMap(ee ->
                                                Maybe.just(Quintet.with(
                                                        aa,
                                                        bb,
                                                        cc,
                                                        dd,
                                                        ee
                                                ))))))).toSingle();
    }
    
    public static <A, B, C, D, E, F> Single<Sextet<A, B, C, D, E, F>> resolveMany(@Nonnull final Maybe<A> a,
                                                                                  @Nonnull final Maybe<B> b,
                                                                                  @Nonnull final Maybe<C> c,
                                                                                  @Nonnull final Maybe<D> d,
                                                                                  @Nonnull final Maybe<E> e,
                                                                                  @Nonnull final Maybe<F> f) {
        return a.flatMap(aa ->
                b.flatMap(bb ->
                        c.flatMap(cc ->
                                d.flatMap(dd ->
                                        e.flatMap(ee ->
                                                f.flatMap(ff ->
                                                        Maybe.just(Sextet.with(
                                                                aa,
                                                                bb,
                                                                cc,
                                                                dd,
                                                                ee,
                                                                ff
                                                        )))))))).toSingle();
    }
    
    public static <A, B, C, D, E, F, G> Single<Septet<A, B, C, D, E, F, G>> resolveMany(@Nonnull final Maybe<A> a,
                                                                                        @Nonnull final Maybe<B> b,
                                                                                        @Nonnull final Maybe<C> c,
                                                                                        @Nonnull final Maybe<D> d,
                                                                                        @Nonnull final Maybe<E> e,
                                                                                        @Nonnull final Maybe<F> f,
                                                                                        @Nonnull final Maybe<G> g) {
        return a.flatMap(aa ->
                b.flatMap(bb ->
                        c.flatMap(cc ->
                                d.flatMap(dd ->
                                        e.flatMap(ee ->
                                                f.flatMap(ff ->
                                                        g.flatMap(gg ->
                                                                Maybe.just(Septet.with(
                                                                        aa,
                                                                        bb,
                                                                        cc,
                                                                        dd,
                                                                        ee,
                                                                        ff,
                                                                        gg
                                                                ))))))))).toSingle();
    }
    
    public static <A, B, C, D, E, F, G, H> Single<Octet<A, B, C, D, E, F, G, H>> resolveMany(@Nonnull final Maybe<A> a,
                                                                                             @Nonnull final Maybe<B> b,
                                                                                             @Nonnull final Maybe<C> c,
                                                                                             @Nonnull final Maybe<D> d,
                                                                                             @Nonnull final Maybe<E> e,
                                                                                             @Nonnull final Maybe<F> f,
                                                                                             @Nonnull final Maybe<G> g,
                                                                                             @Nonnull final Maybe<H> h) {
        return a.flatMap(aa ->
                b.flatMap(bb ->
                        c.flatMap(cc ->
                                d.flatMap(dd ->
                                        e.flatMap(ee ->
                                                f.flatMap(ff ->
                                                        g.flatMap(gg ->
                                                                h.flatMap(hh ->
                                                                        Maybe.just(Octet.with(
                                                                                aa,
                                                                                bb,
                                                                                cc,
                                                                                dd,
                                                                                ee,
                                                                                ff,
                                                                                gg,
                                                                                hh
                                                                        )))))))))).toSingle();
    }
    
    public static <A, B, C, D, E, F, G, H, I> Single<Ennead<A, B, C, D, E, F, G, H, I>> resolveMany(@Nonnull final Maybe<A> a,
                                                                                                    @Nonnull final Maybe<B> b,
                                                                                                    @Nonnull final Maybe<C> c,
                                                                                                    @Nonnull final Maybe<D> d,
                                                                                                    @Nonnull final Maybe<E> e,
                                                                                                    @Nonnull final Maybe<F> f,
                                                                                                    @Nonnull final Maybe<G> g,
                                                                                                    @Nonnull final Maybe<H> h,
                                                                                                    @Nonnull final Maybe<I> i) {
        return a.flatMap(aa ->
                b.flatMap(bb ->
                        c.flatMap(cc ->
                                d.flatMap(dd ->
                                        e.flatMap(ee ->
                                                f.flatMap(ff ->
                                                        g.flatMap(gg ->
                                                                h.flatMap(hh ->
                                                                        i.flatMap(ii ->
                                                                                Maybe.just(Ennead.with(
                                                                                        aa,
                                                                                        bb,
                                                                                        cc,
                                                                                        dd,
                                                                                        ee,
                                                                                        ff,
                                                                                        gg,
                                                                                        hh,
                                                                                        ii
                                                                                ))))))))))).toSingle();
    }
    
    public static <A, B, C, D, E, F, G, H, I, J> Single<Decade<A, B, C, D, E, F, G, H, I, J>> resolveMany(@Nonnull final Maybe<A> a,
                                                                                                          @Nonnull final Maybe<B> b,
                                                                                                          @Nonnull final Maybe<C> c,
                                                                                                          @Nonnull final Maybe<D> d,
                                                                                                          @Nonnull final Maybe<E> e,
                                                                                                          @Nonnull final Maybe<F> f,
                                                                                                          @Nonnull final Maybe<G> g,
                                                                                                          @Nonnull final Maybe<H> h,
                                                                                                          @Nonnull final Maybe<I> i,
                                                                                                          @Nonnull final Maybe<J> j) {
        return a.flatMap(aa ->
                b.flatMap(bb ->
                        c.flatMap(cc ->
                                d.flatMap(dd ->
                                        e.flatMap(ee ->
                                                f.flatMap(ff ->
                                                        g.flatMap(gg ->
                                                                h.flatMap(hh ->
                                                                        i.flatMap(ii ->
                                                                                j.flatMap(jj ->
                                                                                        Maybe.just(Decade.with(
                                                                                                aa,
                                                                                                bb,
                                                                                                cc,
                                                                                                dd,
                                                                                                ee,
                                                                                                ff,
                                                                                                gg,
                                                                                                hh,
                                                                                                ii,
                                                                                                jj
                                                                                        )))))))))))).toSingle();
    }
    
    // Single helpers //
    
    public static <A, B> Single<Pair<A, B>> resolveMany(@Nonnull final Single<A> a,
                                                        @Nonnull final Single<B> b) {
        return a.flatMap(aa ->
                b.flatMap(bb ->
                        Single.just(Pair.with(
                                aa,
                                bb
                        ))));
    }
    
    public static <A, B, C> Single<Triplet<A, B, C>> resolveMany(@Nonnull final Single<A> a,
                                                                @Nonnull final Single<B> b,
                                                                @Nonnull final Single<C> c) {
        return a.flatMap(aa ->
                b.flatMap(bb ->
                        c.flatMap(cc ->
                                Single.just(Triplet.with(
                                        aa,
                                        bb,
                                        cc
                                )))));
    }
    
    public static <A, B, C, D> Single<Quartet<A, B, C, D>> resolveMany(@Nonnull final Single<A> a,
                                                                       @Nonnull final Single<B> b,
                                                                       @Nonnull final Single<C> c,
                                                                       @Nonnull final Single<D> d) {
        return a.flatMap(aa ->
                b.flatMap(bb ->
                        c.flatMap(cc ->
                                d.flatMap(dd ->
                                        Single.just(Quartet.with(
                                                aa,
                                                bb,
                                                cc,
                                                dd)
                                        )))));
    }
    
    public static <A, B, C, D, E> Single<Quintet<A, B, C, D, E>> resolveMany(@Nonnull final Single<A> a,
                                                                             @Nonnull final Single<B> b,
                                                                             @Nonnull final Single<C> c,
                                                                             @Nonnull final Single<D> d,
                                                                             @Nonnull final Single<E> e) {
        return a.flatMap(aa ->
                b.flatMap(bb ->
                        c.flatMap(cc ->
                                d.flatMap(dd ->
                                        e.flatMap(ee ->
                                                Single.just(Quintet.with(
                                                        aa,
                                                        bb,
                                                        cc,
                                                        dd,
                                                        ee
                                                )))))));
    }
    
    public static <A, B, C, D, E, F> Single<Sextet<A, B, C, D, E, F>> resolveMany(@Nonnull final Single<A> a,
                                                                                  @Nonnull final Single<B> b,
                                                                                  @Nonnull final Single<C> c,
                                                                                  @Nonnull final Single<D> d,
                                                                                  @Nonnull final Single<E> e,
                                                                                  @Nonnull final Single<F> f) {
        return a.flatMap(aa ->
                b.flatMap(bb ->
                        c.flatMap(cc ->
                                d.flatMap(dd ->
                                        e.flatMap(ee ->
                                                f.flatMap(ff ->
                                                        Single.just(Sextet.with(
                                                                aa,
                                                                bb,
                                                                cc,
                                                                dd,
                                                                ee,
                                                                ff
                                                        ))))))));
    }
    
    public static <A, B, C, D, E, F, G> Single<Septet<A, B, C, D, E, F, G>> resolveMany(@Nonnull final Single<A> a,
                                                                                        @Nonnull final Single<B> b,
                                                                                        @Nonnull final Single<C> c,
                                                                                        @Nonnull final Single<D> d,
                                                                                        @Nonnull final Single<E> e,
                                                                                        @Nonnull final Single<F> f,
                                                                                        @Nonnull final Single<G> g) {
        return a.flatMap(aa ->
                b.flatMap(bb ->
                        c.flatMap(cc ->
                                d.flatMap(dd ->
                                        e.flatMap(ee ->
                                                f.flatMap(ff ->
                                                        g.flatMap(gg ->
                                                                Single.just(Septet.with(
                                                                        aa,
                                                                        bb,
                                                                        cc,
                                                                        dd,
                                                                        ee,
                                                                        ff,
                                                                        gg
                                                                )))))))));
    }
    
    public static <A, B, C, D, E, F, G, H> Single<Octet<A, B, C, D, E, F, G, H>> resolveMany(@Nonnull final Single<A> a,
                                                                                             @Nonnull final Single<B> b,
                                                                                             @Nonnull final Single<C> c,
                                                                                             @Nonnull final Single<D> d,
                                                                                             @Nonnull final Single<E> e,
                                                                                             @Nonnull final Single<F> f,
                                                                                             @Nonnull final Single<G> g,
                                                                                             @Nonnull final Single<H> h) {
        return a.flatMap(aa ->
                b.flatMap(bb ->
                        c.flatMap(cc ->
                                d.flatMap(dd ->
                                        e.flatMap(ee ->
                                                f.flatMap(ff ->
                                                        g.flatMap(gg ->
                                                                h.flatMap(hh ->
                                                                        Single.just(Octet.with(
                                                                                aa,
                                                                                bb,
                                                                                cc,
                                                                                dd,
                                                                                ee,
                                                                                ff,
                                                                                gg,
                                                                                hh
                                                                        ))))))))));
    }
    
    public static <A, B, C, D, E, F, G, H, I> Single<Ennead<A, B, C, D, E, F, G, H, I>> resolveMany(@Nonnull final Single<A> a,
                                                                                                    @Nonnull final Single<B> b,
                                                                                                    @Nonnull final Single<C> c,
                                                                                                    @Nonnull final Single<D> d,
                                                                                                    @Nonnull final Single<E> e,
                                                                                                    @Nonnull final Single<F> f,
                                                                                                    @Nonnull final Single<G> g,
                                                                                                    @Nonnull final Single<H> h,
                                                                                                    @Nonnull final Single<I> i) {
        return a.flatMap(aa ->
                b.flatMap(bb ->
                        c.flatMap(cc ->
                                d.flatMap(dd ->
                                        e.flatMap(ee ->
                                                f.flatMap(ff ->
                                                        g.flatMap(gg ->
                                                                h.flatMap(hh ->
                                                                        i.flatMap(ii ->
                                                                                Single.just(Ennead.with(
                                                                                        aa,
                                                                                        bb,
                                                                                        cc,
                                                                                        dd,
                                                                                        ee,
                                                                                        ff,
                                                                                        gg,
                                                                                        hh,
                                                                                        ii
                                                                                )))))))))));
    }
    
    public static <A, B, C, D, E, F, G, H, I, J> Single<Decade<A, B, C, D, E, F, G, H, I, J>> resolveMany(@Nonnull final Single<A> a,
                                                                                                          @Nonnull final Single<B> b,
                                                                                                          @Nonnull final Single<C> c,
                                                                                                          @Nonnull final Single<D> d,
                                                                                                          @Nonnull final Single<E> e,
                                                                                                          @Nonnull final Single<F> f,
                                                                                                          @Nonnull final Single<G> g,
                                                                                                          @Nonnull final Single<H> h,
                                                                                                          @Nonnull final Single<I> i,
                                                                                                          @Nonnull final Single<J> j) {
        return a.flatMap(aa ->
                b.flatMap(bb ->
                        c.flatMap(cc ->
                                d.flatMap(dd ->
                                        e.flatMap(ee ->
                                                f.flatMap(ff ->
                                                        g.flatMap(gg ->
                                                                h.flatMap(hh ->
                                                                        i.flatMap(ii ->
                                                                                j.flatMap(jj ->
                                                                                        Single.just(Decade.with(
                                                                                                aa,
                                                                                                bb,
                                                                                                cc,
                                                                                                dd,
                                                                                                ee,
                                                                                                ff,
                                                                                                gg,
                                                                                                hh,
                                                                                                ii,
                                                                                                jj
                                                                                        ))))))))))));
    }
}
