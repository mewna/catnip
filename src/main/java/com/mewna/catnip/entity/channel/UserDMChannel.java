package com.mewna.catnip.entity.channel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mewna.catnip.entity.impl.UserDMChannelImpl;
import com.mewna.catnip.entity.user.User;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;

/**
 * @author natanbc
 * @since 9/12/18
 */
@JsonDeserialize(as = UserDMChannelImpl.class)
public interface UserDMChannel extends DMChannel {
    @Nullable
    @CheckReturnValue
    User recipient();
    
    @Override
    @JsonIgnore
    @CheckReturnValue
    default boolean isUserDM() {
        return true;
    }
    
    @Override
    @JsonIgnore
    @CheckReturnValue
    default boolean isGroupDM() {
        return false;
    }
}
