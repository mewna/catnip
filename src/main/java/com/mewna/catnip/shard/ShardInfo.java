package com.mewna.catnip.shard;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author amy
 * @since 10/17/18.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class ShardInfo {
    private final int id;
    private final int limit;
}
