package com.mewna.catnip.entity.guild;

import com.mewna.catnip.entity.Snowflake;

/**
 * @author amy
 * @since 10/4/18.
 */
public interface UnavailableGuild extends Snowflake {
    boolean unavailable();
}
