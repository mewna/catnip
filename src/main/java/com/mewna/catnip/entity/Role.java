package com.mewna.catnip.entity;

import com.mewna.catnip.entity.impl.Permission;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * @author amy
 * @since 9/4/18.
 */
@SuppressWarnings("unused")
public interface Role {
    @Nonnull
    String id();
    
    @Nonnull
    String name();
    
    int color();
    
    boolean hoist();
    
    @Nonnull
    Set<Permission> permissions();
    
    boolean managed();
    
    boolean mentionable();
}
