package com.mewna.catnip.entity.channel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mewna.catnip.entity.impl.GroupDMChannelImpl;
import com.mewna.catnip.entity.user.User;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.List;

/**
 * A DM with a group of users.
 *
 * @author natanbc
 * @since 9/12/18
 */
@SuppressWarnings("unused")
@JsonDeserialize(as = GroupDMChannelImpl.class)
public interface GroupDMChannel extends DMChannel {
    /**
     * @return The list of users in the group DM.
     */
    @Nonnull
    @CheckReturnValue
    List<User> recipients();
    
    /**
     * @return The hash for the group DM's icon.
     */
    @CheckReturnValue
    String icon();
    
    /**
     * @return The ID of the user who owns the group DM.
     */
    @Nonnull
    @CheckReturnValue
    String ownerId();
    
    /**
     * @return The ID of the application that created the group DM.
     */
    @CheckReturnValue
    String applicationId();
    
    @Override
    @JsonIgnore
    @CheckReturnValue
    default boolean isUserDM() {
        return false;
    }
    
    @Override
    @JsonIgnore
    @CheckReturnValue
    default boolean isGroupDM() {
        return true;
    }
}
