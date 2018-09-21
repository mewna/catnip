package com.mewna.catnip.entity.impl;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.Member;
import com.mewna.catnip.entity.VoiceState;
import lombok.*;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author amy
 * @since 9/21/18.
 */
@Getter
@Setter
@Builder
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public class VoiceStateImpl implements VoiceState, RequiresCatnip {
    private transient Catnip catnip;
    
    private String guildId;
    private String channelId;
    private String userId;
    private String sessionId;
    private boolean deaf;
    private boolean mute;
    private boolean selfDeaf;
    private boolean selfMute;
    private boolean suppress;
    
    @Nullable
    @Override
    public Member member() {
        if(guildId == null) {
            return null;
        } else {
            return catnip.cache().member(guildId, userId);
        }
    }
    
    public void catnip(@Nonnull final Catnip catnip) {
        this.catnip = catnip;
    }
}
