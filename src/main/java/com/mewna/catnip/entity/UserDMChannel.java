package com.mewna.catnip.entity;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * @author natanbc
 * @since 9/12/18
 */
public interface UserDMChannel extends DMChannel {
    @Nonnull
    @CheckReturnValue
    User recipient();
    
    @Override
    @CheckReturnValue
    default boolean isUserDM() {
        return true;
    }
    
    @Override
    @CheckReturnValue
    default boolean isGroupDM() {
        return false;
    }
}
