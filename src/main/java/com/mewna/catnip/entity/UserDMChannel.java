package com.mewna.catnip.entity;

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
