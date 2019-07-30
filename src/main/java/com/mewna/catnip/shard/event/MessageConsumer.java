package com.mewna.catnip.shard.event;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;

import java.io.Closeable;
import java.util.function.Consumer;

public interface MessageConsumer<T> extends Closeable {
    String address();
    
    // This method is specific to Catnip#on and can be safely removed if wanted.
    MessageConsumer<T> handler(Consumer<T> handler);
    
    Observable<T> asObservable();
    
    Flowable<T> asFlowable(BackpressureStrategy backpressureStrategy);
    
    default Flowable<T> asFlowable() {
        return asFlowable(BackpressureStrategy.BUFFER);
    }
    
    @Override
    void close();
}
