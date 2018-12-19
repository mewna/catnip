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
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

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
    public long size() {
        long s = 0;
        for(final CacheView<T> c : sources) {
            s += c.size();
        }
        return s;
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
    public T findAny(@Nonnull final Predicate<T> filter) {
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
    public Collection<T> find(@Nonnull final Predicate<T> filter) {
        final Collection<T> collection = new ArrayList<>();
        for(final CacheView<T> c : sources) {
            c.find(filter, () -> collection);
        }
        return collection;
    }
    
    @Nonnull
    @Override
    public <C extends Collection<T>> C find(@Nonnull final Predicate<T> filter, @Nonnull final Supplier<C> supplier) {
        final C collection = Objects.requireNonNull(supplier.get(), "Provided collection may not be null");
        for(final CacheView<T> c : sources) {
            c.find(filter, () -> collection);
        }
        return collection;
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
