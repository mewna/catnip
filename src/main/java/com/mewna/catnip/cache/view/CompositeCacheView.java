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
    public T getById(final String id) {
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
    public Set<String> keys() {
        return Collections.unmodifiableSet(new AbstractSet<String>() {
            @Nonnull
            @Override
            public Iterator<String> iterator() {
                return CompositeCacheView.this.iterator(c -> c.keys().iterator());
            }
    
            @Override
            public int size() {
                return (int)CompositeCacheView.this.size();
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
                return (int)CompositeCacheView.this.size();
            }
        });
    }
    
    @Nonnull
    @Override
    public Collection<T> snapshot() {
        return snapshot(() -> new ArrayList<>((int)size()));
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
