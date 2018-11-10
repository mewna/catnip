package com.mewna.catnip.entity.misc;

import com.mewna.catnip.entity.Entity;
import com.mewna.catnip.entity.user.User;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author amy
 * @since 10/4/18.
 */
public interface Ready extends Entity {
    @Nonnegative
    int version();
    
    @Nonnull
    User user();
    
    @Nonnull
    List<String> trace();
}
