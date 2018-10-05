package com.mewna.catnip.entity;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.Entity;

import javax.annotation.Nonnull;

public interface RequiresCatnip extends Entity {
    void catnip(@Nonnull Catnip catnip);
}
