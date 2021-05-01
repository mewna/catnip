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
import com.mewna.catnip.entity.channel.DMChannel;
import com.mewna.catnip.entity.channel.GuildChannel;
import com.mewna.catnip.entity.partials.HasJoinedAt;
import com.mewna.catnip.entity.partials.Mentionable;
import com.mewna.catnip.entity.partials.Permissable;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.entity.user.VoiceState;
import com.mewna.catnip.entity.util.Permission;
import com.mewna.catnip.util.PermissionUtil;
import com.mewna.catnip.util.rx.RxHelpers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

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
@SuppressWarnings({"unused", "RedundantSuppression"})
public interface Member extends Mentionable, Permissable, HasJoinedAt {
    /**
     * The user equivalent to this member.
     */
    @Nonnull
    @CheckReturnValue
    default Maybe<User> user() {
        return catnip().cache().user(idAsLong());
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
    default Single<String> effectiveName() {
        final String nick = nick();
        return nick != null ? Single.just(nick) : user().map(User::username).toSingle();
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
    default Single<Boolean> mute() {
        return voiceState().map(s -> s.selfMute() || s.mute()).defaultIfEmpty(false);
    }
    
    /**
     * Whether the user is deafened.
     * <br>Deafened users cannot receive nor send voice.
     *
     * @return True if deafened, false otherwise.
     */
    @CheckReturnValue
    default Single<Boolean> deaf() {
        return voiceState().map(s -> s.selfDeaf() || s.deaf()).defaultIfEmpty(false);
    }
    
    /**
     * @return The user's voice state in this guild. May be null.
     */
    @CheckReturnValue
    default Maybe<VoiceState> voiceState() {
        return guild().mapOptional(g -> Optional.ofNullable(g.voiceStates().getById(id())));
    }
    
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
    @Override
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
     * @return Future with the result of the DM creation.
     */
    @CheckReturnValue
    default Single<DMChannel> createDM() {
        return catnip().rest().user().createDM(id());
    }
    
    default Set<Permission> permissions() {
        return Permission.toSet(PermissionUtil.effectivePermissions(this));
    }
    
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
    default Single<Boolean> isOwner() {
        return guild().map(guild -> guild.ownerIdAsLong() == idAsLong()).toSingle();
    }
    
    /**
     * @return A mention for this member that can be sent in a message.
     */
    @Nonnull
    @CheckReturnValue
    default String asMention() {
        return nick() != null ? "<@!" + id() + '>' : "<@" + id() + '>' ;
    }
    
    /**
     * @param role The role to add.
     * @param target The member to add the role to.
     *
     * @return A {@code Completable} that completes when the role is added.
     */
    @Nonnull
    @CheckReturnValue
    default Completable addRole(@Nonnull final Role role, @Nonnull final Member target) {
        return addRole(role, target, null);
    }
    
    /**
     * @param roleId The id of the role to add.
     * @param memberId The id of the member to add the role to.
     *
     * @return A {@code Completable} that completes when the role is added.
     */
    @Nonnull
    @CheckReturnValue
    default Completable addRole(@Nonnull final String roleId, @Nonnull final String memberId) {
        return addRole(roleId, memberId, null);
    }
    
    /**
     * @param role   The role to add.
     *            @param target The member to add the role to.
     * @param reason The reason for adding the role.
     *
     * @return A {@code Completable} that completes when the role is added.
     */
    @Nonnull
    @CheckReturnValue
    default Completable addRole(@Nonnull final Role role, @Nonnull final Member target, @Nullable final String reason) {
        return addRole(role.id(), target.id(), reason);
    }
    
    /**
     * @param roleId The id of the role to add.
     *              @param memberId The id of the member to add the role to.
     * @param reason The reason for adding the role.
     *
     * @return A {@code Completable} that completes when the role is added.
     */
    @Nonnull
    @CheckReturnValue
    default Completable addRole(@Nonnull final String roleId, @Nonnull final String memberId, @Nullable final String reason) {
        PermissionUtil.checkPermissions(catnip(), guildId(), Permission.MANAGE_ROLES);
        return RxHelpers.resolveMany(
                guild(),
                catnip().cache().role(guildId(), roleId),
                catnip().cache().member(guildId(), memberId)
        ).flatMapCompletable(triple -> {
            final var guild = triple.getValue0();
            final var role = triple.getValue1();
            final var member = triple.getValue2();
            PermissionUtil.checkHierarchy(role, guild);
            PermissionUtil.checkHierarchy(member, guild);
            return catnip().rest().guild().addGuildMemberRole(guildId(), memberId, roleId, reason);
        });
    }
    
    /**
     * @param role The role to remove.
     *             @param member The member to remove from.
     *
     * @return A {@code Completable} that completes when the role is removed.
     */
    @Nonnull
    @CheckReturnValue
    default Completable removeRole(@Nonnull final Role role, @Nonnull final Member member) {
        return removeRole(role, member, null);
    }
    
    /**
     * @param roleId The id of the role to remove.
     * @param memberId The id of the member to remove from.
     *
     * @return A {@code Completable} that completes when the role is removed.
     */
    @Nonnull
    @CheckReturnValue
    default Completable removeRole(@Nonnull final String roleId, @Nonnull final String memberId) {
        return catnip().rest().guild().removeGuildMemberRole(guildId(), memberId, roleId);
    }
    
    /**
     * @param role   The role to remove.
     * @param member The member to remove from.
     * @param reason The reason for removing the role.
     *
     * @return A {@code Completable} that completes when the role is removed.
     */
    @Nonnull
    @CheckReturnValue
    default Completable removeRole(@Nonnull final Role role, @Nonnull final Member member, @Nullable final String reason) {
        return removeRole(role.id(), member.id(), reason);
    }
    
    /**
     * @param roleId The id of the role to remove.
     *               @param memberId The id of the member to remove from.
     * @param reason The reason for removing the role.
     *
     * @return A {@code Completable} that completes when the role is removed.
     */
    @Nonnull
    @CheckReturnValue
    default Completable removeRole(@Nonnull final String roleId, @Nonnull final String memberId, @Nullable final String reason) {
    
        PermissionUtil.checkPermissions(catnip(), guildId(), Permission.MANAGE_ROLES);
        return RxHelpers.resolveMany(
                guild(),
                catnip().cache().role(guildId(), roleId),
                catnip().cache().member(guildId(), memberId)
        ).flatMapCompletable(triple -> {
            final var guild = triple.getValue0();
            final var role = triple.getValue1();
            final var member = triple.getValue2();
            PermissionUtil.checkHierarchy(role, guild);
            PermissionUtil.checkHierarchy(member, guild);
            return catnip().rest().guild().removeGuildMemberRole(guildId(), memberId, roleId, reason);
        });
    }
}
