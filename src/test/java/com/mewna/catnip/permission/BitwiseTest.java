package com.mewna.catnip.permission;

import com.mewna.catnip.entity.Permission;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.mewna.catnip.entity.Permission.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("WeakerAccess")
public class BitwiseTest {
    @Test
    public void testAll() {
        final Permission[] perms = values();
        final int expected = 2146958847;
        int total = 0;
        
        for(final Permission p : perms) {
            total |= p.value();
        }
        
        assertEquals(expected, total);
    }
    
    @Test
    public void testFromLong() {
        final long toTest = 805314622L;
        final List<Permission> expected = Arrays.asList(
                ADMINISTRATOR,
                MANAGE_GUILD,
                MANAGE_ROLES,
                MANAGE_CHANNELS,
                KICK_MEMBERS,
                BAN_MEMBERS,
                MANAGE_WEBHOOKS,
                MANAGE_MESSAGES
        );
        
        final List<Permission> result = new ArrayList<>(toSet(toTest));
        
        Collections.sort(expected);
        Collections.sort(result);
        
        assertArrayEquals(expected.toArray(), result.toArray());
    }
}
