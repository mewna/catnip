package com.mewna.catnip.entity;

import java.util.List;

/**
 * @author natanbc
 * @since 9/12/18
 */
public interface GroupDMChannel extends DMChannel {
    List<User> recipients();
    String icon();
    String ownerId();
    String applicationId();
    
    @Override
    default boolean isUserDM() {
        return false;
    }
    
    @Override
    default boolean isGroupDM() {
        return true;
    }
}
