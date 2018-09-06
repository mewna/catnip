package com.mewna.catnip.entity;

import com.mewna.catnip.util.Utils;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.time.OffsetDateTime;

public interface Snowflake extends Entity {
    /**
     * The ID of this snowflake.
     *
     * @return String representing the ID.
     */
    @CheckReturnValue
    String id();
    
    /**
     * The ID of this snowflake, as a long.
     *
     * @return Long representing the ID.
     */
    @CheckReturnValue
    default long idAsLong() {
        return Long.parseUnsignedLong(id());
    }
    
    /**
     * The time this snowflake was generated.
     *
     * @return OffsetDateTime representing when this snowflake was generated.
     */
    @Nonnull
    @CheckReturnValue
    default OffsetDateTime creationTime() {
        return Utils.creationTimeOf(idAsLong());
    }
}
