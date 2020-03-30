/*
 * Copyright (c) 2019 amy, All rights reserved.
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

package com.mewna.catnip.entity.impl.guild;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.RequiresCatnip;
import com.mewna.catnip.entity.Timestamped;
import com.mewna.catnip.entity.guild.Member;
import lombok.*;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.Set;

/**
 * @author amy
 * @since 9/1/18.
 */
@Getter
@Setter
@Builder
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public class MemberImpl implements Member, RequiresCatnip, Timestamped {
    private transient Catnip catnip;
    
    private long idAsLong;
    private long guildIdAsLong;
    private String nick;
    private Set<String> roleIds;
    private String joinedAt;
    private String premiumSince;
    
    @Setter(AccessLevel.NONE)
    private boolean deaf;
    @Setter(AccessLevel.NONE)
    private boolean mute;
    
    @Nonnull
    @Override
    public OffsetDateTime joinedAt() {
        return parseTimestamp(joinedAt);
    }
    
    @Nullable
    @Override
    public OffsetDateTime premiumSince() {
        return premiumSince == null ? null : parseTimestamp(premiumSince);
    }
    
    @Override
    public void catnip(@Nonnull final Catnip catnip) {
        this.catnip = catnip;
    }
    
    @Override
    public int hashCode() {
        return Long.hashCode(idAsLong);
    }
    
    @Override
    public boolean equals(final Object obj) {
        return obj instanceof Member && ((Member) obj).idAsLong() == idAsLong;
    }
    
    @Override
    public String toString() {
        return String.format("Member (%s, %s)", idAsLong, effectiveName());
    }
}
