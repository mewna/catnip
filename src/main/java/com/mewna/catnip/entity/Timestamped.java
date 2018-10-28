package com.mewna.catnip.entity;

import javax.annotation.Nullable;
import java.time.OffsetDateTime;

/**
 * @author amy
 * @since 10/28/18.
 */
public interface Timestamped {
    default OffsetDateTime parseTimestamp(@Nullable final CharSequence raw) {
        return raw == null ? null : OffsetDateTime.parse(raw);
    }
}
