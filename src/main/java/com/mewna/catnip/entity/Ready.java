package com.mewna.catnip.entity;

import com.mewna.catnip.entity.impl.RequiresCatnip;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author amy
 * @since 10/4/18.
 */
public interface Ready extends RequiresCatnip {
    @Nonnegative
    int version();
    
    @Nonnull
    User user();
    
    @Nonnull
    List<String> trace();
}
