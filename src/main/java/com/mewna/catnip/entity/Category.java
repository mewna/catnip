package com.mewna.catnip.entity;

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
