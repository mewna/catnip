package com.mewna.catnip.entity;

import com.mewna.catnip.Catnip;

import javax.annotation.Nonnull;

public interface RequiresCatnip extends Entity {
    void catnip(@Nonnull Catnip catnip);
}
