package com.mewna.catnip.entity;

import com.mewna.catnip.Catnip;

/**
 * @author natanbc
 * @since 5/9/18.
 */
public interface Entity {
    /**
     * Returns the catnip instance associated with this entity.
     *
     * @return The catnip instance of this entity.
     */
    Catnip catnip();
}
