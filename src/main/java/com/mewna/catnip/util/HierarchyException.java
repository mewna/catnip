package com.mewna.catnip.util;

import com.mewna.catnip.entity.partials.Permissable;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class HierarchyException extends RuntimeException {
    
    private final Permissable actor;
    private final Permissable target;
    
    public HierarchyException(final Permissable actor, final Permissable target) {
        super(message(actor, target));
        this.actor = actor;
        this.target = target;
    }
    
    private static String message(final Permissable actor, final Permissable target) {
        return "Could not interact with " + target;
    }
    
}
