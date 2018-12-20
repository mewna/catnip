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

package com.mewna.catnip.cache.view;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;

/**
 * A {@link CacheView CacheView} that's equivalent to a collection of provided ones.
 * Any update to any of the provided views will update this view.
 *
 * @param <T> Type of the entity held by this cache.
 *
 * @author natanbc
 * @since 12/15/18
 */
public class CompositeCacheView<T> implements CacheView<T> {
    protected final Collection<? extends CacheView<T>> sources;
    
    public CompositeCacheView(@Nonnull final Collection<? extends CacheView<T>> sources) {
        this.sources = sources;
    }
    
    @Override
    public void forEach(final Consumer<? super T> action) {
        for(final CacheView<T> c : sources) {
            c.forEach(action);
        }
    }
    
    @Override
    public long size() {
        long s = 0;
        for(final CacheView<T> c : sources) {
            s += c.size();
        }
        return s;
    }
    
    @Override
    public boolean isEmpty() {
        for(final CacheView<T> c : sources) {
            if(!c.isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public T getById(final long id) {
        for(final CacheView<T> c : sources) {
            final T element = c.getById(id);
            if(element != null) {
                return element;
            }
        }
        return null;
    }
    
    @Override
    public T findAny(@Nonnull final Predicate<? super T> filter) {
        for(final CacheView<T> c : sources) {
            final T element = c.findAny(filter);
            if(element != null) {
                return element;
            }
        }
        return null;
    }
    
    @Nonnull
    @Override
    public Collection<T> find(@Nonnull final Predicate<? super T> filter) {
        final Collection<T> collection = new ArrayList<>();
        for(final CacheView<T> c : sources) {
            c.find(filter, () -> collection);
        }
        return collection;
    }
    
    @Nonnull
    @Override
    public <C extends Collection<T>> C find(@Nonnull final Predicate<? super T> filter, @Nonnull final Supplier<C> supplier) {
        final C collection = Objects.requireNonNull(supplier.get(), "Provided collection may not be null");
        for(final CacheView<T> c : sources) {
            c.find(filter, () -> collection);
        }
        return collection;
    }
    
    @Override
    public <A, R> R collect(final Collector<? super T, A, R> collector) {
        final A a = collector.supplier().get();
        final BiConsumer<A, ? super T> accumulator = collector.accumulator();
        forEach(element -> accumulator.accept(a, element));
        return collector.finisher().apply(a);
    }
    
    @Override
    public <R> R collect(final Supplier<R> supplier, final BiConsumer<R, ? super T> accumulator, final BiConsumer<R, R> combiner) {
        final R result = supplier.get();
        forEach(element -> accumulator.accept(result, element));
        return result;
    }
    
    @Override
    public <U> U reduce(final U identity, final BiFunction<U, ? super T, U> accumulator, final BinaryOperator<U> combiner) {
        U result = identity;
        for(final CacheView<T> view : sources) {
            result = view.reduce(result, accumulator, combiner);
        }
        return result;
    }
    
    @Override
    public Optional<T> reduce(final BinaryOperator<T> accumulator) {
        boolean foundAny = false;
        T result = null;
        for(final CacheView<T> view : sources) {
            if(!foundAny) {
                result = view.reduce(accumulator).orElse(null);
                foundAny = true;
            } else {
                result = view.reduce(result, accumulator);
            }
        }
        //this could be Optional.ofNullable, but this method will ensure equal
        //behaviour to the equivalent method in DefaultCacheView, which would also
        //throw if the accumulator resulted in a null value
        //noinspection ConstantConditions
        return foundAny ? Optional.of(result) : Optional.empty();
    }
    
    @Override
    public T reduce(final T identity, final BinaryOperator<T> accumulator) {
        T result = identity;
        for(final CacheView<T> view : sources) {
            result = view.reduce(result, accumulator);
        }
        return result;
    }
    
    @Override
    public boolean anyMatch(final Predicate<? super T> predicate) {
        for(final CacheView<T> view : sources) {
            if(view.anyMatch(predicate)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean allMatch(final Predicate<? super T> predicate) {
        for(final CacheView<T> view : sources) {
            if(!view.allMatch(predicate)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public Optional<T> min(final Comparator<? super T> comparator) {
        boolean foundAny = false;
        T min = null;
        for(final CacheView<T> view : sources) {
            final T viewMin = view.min(comparator).orElse(null);
            if(!foundAny) {
                min = viewMin;
                foundAny = true;
            } else {
                if(comparator.compare(min, viewMin) > 0) {
                    min = viewMin;
                }
            }
        }
        //this could be Optional.ofNullable, but this method will ensure equal
        //behaviour to the equivalent method in DefaultCacheView, which would also
        //throw if the accumulator resulted in a null value
        //noinspection ConstantConditions
        return foundAny ? Optional.of(min) : Optional.empty();
    }
    
    @Override
    public Optional<T> max(final Comparator<? super T> comparator) {
        boolean foundAny = false;
        T max = null;
        for(final CacheView<T> view : sources) {
            final T viewMax = view.max(comparator).orElse(null);
            if(!foundAny) {
                max = viewMax;
                foundAny = true;
            } else {
                if(comparator.compare(max, viewMax) < 0) {
                    max = viewMax;
                }
            }
        }
        //this could be Optional.ofNullable, but this method will ensure equal
        //behaviour to the equivalent method in DefaultCacheView, which would also
        //throw if the accumulator resulted in a null value
        //noinspection ConstantConditions
        return foundAny ? Optional.of(max) : Optional.empty();
    }
    
    @Override
    public boolean noneMatch(final Predicate<? super T> predicate) {
        return !anyMatch(predicate);
    }
    
    @Nonnull
    @Override
    public Set<Long> keys() {
        return Collections.unmodifiableSet(new AbstractSet<Long>() {
            @Nonnull
            @Override
            public Iterator<Long> iterator() {
                return CompositeCacheView.this.iterator(c -> c.keys().iterator());
            }
            
            @Override
            public int size() {
                return (int) CompositeCacheView.this.size();
            }
        });
    }
    
    @Nonnull
    @Override
    public Collection<T> values() {
        return Collections.unmodifiableCollection(new AbstractCollection<T>() {
            @Nonnull
            @Override
            public Iterator<T> iterator() {
                return CompositeCacheView.this.iterator();
            }
            
            @Override
            public int size() {
                return (int) CompositeCacheView.this.size();
            }
        });
    }
    
    @Nonnull
    @Override
    public Collection<T> snapshot() {
        return snapshot(() -> new ArrayList<>((int) size()));
    }
    
    @Nonnull
    @Override
    public <C extends Collection<T>> C snapshot(@Nonnull final Supplier<C> supplier) {
        final C collection = Objects.requireNonNull(supplier.get(), "Provided collection may not be null");
        for(final CacheView<T> c : sources) {
            c.snapshot(() -> collection);
        }
        return collection;
    }
    
    @Nonnull
    @Override
    public Iterator<T> iterator() {
        return iterator(CacheView::iterator);
    }
    
    private <U> Iterator<U> iterator(@Nonnull final Function<CacheView<T>, Iterator<U>> mapper) {
        final Queue<Iterator<U>> queue = new LinkedList<>();
        for(final CacheView<T> c : sources) {
            queue.add(mapper.apply(c));
        }
        return new JoiningIterator<>(queue);
    }
    
    private static final class JoiningIterator<T> implements Iterator<T> {
        private final Queue<Iterator<T>> queue;
        private Iterator<T> current;
        
        private JoiningIterator(final Queue<Iterator<T>> queue) {
            this.queue = queue;
        }
        
        @Override
        public boolean hasNext() {
            while(current == null || !current.hasNext()) {
                current = queue.poll();
                if(current == null) {
                    return false;
                }
            }
            return true;
        }
        
        @Override
        public T next() {
            return current.next();
        }
        
        @Override
        public void remove() {
            current.remove();
        }
    }
}
