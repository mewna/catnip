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

package com.mewna.catnip.entity.channel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mewna.catnip.entity.impl.GroupDMChannelImpl;
import com.mewna.catnip.entity.user.User;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.List;

/**
 * A DM with a group of users.
 *
 * @author natanbc
 * @since 9/12/18
 */
@SuppressWarnings("unused")
@JsonDeserialize(as = GroupDMChannelImpl.class)
public interface GroupDMChannel extends DMChannel {
    @Nonnull
    @Override
    default ChannelType type() {
        return ChannelType.GROUP_DM;
    }
    
    /**
     * @return The list of users in the group DM.
     */
    @Nonnull
    @CheckReturnValue
    List<User> recipients();
    
    /**
     * @return The hash for the group DM's icon.
     */
    @CheckReturnValue
    String icon();
    
    /**
     * @return The ID of the user who owns the group DM.
     */
    @Nonnull
    @CheckReturnValue
    String ownerId();
    
    /**
     * @return The ID of the application that created the group DM.
     */
    @CheckReturnValue
    String applicationId();
    
    @Override
    @JsonIgnore
    @CheckReturnValue
    default boolean isUserDM() {
        return false;
    }
    
    @Override
    @JsonIgnore
    @CheckReturnValue
    default boolean isGroupDM() {
        return true;
    }
}
