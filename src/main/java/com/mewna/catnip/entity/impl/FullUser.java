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

package com.mewna.catnip.entity.impl;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.Mentionable;
import com.mewna.catnip.entity.RequiresCatnip;
import com.mewna.catnip.entity.Snowflake;
import com.mewna.catnip.entity.guild.Member;
import com.mewna.catnip.entity.impl.user.PresenceImpl;
import com.mewna.catnip.entity.impl.user.UserImpl;
import com.mewna.catnip.entity.user.Presence;
import com.mewna.catnip.entity.user.Presence.Activity;
import com.mewna.catnip.entity.user.Presence.OnlineStatus;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.entity.user.UserFlag;
import com.mewna.catnip.entity.user.VoiceState;
import com.mewna.catnip.util.optics.Lens;
import com.mewna.catnip.util.optics.PartialLens;
import io.reactivex.rxjava3.core.Maybe;
import lombok.*;
import lombok.experimental.Accessors;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A mega-conglomeration of a user. Having such a class is admittedly kinda
 * messy, but is also meant to be strictly an implementation detail. Having a
 * class like this internally allows for a lot of deduplication to become
 * possible, ex. deduping presences and users with minimal effort, while ALSO
 * not needing to do something like iterating over the entire cache to check if
 * a user exists in any guilds.<p />
 * <p>
 * Rather than querying a cache for a specific value, this class exposes
 * partial views of itself via lenses. See {@link Lens} and {@link PartialLens}
 * for a bit more information; you should look up "functional programming
 * optics" for the proper in-depth explanation.
 *
 * @author amy
 * @since 9/29/20.
 */
@Builder(toBuilder = true)
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings({"unused", "RedundantSuppression"})
public final class FullUser implements Snowflake, Mentionable, RequiresCatnip {
    // Members aren't stored as a part of membership because there's a lot of
    // those. Instead, we just track guild IDs so we can know when to purge a
    // given user from the cache without having to iterate all guilds to check
    // for membership
    @Getter
    private final Set<Long> memberships = new HashSet<>();
    @Getter
    private transient Catnip catnip;
    
    // User
    @Getter
    private long idAsLong;
    private String username;
    private String discriminator;
    private String avatar;
    private boolean bot;
    private Set<UserFlag> publicFlags;
    
    // Presence
    private OnlineStatus status;
    private List<Activity> activities;
    private OnlineStatus mobileStatus;
    private OnlineStatus webStatus;
    private OnlineStatus desktopStatus;
    
    @Override
    public void catnip(@Nonnull final Catnip catnip) {
        this.catnip = catnip;
    }
    
    @Nonnull
    public String asMention() {
        return Objects.requireNonNull(user().get(this)).asMention();
    }
    
    public void addMembership(@Nonnegative final long id) {
        memberships.add(id);
    }
    
    public void removeMembership(@Nonnegative final long id) {
        memberships.remove(id);
    }
    
    // A user can be collected if they have no guild memberships left.
    public boolean canBeCollected() {
        return memberships.isEmpty();
    }
    
    @Nonnull
    public PartialLens<FullUser, Maybe<Member>> membership(@Nonnegative final long id) {
        if(!memberships.contains(id)) {
            throw new IllegalArgumentException("id " + id + " not a guild membership for " + idAsLong);
        }
        return PartialLens.of(fullUser -> catnip.cache().member(id, idAsLong));
    }
    
    @Nonnull
    public PartialLens<FullUser, Maybe<VoiceState>> voiceState(@Nonnegative final long id) {
        if(!memberships.contains(id)) {
            throw new IllegalArgumentException("id " + id + " not a guild membership for " + idAsLong);
        }
        return PartialLens.of(fullUser -> catnip.cache().voiceState(id, idAsLong));
    }
    
    @Nonnull
    public PartialLens<FullUser, User> user() {
        return PartialLens.<FullUser, User, UserImpl>lenseEntity(catnip, fullUser -> UserImpl.builder()
                .catnip(catnip)
                .idAsLong(idAsLong)
                .username(username)
                .discriminator(discriminator)
                .avatar(avatar)
                .bot(bot)
                .publicFlags(publicFlags)
                .build())
                // Note: Function.identity() does NOT work here!
                .compose(PartialLens.of(u -> u));
    }
    
    @Nonnull
    public PartialLens<FullUser, Presence> presence() {
        return PartialLens.<FullUser, Presence, PresenceImpl>lenseEntity(catnip, fullUser -> PresenceImpl.builder()
                .catnip(catnip)
                .status(status)
                .activities(activities)
                .mobileStatus(mobileStatus)
                .webStatus(webStatus)
                .desktopStatus(desktopStatus)
                .build())
                // Note: Function.identity() does NOT work here!
                .compose(PartialLens.of(p -> p));
    }
    
    @Nonnull
    public static Lens<FullUser, User> patchUser(@Nonnull final User user) {
        return Lens.of(__ -> user, (fullUser, patch) -> fullUser.toBuilder()
                .catnip(patch.catnip())
                .idAsLong(patch.idAsLong())
                .username(patch.username())
                .discriminator(patch.discriminator())
                .avatar(patch.avatar())
                .bot(patch.bot())
                .publicFlags(patch.publicFlags())
                .build());
    }
    
    @Nonnull
    public static Lens<FullUser, Presence> patchPresence(@Nonnull final Presence presence) {
        return Lens.of(__ -> presence, (fullUser, patch) -> fullUser.toBuilder()
                .catnip(patch.catnip())
                .status(presence.status())
                .activities(presence.activities())
                .mobileStatus(presence.mobileStatus())
                .webStatus(presence.webStatus())
                .desktopStatus(presence.desktopStatus())
                .build());
    }
}
