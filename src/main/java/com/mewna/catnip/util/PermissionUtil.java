/*
 *
 *  * Copyright (c) 2018 amy, All rights reserved.
 *  *
 *  * Redistribution and use in source and binary forms, with or without
 *  * modification, are permitted provided that the following conditions are met:
 *  *
 *  * 1. Redistributions of source code must retain the above copyright notice, this
 *  *    list of conditions and the following disclaimer.
 *  * 2. Redistributions in binary form must reproduce the above copyright notice,
 *  *    this list of conditions and the following disclaimer in the
 *  *    documentation and/or other materials provided with the distribution.
 *  * 3. Neither the name of the copyright holder nor the names of its contributors
 *  *    may be used to endorse or promote products derived from this software without
 *  *    specific prior written permission.
 *  *
 *  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 *  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 *  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package com.mewna.catnip.util;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.channel.GuildChannel;
import com.mewna.catnip.entity.guild.*;
import com.mewna.catnip.entity.partials.Permissable;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.entity.util.Permission;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

@SuppressWarnings("ConstantConditions")
public final class PermissionUtil {
    private PermissionUtil() {
    }
    
    private static long basePermissions(final Permissable holder) {
        final Catnip catnip = holder.catnip();
        final Guild guild = catnip.cache().guild(holder.guildId()).blockingGet();
        // Could be simplified to just Guild.role($.idAsLong()), but custom caches are a thing with nullable guilds.
        final Role publicRole = catnip.cache().role(holder.guildId(), holder.guildId()).blockingGet();
        if (guild == null || publicRole == null || guild.ownerId().equals(holder.id())) {
            return Permission.ALL;
        }
        
        long permissions = publicRole.permissionsRaw();
        if (holder instanceof Member) {
            for(final Role role : ((Member) holder).orderedRoles()) {
                permissions |= role.permissionsRaw();
            }
        }
        if (Permission.ADMINISTRATOR.isPresent(permissions)) {
            return Permission.ALL;
        }
        return permissions;
    }
    
    private static long overridePermissions(final long base, final Permissable holder, final GuildChannel channel) {
        if (Permission.ADMINISTRATOR.isPresent(base)) {
            return Permission.ALL;
        }
        long permissions = base;
        final Collection<PermissionOverride> list = channel.overrides();
        final PermissionOverride everyoneOverride = find(list, holder.guildId());
        if (everyoneOverride != null) {
            permissions &= ~everyoneOverride.denyRaw();
            permissions |= everyoneOverride.allowRaw();
        }
        long deny = Permission.NONE;
        long allow = Permission.NONE;
        if (holder instanceof Member) {
            for(final String role : ((Member) holder).roleIds()) {
                final PermissionOverride override = find(list, role);
                if (override != null) {
                    allow |= override.allowRaw();
                    deny |= override.denyRaw();
                }
            }
        }
        permissions &= ~deny;
        permissions |= allow;
        final PermissionOverride memberOverride = find(list, holder.id());
        if (memberOverride != null) {
            permissions &= ~memberOverride.denyRaw();
            permissions |= memberOverride.allowRaw();
        }
        return permissions;
    }
    
    private static PermissionOverride find(final Collection<PermissionOverride> list, final String id) {
        for(final PermissionOverride p : list) {
            if (p.id().equals(id)) {
                return p;
            }
        }
        return null;
    }
    
    public static long effectivePermissions(@Nonnull final Permissable member) {
        return basePermissions(member);
    }
    
    public static long effectivePermissions(@Nonnull final Permissable member, @Nonnull final GuildChannel channel) {
        return overridePermissions(basePermissions(member), member, channel);
    }
    
    public static void checkPermissions(@Nonnull final Catnip catnip, @Nullable final String guildId,
                                        @Nonnull final Permission... permissions) {
        if (!catnip.options().enforcePermissions() || guildId == null) {
            return;
        }
        final User me = catnip.selfUser().blockingGet();
        if (me == null) {
            return;
        }
        final Member self = catnip.cache().member(guildId, me.id()).blockingGet();
        if (self == null) {
            return;
        }
        final long needed = Permission.from(permissions);
        final long actual = effectivePermissions(self);
        if ((actual & needed) != needed) {
            final long missing = needed & ~actual;
            throw new MissingPermissionException(Permission.toSet(missing));
        }
    }
    
    public static void checkPermissions(@Nonnull final Catnip catnip, @Nullable final String guildId,
                                        @Nullable final String channelId, @Nonnull final Permission... permissions) {
        if (!catnip.options().enforcePermissions() || guildId == null || channelId == null) {
            return;
        }
        final User me = catnip.selfUser().blockingGet();
        if (me == null) {
            return;
        }
        final Member self = catnip.cache().member(guildId, me.id()).blockingGet();
        final GuildChannel channel = catnip.cache().channel(guildId, channelId).blockingGet();
        if (self == null || channel == null) {
            return;
        }
        final long needed = Permission.from(permissions);
        final long actual = effectivePermissions(self, channel);
        if ((actual & needed) != needed) {
            final long missing = needed & ~actual;
            throw new MissingPermissionException(Permission.toSet(missing));
        }
    }
    
    public static void checkHierarchy(@Nonnull final Member target, @Nonnull final Guild guild) {
        final Member self = guild.selfMember().blockingGet();
        if (!self.canInteract(target)) {
            throw new HierarchyException(self, target);
        }
    }
    
    public static void checkHierarchy(@Nonnull final Role target, @Nonnull final Guild guild) {
        final Member self = guild.selfMember().blockingGet();
        if (!self.canInteract(target)) {
            throw new HierarchyException(self, target);
        }
    }
    
    /**
     * Checks whether a member has the permission to interact with another member
     *
     * @param actor  The member that want to perform the action
     * @param target The member that the action is performed on
     *
     * @return Whether the actor can interact with the target or not
     *
     * @throws IllegalStateException If the actor is not on the same guild as the target
     */
    public static boolean canInteract(@Nonnull final Member actor, @Nonnull final Member target) {
        if (actor.isOwner().blockingGet()) {
            return true;
        }
        if (target.isOwner().blockingGet()) {
            return false;
        }
        if (actor.orderedRoles().isEmpty()) {
            return actor.isOwner().blockingGet();
        }
        return canInteract(actor.orderedRoles(Collections.reverseOrder()).iterator().next(), target);
    }
    
    /**
     * Checks whether a role has the permission to interact with a member
     *
     * @param actor  The role that want to perform the action
     * @param target The member that the action is performed on
     *
     * @return Whether the actor can interact with the target or not
     *
     * @throws IllegalStateException If the actor is not on the same guild as the target
     */
    public static boolean canInteract(@Nonnull final Role actor, @Nonnull final Member target) {
        checkGuildEquality(actor, target);
        // Nobody can interact with the owner
        if (target.isOwner().blockingGet()) {
            return false;
        }
        if (target.orderedRoles().isEmpty()) {
            return true;
        }
        return canInteract(actor, target.orderedRoles(Comparator.reverseOrder()).iterator().next());
    }
    
    /**
     * Checks whether a member has the permission to interact with a role
     *
     * @param actor  The member that want to perform the action
     * @param target The role that the action is performed on
     *
     * @return Whether the actor can interact with the target or not
     *
     * @throws IllegalStateException If the actor is not on the same guild as the target
     */
    public static boolean canInteract(@Nonnull final Member actor, @Nonnull final Role target) {
        checkGuildEquality(actor, target);
        // Owner has any permission event if he has not a single role
        if (actor.isOwner().blockingGet()) {
            return true;
        }
        if (actor.orderedRoles().isEmpty()) {
            return actor.isOwner().blockingGet();
        }
        // Check if the highest role of the actor is higher than the role of the target
        return canInteract(actor.orderedRoles(Comparator.reverseOrder()).iterator().next(), target);
    }
    
    /**
     * Checks whether a role has the permission to interact with another role
     *
     * @param actor  The role that want to perform the action
     * @param target The role that the action is performed on
     *
     * @return Whether the actor can interact with the target or not
     *
     * @throws IllegalStateException If the actor is not on the same guild as the target
     */
    public static boolean canInteract(@Nonnull final Role actor, @Nonnull final Role target) {
        checkGuildEquality(actor, target);
        return actor.position() > target.position();
    }
    
    private static void checkGuildEquality(final GuildEntity actor, final GuildEntity target) {
        if (!actor.guild().equals(target.guild())) {
            throw new IllegalStateException("Actor and target mus be on the same guild!");
        }
    }
}
