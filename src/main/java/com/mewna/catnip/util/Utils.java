package com.mewna.catnip.util;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public final class Utils {
    public static final long DISCORD_EPOCH = 1420070400000L;
    
    private Utils() {}
    
    public static OffsetDateTime creationTimeOf(final long id) {
        final long discordTimestamp = id >> 22;
        final Instant instant = Instant.ofEpochMilli(discordTimestamp + DISCORD_EPOCH);
        return instant.atOffset(ZoneOffset.UTC);
    }
}
