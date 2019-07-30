package com.mewna.catnip.shard.event;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.shard.DiscordEvent;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class DefaultDispatchManagerTest {
    private DispatchManager dispatchManager() {
        final var mock = Mockito.mock(Catnip.class);
        when(mock.dispatchManager()).thenReturn(new DefaultDispatchManager());
        return mock.dispatchManager();
    }
    
    @Test
    void simpleRun() {
        final var dispatchManager = dispatchManager();
        //TODO TEST
    }
}