package com.mewna.catnip.entity.misc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mewna.catnip.entity.Snowflake;
import com.mewna.catnip.entity.impl.TeamImpl;
import com.mewna.catnip.entity.util.ImageOptions;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Information about a team.
 *
 * @author Bowser65
 * @since 06/24/19.
 */
@JsonDeserialize(as = TeamImpl.class)
public interface Team extends Snowflake {
    /**
     * @return The name of the team.
     */
    @Nonnull
    String name();
    
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
    
    /**
     * The URL for the team's set icon. Can be null if the user has not set an avatar.
     *
     * @param options {@link ImageOptions Image Options}.
     *
     * @return String containing the URL to its icon, options considered. Can be null.
     */
    @Nullable
    @JsonIgnore
    @CheckReturnValue
    String iconUrl(@Nonnull final ImageOptions options);
    
    /**
     * The URL for the user's set avatar. Can be null if the user has not set an avatar.
     *
     * @return String containing the URL to its icon. Can be null.
     */
    @Nullable
    @JsonIgnore
    @CheckReturnValue
    String iconUrl();
    
    /**
     * @return The hash of the image of the team's icon, if set.
     */
    @Nullable
    String icon();
}
