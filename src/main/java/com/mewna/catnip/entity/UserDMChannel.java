package com.mewna.catnip.entity;

/**
 * @author natanbc
 * @since 9/12/18
 */
public interface UserDMChannel extends DMChannel {
    User recipient();
    
    @Override
    default boolean isUserDM() {
        return true;
    }
    
    @Override
    default boolean isGroupDM() {
        return false;
    }
}
