package com.mewna.catnip.entity.guild;

import com.mewna.catnip.entity.Snowflake;
import com.mewna.catnip.entity.util.Permission;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * @author amy
 * @since 9/4/18.
 */
@SuppressWarnings("unused")
public interface Role extends Snowflake {
    /**
     * The unique snowflake ID of this role.
     *
     * @return String representing the role ID.
     */
    @Nonnull
    String id();
    
    /**
     * The name of the role. Not unique
     *
     * @return String containing the role name.
     */
    @Nonnull
    String name();
    
    /**
     * The id of the guild this role is from.
     *
     * @return String representing the guild ID.
     */
    @Nonnull
    String guildId();
    
    /**
     * Integer representation of the role color.
     * <br>To use this, you must convert the integer to base-16, hex, format.
     * //TODO: Test default role color.
     *
     * @return The integer representation of the role color. Never null.
     */
    int color();
    
    /**
     * Whether or not the role "hoists" users with it into a group in the user list.
     *
     * @return True if the role is grouped and hoisted, false otherwise.
     */
    boolean hoist();
    
    /**
     * Permissions this role grants users in the guild.
     * <br>Channels may override and add to or remove from this.
     *
     * @return Set of permissions granted by this role. Never null.
     */
    @Nonnull
    Set<Permission> permissions();
    
    /**
     * Whether or not this role is managed by a 3rd party connection.
     * <br>Most often, this is seen on bots which were added using a permission parameter in their invite link.
     * <br>If true, this role cannot be modified by users through normal means.
     *
     * @return True if the role is managed, false otherwise.
     */
    boolean managed();
    
    /**
     * Whether or not mentioning this role will effectively mention those with it.
     * <br>Roles can always be mentioned, but will not mention users with it unless this is true.
     *
     * @return True if the role is mentionable, false otherwise.
     */
    boolean mentionable();
}
