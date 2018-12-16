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
import com.mewna.catnip.entity.guild.Guild;
import com.mewna.catnip.entity.guild.Member;
import com.mewna.catnip.entity.guild.PermissionOverride;
import com.mewna.catnip.entity.guild.Role;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.entity.util.Permission;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public final class PermissionUtil {
    private PermissionUtil() {}
    
    private static long basePermissions(final Catnip catnip, final Member member) {
        final Guild guild = catnip.cache().guild(member.guildId());
        final Role publicRole = catnip.cache().role(member.guildId(), member.guildId());
        if(guild == null || publicRole == null || guild.ownerId().equals(member.id())) {
            return Permission.ALL;
        }
        long permissions = publicRole.permissionsRaw();
        for(final Role role : member.roles()) {
            permissions |= role.permissionsRaw();
        }
        if(Permission.ADMINISTRATOR.isPresent(permissions)) {
            return Permission.ALL;
        }
        return permissions;
    }
    
    private static long overridePermissions(final long base, final Member member, final GuildChannel channel) {
        if(Permission.ADMINISTRATOR.isPresent(base)) {
            return Permission.ALL;
        }
        long permissions = base;
        final Collection<PermissionOverride> list = channel.overrides();
        final PermissionOverride everyoneOverride = find(list, member.guildId());
        if(everyoneOverride != null) {
            permissions &= ~everyoneOverride.denyRaw();
            permissions |= everyoneOverride.allowRaw();
        }
        long deny = Permission.NONE;
        long allow = Permission.NONE;
        for(final String role : member.roleIds()) {
            final PermissionOverride override = find(list, role);
            if(override != null) {
                allow |= override.allowRaw();
                deny |= override.denyRaw();
            }
        }
        permissions &= ~deny;
        permissions |= allow;
        final PermissionOverride memberOverride = find(list, member.id());
        if(memberOverride != null) {
            permissions &= ~memberOverride.denyRaw();
            permissions |= memberOverride.allowRaw();
        }
        return permissions;
    }
    
    private static PermissionOverride find(final Collection<PermissionOverride> list, final String id) {
        for(final PermissionOverride p : list) {
            if(p.id().equals(id)) {
                return p;
            }
        }
        return null;
    }
    
    public static long effectivePermissions(@Nonnull final Catnip catnip, @Nonnull final Member member) {
        return basePermissions(catnip, member);
    }
    
    public static long effectivePermissions(@Nonnull final Catnip catnip, @Nonnull final Member member,
                                            @Nonnull final GuildChannel channel) {
        return overridePermissions(basePermissions(catnip, member), member, channel);
    }
    
    public static void checkPermissions(@Nonnull final Catnip catnip, @Nullable final String guildId,
                                        @Nonnull final Permission... permissions) {
        if(!catnip.enforcePermissions() || guildId == null) {
            return;
        }
        final User me = catnip.selfUser();
        if(me == null) {
            return;
        }
        final Member self = catnip.cache().member(guildId, me.id());
        if(self == null) {
            return;
        }
        final long needed = Permission.from(permissions);
        final long actual = effectivePermissions(catnip, self);
        if((actual & needed) != needed) {
            final long missing = needed & ~actual;
            throw new MissingPermissionException(Permission.toSet(missing));
        }
    }
    
    public static void checkPermissions(@Nonnull final Catnip catnip, @Nullable final String guildId,
                                        @Nullable final String channelId, @Nonnull final Permission... permissions) {
        if(!catnip.enforcePermissions() || guildId == null || channelId == null) {
            return;
        }
        final User me = catnip.selfUser();
        if(me == null) {
            return;
        }
        final Member self = catnip.cache().member(guildId, me.id());
        final GuildChannel channel = catnip.cache().channel(guildId, channelId);
        if(self == null || channel == null) {
            return;
        }
        final long needed = Permission.from(permissions);
        final long actual = effectivePermissions(catnip, self, channel);
        if((actual & needed) != needed) {
            final long missing = needed & ~actual;
            throw new MissingPermissionException(Permission.toSet(missing));
        }
    }
}
