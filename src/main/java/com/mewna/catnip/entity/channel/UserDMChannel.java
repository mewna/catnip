package com.mewna.catnip.entity.channel;

import com.mewna.catnip.entity.user.User;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;

/**
 * @author natanbc
 * @since 9/12/18
 */
public interface UserDMChannel extends DMChannel {
    @Nullable
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
