package com.mewna.catnip.entity;

import com.mewna.catnip.util.Utils;

import java.time.OffsetDateTime;

public interface Snowflake extends Entity {
    String id();
    
    default long idAsLong() {
        return Long.parseUnsignedLong(id());
    }
    
    default OffsetDateTime creationTime() {
        return Utils.creationTimeOf(idAsLong());
    }
}
