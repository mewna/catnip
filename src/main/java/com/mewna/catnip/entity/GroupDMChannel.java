package com.mewna.catnip.entity;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author natanbc
 * @since 9/12/18
 */
public interface GroupDMChannel extends DMChannel {
    @Nonnull
    @CheckReturnValue
    List<User> recipients();
    
    @CheckReturnValue
    String icon();
    
    @Nonnull
    @CheckReturnValue
    String ownerId();
    
    @CheckReturnValue
    String applicationId();
    
    @Override
    @CheckReturnValue
    default boolean isUserDM() {
        return false;
    }
    
    @Override
    @CheckReturnValue
    default boolean isGroupDM() {
        return true;
    }
}
