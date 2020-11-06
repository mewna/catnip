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

package com.mewna.catnip.rest.guild;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.mewna.catnip.entity.guild.Member;
import com.mewna.catnip.entity.guild.Role;
import com.mewna.catnip.entity.user.VoiceState;
import io.reactivex.rxjava3.core.Single;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * @author SamOphis
 * @since 10/18/2018
 */

@Getter(onMethod_ = {@CheckReturnValue, @Nullable})
@Setter(onParam_ = @Nonnull, onMethod_ = {@CheckReturnValue, @Nonnull})
@Accessors(fluent = true, chain = true)
@NoArgsConstructor
@SuppressWarnings({"unused", "RedundantSuppression"})
public class MemberData {
    @Getter(AccessLevel.NONE)
    private Set<String> roles;
    
    private String nickname;
    @Setter(onParam_ = @Nullable, onMethod_ = {@CheckReturnValue, @Nonnull})
    private String channelId;
    private Boolean mute;
    private Boolean deaf;
    
    private static String blockingVoiceChannel(@Nonnull final Member member) {
        return Optional.ofNullable(member.catnip().cache().voiceState(member.guildId(), member.id()).blockingGet())
                .map(VoiceState::channelId).orElse(null);
    }
    
    @Nonnull
    @CheckReturnValue
    public static Single<MemberData> of(@Nonnull final Member member) {
        return member.voiceState()
                .map(state -> new MemberData()
                        .roles(member.roleIds())
                        .deaf(state.deaf() || state.selfDeaf())
                        .mute(state.mute() || state.selfMute())
                        .nickname(member.nick())
                        .channelId(state.channelId()))
                .defaultIfEmpty(new MemberData()
                        .roles(member.roleIds())
                        .deaf(false)
                        .mute(false)
                        .nickname(member.nick())
                        .channelId(null)
                );
    }
    
    @Nonnull
    @CheckReturnValue
    public static MemberData blockingOf(@Nonnull final Member member) {
        final VoiceState voiceState = member.voiceState().blockingGet();
        return new MemberData()
                .roles(member.roleIds())
                .deaf(voiceState != null ? member.deaf().blockingGet() : null)
                .mute(voiceState != null ? member.mute().blockingGet() : null)
                .nickname(member.nick())
                .channelId(voiceState != null ? voiceState.channelId() : null)
                ;
    }
    
    @Nonnull
    @CheckReturnValue
    public Collection<String> roles() {
        return List.copyOf(roles);
    }
    
    @Nonnull
    @CheckReturnValue
    @SuppressWarnings("TypeMayBeWeakened")
    public MemberData addRole(@Nonnull final Role role) {
        if (roles == null) {
            roles = new HashSet<>();
        }
        roles.add(role.id());
        return this;
    }
    
    @Nonnull
    @CheckReturnValue
    public MemberData addRole(@Nonnull final String roleId) {
        if (roles == null) {
            roles = new HashSet<>();
        }
        roles.add(roleId);
        return this;
    }
    
    @Nonnull
    @CheckReturnValue
    @SuppressWarnings("TypeMayBeWeakened")
    public MemberData removeRole(@Nonnull final Role role) {
        if (roles != null) {
            roles.remove(role.id());
        }
        return this;
    }
    
    @Nonnull
    @CheckReturnValue
    public MemberData removeRole(@Nonnull final String roleId) {
        if (roles != null) {
            roles.remove(roleId);
        }
        return this;
    }
    
    @Nonnull
    @CheckReturnValue
    public JsonObject toJson() {
        final JsonObject object = new JsonObject();
        if (roles != null) {
            final JsonArray array = new JsonArray();
            array.addAll(roles);
            object.put("roles", array);
        }
        if (nickname != null) {
            object.put("nick", nickname);
        }
        if (mute != null) {
            object.put("mute", mute);
        }
        if (deaf != null) {
            object.put("deaf", deaf);
        }
        object.put("channel_id", channelId);
        return object;
    }
}

