package com.mewna.catnip.entity.misc;

import com.mewna.catnip.entity.Entity;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Fired when a shard resumes successfully.
 *
 * @author amy
 * @since 11/10/18.
 */
public interface Resumed extends Entity {
    /**
     * @return Debugging trace.
     */
    @Nonnull
    List<String> trace();
}
