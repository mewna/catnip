/*
 * Copyright (c) 2018 amy, All rights reserved.
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

package com.mewna.catnip.entity.guild;

import com.mewna.catnip.cache.view.CacheView;
import com.mewna.catnip.entity.Mentionable;
import com.mewna.catnip.entity.channel.DMChannel;
import com.mewna.catnip.entity.channel.GuildChannel;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.entity.util.Permission;
import com.mewna.catnip.rest.guild.MemberData;
import com.mewna.catnip.util.PermissionUtil;
import io.reactivex.Completable;
import io.reactivex.Single;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A member in a guild.
 *
 * @author amy
 * @since 9/4/18.
 */
@SuppressWarnings("unused")
public interface Member extends Mentionable, PermissionHolder {
    /**
     * The user equivalent to this member.
     */
    @Nonnull
    @CheckReturnValue
    default User user() {
        return Objects.requireNonNull(catnip().cache().user(idAsLong()), "User not found. It may have been removed from the cache.");
    }
    
    /**
     * The user's nickname in this guild.
     *
     * @return User's nickname. Null if not set.
     */
    @Nullable
    @CheckReturnValue
    String nick();
    
    /**
     * The user's effective name show in this guild.
     *
     * @return User's nickname, if set, otherwise the username.
     */
    @Nonnull
    @CheckReturnValue
    default String effectiveName() {
        final String nick = nick();
        return nick != null ? nick : user().username();
    }
    
    /**
     * The ids of the user's roles in this guild.
     *
     * @return A {@link Set} of the ids of the user's roles.
     */
    @Nonnull
    @CheckReturnValue
    Set<String> roleIds();
    
    /**
     * The member's roles in the guild.
     *
     * @return A {@link Set} of the member's roles.
     */
    @Nonnull
    @CheckReturnValue
    default Set<Role> roles() {
        final CacheView<Role> roles = catnip().cache().roles(guildId());
        return roleIds().stream()
                .map(roles::getById)
                .collect(Collectors.toUnmodifiableSet());
    }
    
    /**
     * The member's roles in the guild, sorted from lowest to highest.
     *
     * @return A {@link List} of the member's roles.
     */
    @Nonnull
    @CheckReturnValue
    default List<Role> orderedRoles() {
        return orderedRoles(Comparator.naturalOrder());
    }
    
    @Nonnull
    @CheckReturnValue
    default List<Role> orderedRoles(final Comparator<Role> comparator) {
        final CacheView<Role> roles = catnip().cache().roles(guildId());
        final List<Role> ordered = new ArrayList<>(roleIds().size());
        for(final String id : roleIds()) {
            final Role role = roles.getById(id);
            if(role != null) {
                ordered.add(role);
            }
        }
        ordered.sort(comparator);
        return ordered;
    }
    
    /**
     * Whether the user is voice muted.
     * <br>Voice muted user cannot transmit voice.
     *
     * @return True if muted, false otherwise.
     */
    @CheckReturnValue
    boolean mute();
    
    /**
     * Whether the user is deafened.
     * <br>Deafened users cannot receive nor send voice.
     *
     * @return True if deafened, false otherwise.
     */
    @CheckReturnValue
    boolean deaf();
    
    /**
     * When the user joined the server last.
     * <br>Members who have joined, left, then rejoined will only have the most
     * recent join exposed.
     * <br>This may be null under some conditions, ex. a member leaving a
     * guild. In cases like this, catnip will attempt to load the old data from
     * the cache if possible, but it may not work, hence nullability.
     *
     * @return The {@link OffsetDateTime date and time} the member joined the guild.
     */
    @Nullable
    @CheckReturnValue
    OffsetDateTime joinedAt();
    
    /**
     * When the user last used their Nitro Boost on this guild.
     * <br>Members who have un-boosted a guild then re-boosted it will only
     * have the most recent boost exposed.
     * <br>This will be null if the user is not currently boosting the guild.
     *
     * @return The {@link OffsetDateTime date and time} when the member boosted
     * the guild.
     */
    @Nullable
    @CheckReturnValue
    OffsetDateTime premiumSince();
    
    /**
     * The member's color, as shown in the official Discord Client, or {@code null} if they have no roles with a color.
     * <br>This will iterate over all the roles this member has, so try to avoid calling this method multiple times
     * if you only need the value once.
     *
     * @return A {@link Color color} representing the member's color, as shown in the official Discord Client.
     */
    @Nullable
    @CheckReturnValue
    default Color color() {
        Role highest = null;
        
        final CacheView<Role> cache = catnip().cache().roles(guildId());
        for(final String id : roleIds()) {
            final Role role = cache.getById(id);
            if(role != null && role.color() != 0) {
                if(highest == null || role.compareTo(highest) > 0) {
                    highest = role;
                }
            }
        }
        return highest == null ? null : new Color(highest.color());
    }
    
    /**
     * Creates a DM channel with this member's user.
     *
     * @return Single which wraps the result of the DM creation.
     */
    @CheckReturnValue
    default Single<DMChannel> createDM() {
        return catnip().rest().user().createDM(id());
    }
    
    /**
     * Mutes/unmutes this member without specifying a reason.
     * <p>
     * </br><b>This requires you to have voice state cache enabled.</b>
     * </p>
     *
     * @param muted Whether to mute or unmute this member.
     * @return Completable which you can handle as you see fit.
     */
    default Completable mute(final boolean muted) {
        return mute(muted, null);
    }
    
    /**
     * Mutes/unmutes this member with an optional reason specified.
     * <p>
     * </br><b>This requires you to have voice state cache enabled.</b>
     * </p>
     *
     * @param muted Whether to mute or unmute this member.
     * @param reason Nullable string containing the reason for this action. {@code null} for no reason.
     * @return Completable which you can handle as you see fit.
     */
    default Completable mute(final boolean muted, @Nullable final String reason) {
        final var memberData = MemberData.of(this).mute(muted);
        return catnip().rest().guild().modifyGuildMember(guildId(), id(), memberData, reason);
    }
    
    /**
     * Deafens/undeafens this member without specifying a reason.
     * <p>
     * </br><b>This requires you to have voice state cache enabled.</b>
     * </p>
     *
     * @param deafened Whether to deafen or undeafen this member.
     * @return Completable which you can handle as you see fit.
     */
    default Completable deafen(final boolean deafened) {
        return deafen(deafened, null);
    }
    
    /**
     * Deafens/undeafens this member with an optional reason specified.
     * <p>
     * </br><b>This requires you to have voice state cache enabled.</b>
     * </p>
     *
     * @param deafened Whether to deafen or undeafen this member.
     * @param reason Nullable string containing the reason for this action. {@code null} for no reason.
     * @return Completable which you can handle as you see fit.
     */
    default Completable deafen(final boolean deafened, @Nullable final String reason) {
        final var memberData = MemberData.of(this).deaf(deafened);
        return catnip().rest().guild().modifyGuildMember(guildId(), id(), memberData, reason);
    }
    
    /**
     * Returns set of permissions this member effectively has.
     * @return Set containing the permissions this member effectively has.
     */
    default Set<Permission> permissions() {
        return Permission.toSet(PermissionUtil.effectivePermissions(this));
    }
    
    /**
     * Returns set of permissions this member effectively has in a given channel.
     * @param channel Channel to check permissions against.
     * @return Set of permissions this member effectively has, with respect to a given channel.
     */
    default Set<Permission> permissions(@Nonnull final GuildChannel channel) {
        return Permission.toSet(PermissionUtil.effectivePermissions(this, channel));
    }
    
    
    @Override
    default boolean hasPermissions(@Nonnull final Collection<Permission> permissions) {
        final long needed = Permission.from(permissions);
        final long actual = PermissionUtil.effectivePermissions(this);
        return (actual & needed) == needed;
    }
    
    @Override
    default boolean hasPermissions(@Nonnull final GuildChannel channel, @Nonnull final Collection<Permission> permissions) {
        final long needed = Permission.from(permissions);
        final long actual = PermissionUtil.effectivePermissions(this, channel);
        return (actual & needed) == needed;
    }
    
    @Override
    default boolean canInteract(@Nonnull final Role role) {
        return PermissionUtil.canInteract(this, role);
    }
    
    @Override
    default boolean canInteract(@Nonnull final Member member) {
        return PermissionUtil.canInteract(this, member);
    }
    
    /**
     * Checks if the member is the owner of the guild.
     *
     * @return Whether the member owns the guild or not
     */
    default boolean isOwner() {
        return guild().owner().equals(this);
    }
    
    /**
     * @return A mention for this member that can be sent in a message.
     */
    @Nonnull
    @CheckReturnValue
    default String asMention() {
        return nick() != null ? "<@!" + id() + '>' : "<@" + id() + '>';
    }
}
