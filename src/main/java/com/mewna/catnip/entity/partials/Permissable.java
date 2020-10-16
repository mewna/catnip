/*
 * Copyright (c) 2020 amy, All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.mewna.catnip.entity.partials;

import com.mewna.catnip.entity.guild.GuildEntity;
import com.mewna.catnip.entity.guild.Member;
import com.mewna.catnip.entity.guild.Role;
import com.mewna.catnip.entity.partials.Snowflake;
import com.mewna.catnip.entity.channel.GuildChannel;
import com.mewna.catnip.entity.util.Permission;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

/**
 * An entity which is permission-scoped in catnip.
 *
 * @author schlaubi
 * @since 13/2/19.
 */
public interface Permissable extends GuildEntity, Snowflake {
    
    /**
     * Returns a list of the entity's permissions.
     * @return a list of the entity's permissions
     */
    Set<Permission> permissions();
    
    /**
     * Returns the raw permissions of the entity as a long.
     * @return the raw permissions of the entity as a long
     */
    default long permissionsRaw() {
        return Permission.from(permissions());
    }
    
    /**
     * Checks whether the entity has the permissions or not.
     * @param permissions The permissions to check.
     * @return Whether the entity has the permissions or not
     */
    boolean hasPermissions(@Nonnull final Collection<Permission> permissions);
    
    /**
     * Checks whether the entity has the permissions or not.
     * @param permissions The permissions to check.
     * @return Whether the entity has the permissions or not
     */
    default boolean hasPermissions(@Nonnull final Permission... permissions) {
        return hasPermissions(Arrays.asList(permissions));
    }
    
    /**
     * Checks whether the entity has the permissions or not in a specific {@link GuildChannel}.
     * @param channel The channel in which the entity should have the permission
     * @param permissions The permissions to check.
     * @return Whether the entity has the permissions or not
     */
    boolean hasPermissions(@Nonnull final GuildChannel channel, @Nonnull final Collection<Permission> permissions);
    
    /**
     * Checks whether the entity has the permissions or not in a specific {@link GuildChannel}.
     * @param channel The channel in which the entity should have the permission.
     * @param permissions The permissions to check.
     * @return Whether the entity has the permissions or not
     */
    default boolean hasPermissions(@Nonnull final GuildChannel channel, @Nonnull final Permission... permissions) {
        return hasPermissions(channel, Arrays.asList(permissions));
    }
    
    /**
     * Checks whether the entity can interact with a role or not.
     * @param role The role the entity should interact with.
     * @return Whether the entity can interact with a role or not
     */
    boolean canInteract(@Nonnull final Role role);
    
    /**
     * Checks whether the entity can interact with a member or not.
     * @param member The member the entity should interact with.
     * @return Whether the entity can interact with a member or not
     */
    boolean canInteract(@Nonnull final Member member);
    
}
