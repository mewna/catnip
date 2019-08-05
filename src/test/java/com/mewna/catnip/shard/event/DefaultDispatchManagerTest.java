package com.mewna.catnip.shard.event;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.util.rx.RxHelpers;
import io.reactivex.Scheduler;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class DefaultDispatchManagerTest {
    private final Scheduler scheduler = RxHelpers.FORK_JOIN_SCHEDULER;
    private final Object event = new Object();
    
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
        final boolean[] wasExecuted = { false };
    
        dispatchManager.createConsumer("simpleRun").handler(o -> wasExecuted[0] = true);
        dispatchManager.dispatchEvent("simpleRun", event);
        Thread.sleep(100L);
        assertTrue(wasExecuted[0], "Not dispatched");
    }
    
    @Test
    void testConsistency() throws InterruptedException {
        final var dispatchManager = dispatchManager();
        final var amount = 1000;
        final AtomicInteger counted = new AtomicInteger();
    
        dispatchManager.createConsumer("testConsistency").handler(o -> counted.incrementAndGet());
    
        for(int i = 0; i < amount; i++) {
            dispatchManager.dispatchEvent("testConsistency", event);
        }
        
        Thread.sleep(100L);
        assertEquals(amount, counted.get(), "Not all events were dispatched");
    }
    
    @Test
    void testObservable() throws InterruptedException {
        final var dispatchManager = dispatchManager();
        final var amount = 1000;
        final AtomicInteger counted = new AtomicInteger();
    
        final var disposable = dispatchManager.createConsumer("testObservable")
                .asObservable().subscribe(o -> counted.incrementAndGet());
    
        for(int i = 0; i < amount; i++) {
            dispatchManager.dispatchEvent("testObservable", event);
        }
        
        Thread.sleep(100L);
        
        assertEquals(amount, counted.get(), "Not all events were dispatched");
        disposable.dispose();
    
        for(int i = 0; i < amount; i++) {
            dispatchManager.dispatchEvent("testObservable", event);
        }
    
        assertEquals(amount, counted.get(), "Observable was not disposed");
    }
    
    @Test
    void testFlowable() throws InterruptedException {
        final var dispatchManager = dispatchManager();
        final var event = new Object();
        final var amount = 1000;
        final AtomicInteger counted = new AtomicInteger();
        
        final var disposable = dispatchManager.createConsumer("testFlowable")
                .asFlowable().subscribe(o -> counted.incrementAndGet());
        
        for(int i = 0; i < amount; i++) {
            dispatchManager.dispatchEvent("testFlowable", event);
        }
    
        Thread.sleep(100L);
    
        assertEquals(amount, counted.get(), "Not all events were dispatched");
        disposable.dispose();
    
        for(int i = 0; i < amount; i++) {
            dispatchManager.dispatchEvent("testFlowable", event);
        }
        
        assertEquals(amount, counted.get(), "Flowable was not disposed");
    }
    
    
}