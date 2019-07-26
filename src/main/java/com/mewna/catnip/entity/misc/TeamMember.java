package com.mewna.catnip.entity.misc;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mewna.catnip.entity.impl.misc.TeamMemberImpl;
import com.mewna.catnip.entity.user.User;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.List;

/**
 * Information about a team member.
 *
 * @author Bowser65
 * @since 06/24/19.
 */
@JsonDeserialize(as = TeamMemberImpl.class)
public interface TeamMember {
    /**
     * @return The membership state. Either 1 for pending, 2 for accepted.
     */
    int membershipState();
    
    /**
     * @return The permissions of the member. Will always be ["*"] as teams doesn't have permissions yet.
     */
    @Nonnull
    List<String> permissions();
    
    /**
     * @return The ID of the parent team they are member of.
     */
    @CheckReturnValue
    default String teamId() {
        return Long.toUnsignedString(teamIdAsLong());
    }
    
    /**
     * @return The ID of the parent team they are member of, as a long.
     */
    @CheckReturnValue
    long teamIdAsLong();
    
    /**
     * @return The user.
     */
    @Nonnull
    @CheckReturnValue
    User user();
}
