package com.mewna.catnip.entity.guild;

import com.mewna.catnip.entity.Snowflake;

/**
 * An unavailable guild.
 *
 * @author amy
 * @since 10/4/18.
 */
public interface UnavailableGuild extends Snowflake {
    /**
     * @return Whether the guild is unavailable.
     */
    boolean unavailable();
}
