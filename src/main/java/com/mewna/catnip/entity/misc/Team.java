package com.mewna.catnip.entity.misc;

import com.mewna.catnip.entity.partials.HasIcon;
import com.mewna.catnip.entity.partials.HasName;
import com.mewna.catnip.entity.partials.Snowflake;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.List;

/**
 * Information about a team.
 *
 * @author Bowser65
 * @since 06/24/19.
 */
public interface Team extends Snowflake, HasName, HasIcon {
    /**
     * The ID of the team owner
     *
     * @return String representing their ID.
     */
    @CheckReturnValue
    default String ownerId() {
        return Long.toUnsignedString(ownerIdAsLong());
    }
    
    /**
     * The ID of the team owner, as a long.
     *
     * @return Long representing their ID.
     */
    @CheckReturnValue
    long ownerIdAsLong();
    
    /**
     * @return The members of the team
     */
    @Nonnull
    List<TeamMember> members();
}
