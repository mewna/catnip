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

package com.mewna.catnip.entity.impl.misc;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.RequiresCatnip;
import com.mewna.catnip.entity.misc.ApplicationOwner;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.entity.user.UserFlag;
import com.mewna.catnip.entity.util.ImageOptions;
import com.mewna.catnip.entity.util.ImageType;
import com.mewna.catnip.util.CDNFormat;
import lombok.*;
import lombok.experimental.Accessors;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@Builder
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationOwnerImpl implements ApplicationOwner, RequiresCatnip {
    private transient Catnip catnip;
    
    private long idAsLong;
    private String username;
    private String discriminator;
    private String avatar;
    private boolean bot;
    
    @Override
    public boolean isTeam() {
        return discriminator().equals("0000");
    }
    
    @Override
    public void catnip(@Nonnull final Catnip catnip) {
        this.catnip = catnip;
    }
    
    @Override
    @CheckReturnValue
    public boolean animatedAvatar() {
        return avatar != null && avatar.startsWith("a_");
    }
    
    @Nonnull
    @CheckReturnValue
    public String defaultAvatarUrl() {
        return CDNFormat.defaultAvatarUrl(discriminator);
    }
    
    @Nullable
    @CheckReturnValue
    public String avatarUrl(@Nonnull final ImageOptions options) {
        return CDNFormat.avatarUrl(id(), avatar, options);
    }
    
    @Nullable
    @CheckReturnValue
    public String avatarUrl() {
        return avatarUrl(defaultOptions());
    }
    
    @Nonnull
    @CheckReturnValue
    public String effectiveAvatarUrl(@Nonnull final ImageOptions options) {
        return avatar == null ? defaultAvatarUrl() : Objects.requireNonNull(
                avatarUrl(options),
                "Avatar url is null but avatar hash is present (??)"
        );
    }
    
    @Nonnull
    @CheckReturnValue
    public String effectiveAvatarUrl() {
        return effectiveAvatarUrl(defaultOptions());
    }
    
    @Nonnull
    @Override
    public Set<UserFlag> publicFlags() {
        throw new UnsupportedOperationException("unsupported method");
    }
    
    private ImageOptions defaultOptions() {
        return new ImageOptions().type(animatedAvatar() ? ImageType.GIF : null);
    }
    
    @Override
    public int hashCode() {
        return Long.hashCode(idAsLong);
    }
    
    @Override
    public boolean equals(final Object obj) {
        return obj instanceof User && ((User) obj).idAsLong() == idAsLong;
    }
    
    @Override
    public String toString() {
        return isTeam() ?
                String.format("Team (%s)", idAsLong) :
                String.format("User (%s#%s)", username, discriminator);
    }
    
    @Nullable
    @Override
    public String banner() {
        return null;
    }
    
    @Override
    public int accentColor() {
        return 0;
    }
}
