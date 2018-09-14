package com.mewna.catnip.entity;

/**
 * @author natanbc
 * @since 9/12/18
 */
public interface Category extends Channel {
    @Override
    default boolean isText() {
        return false;
    }
    
    @Override
    default boolean isVoice() {
        return false;
    }
    
    @Override
    default boolean isCategory() {
        return true;
    }
}
