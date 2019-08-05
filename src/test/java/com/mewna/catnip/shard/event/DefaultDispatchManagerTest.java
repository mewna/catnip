package com.mewna.catnip.shard.event;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.util.rx.RxHelpers;
import io.reactivex.Scheduler;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class DefaultDispatchManagerTest {
    private final Scheduler scheduler = RxHelpers.FORK_JOIN_SCHEDULER;
    private final Object event = new Object();
    private final long timeout = 500;
    
    private DispatchManager dispatchManager() {
        final var mock = Mockito.mock(Catnip.class);
        when(mock.rxScheduler()).thenReturn(scheduler);
        
        final var dispatchManager = new DefaultDispatchManager();
        dispatchManager.catnip(mock);
        return dispatchManager;
    }
    
    @Test
    void simpleRun() throws InterruptedException {
        final var dispatchManager = dispatchManager();
        final Semaphore semaphore = new Semaphore(1);
        semaphore.acquire();
        
        dispatchManager.createConsumer("simpleRun").handler(o -> semaphore.release());
        dispatchManager.dispatchEvent("simpleRun", event);
        assertTrue(semaphore.tryAcquire(timeout, TimeUnit.MILLISECONDS), "Not dispatched");
    }
    
    @Test
    void testConsistency() throws InterruptedException {
        final var dispatchManager = dispatchManager();
        final var amount = 100;
        final Semaphore semaphore = new Semaphore(amount);
        semaphore.acquire(amount);
        
        dispatchManager.createConsumer("testConsistency").handler(o -> semaphore.release());
        
        for(int i = 0; i < amount; i++) {
            dispatchManager.dispatchEvent("testConsistency", event);
        }
        
        assertTrue(semaphore.tryAcquire(amount, timeout, TimeUnit.MILLISECONDS), "Not all events were dispatched");
    }
    
    @Test
    void testObservable() throws InterruptedException {
        final var dispatchManager = dispatchManager();
        final var amount = 100;
        final AtomicInteger counted = new AtomicInteger();
        final Semaphore semaphore = new Semaphore(amount);
        semaphore.acquire(amount);
        
        final var disposable = dispatchManager.createConsumer("testObservable")
                .asObservable().subscribe(o -> {
                    counted.incrementAndGet();
                    semaphore.release();
                });
        
        for(int i = 0; i < amount; i++) {
            dispatchManager.dispatchEvent("testObservable", event);
        }
    
        assertTrue(semaphore.tryAcquire(amount, timeout, TimeUnit.MILLISECONDS), "Not all events were dispatched");
        disposable.dispose();
    
        for(int i = 0; i < amount; i++) {
            dispatchManager.dispatchEvent("testObservable", event);
        }
        
        assertEquals(amount, counted.get(), "Observable was not disposed");
    }
    
    @Test
    void testFlowable() throws InterruptedException {
        final var dispatchManager = dispatchManager();
        final var amount = 100;
        final AtomicInteger counted = new AtomicInteger();
        final Semaphore semaphore = new Semaphore(amount);
        semaphore.acquire(amount);
        
        final var disposable = dispatchManager.createConsumer("testFlowable")
                .asFlowable().subscribe(o -> {
                    counted.incrementAndGet();
                    semaphore.release();
                });
        
        for(int i = 0; i < amount; i++) {
            dispatchManager.dispatchEvent("testFlowable", event);
        }
    
        assertTrue(semaphore.tryAcquire(amount, timeout, TimeUnit.MILLISECONDS), "Not all events were dispatched");
        disposable.dispose();
    
        for(int i = 0; i < amount; i++) {
            dispatchManager.dispatchEvent("testFlowable", event);
        }
        
        assertEquals(amount, counted.get(), "Flowable was not disposed");
    }
}