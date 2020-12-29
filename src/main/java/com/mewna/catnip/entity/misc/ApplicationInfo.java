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

package com.mewna.catnip.entity.misc;

import com.mewna.catnip.entity.partials.*;
import com.mewna.catnip.entity.user.UserFlag;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

/**
 * Information about an application.
 *
 * @author amy
 * @since 10/17/18.
 */
public interface ApplicationInfo extends Snowflake, Nameable, HasIcon, NullDescribable, HasGuild {
    /**
     * @return A non-{@code null}, possibly-empty list of the application's RPC
     * origins.
     */
    @Nonnull
    List<String> rpcOrigins();
    
    /**
     * @return Whether or not the application is a public bot.
     */
    boolean publicBot();
    
    /**
     * @return Whether or not the application requires a code grant before the
     * bot can be added.
     */
    boolean requiresCodeGrant();
    
    /**
     * @return The entity that owns the application.
     */
    @Nonnull
    ApplicationOwner owner();
    
    /**
     * @return The team that owns the application. Can be null.
     */
    @Nullable
    Team team();
    
    /**
     * If this application is a game sold on Discord, this field will be the
     * summary field for the store page of its primary SKU.
     */
    @Nullable
    String summary();
    
    /**
     * @return The base64 encoded key for the GameSDK's {@code GetTicket}.
     */
    @Nullable
    String verifyKey();
    
    /**
     * If this application is a game sold on Discord, this field will be the
     * guild to which it has been linked.
     */
    @Nullable
    @Override
    default String guildId() {
        return Long.toUnsignedString(guildIdAsLong());
    }
    
    /**
     * If this application is a game sold on Discord, this field will be the id
     * of the "Game SKU" that is created, if exists.
     */
    @Nullable
    String primarySkuId();
    
    /**
     * If this application is a game sold on Discord, this field will be the
     * URL slug that links to the store page.
     */
    @Nullable
    String slug();
    
    /**
     * If this application is a game sold on Discord, this field will be the
     * hash of the image on store embeds.
     */
    @Nullable
    String coverImage();
    
    /**
     * @return The application's public flags.
     */
    @Nonnull
    Set<UserFlag> flags();
}
