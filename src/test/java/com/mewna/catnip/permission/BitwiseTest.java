package com.mewna.catnip.permission;

import com.mewna.catnip.entity.Permission;

import static com.mewna.catnip.entity.Permission.*;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("WeakerAccess")
public class BitwiseTest {
    @Test
    public void testAll() {
        final Permission[] perms = values();
        final int expected = 2146958847;
        int total = 0;

        for (Permission p : perms) {
            total |= p.getValue();
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

        final List<Permission> result = toList(toTest);

        Collections.sort(expected);
        Collections.sort(result);

        assertArrayEquals(expected.toArray(), result.toArray());
    }
}
