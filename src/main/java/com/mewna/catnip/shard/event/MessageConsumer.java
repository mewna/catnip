package com.mewna.catnip.shard.event;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;

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
