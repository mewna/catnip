package com.mewna.catnip.permission;

import com.mewna.catnip.entity.Permission;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BitwiseTest {
	@Test
	public void testAll() {
		Permission[] perms = Permission.values();
		int expected = 2146958847;
		int total = 0;

		for(Permission p : perms) {
			total |= p.getValue();
		}

		assertEquals(expected, total);
	}
}
