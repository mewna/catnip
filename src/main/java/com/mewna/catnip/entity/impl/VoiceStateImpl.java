package com.mewna.catnip.entity.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.RequiresCatnip;
import com.mewna.catnip.entity.guild.Member;
import com.mewna.catnip.entity.user.VoiceState;
import lombok.*;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author amy
 * @since 9/21/18.
 */
@Getter(onMethod_ = @JsonProperty)
@Setter(onMethod_ = @JsonProperty)
@Builder
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public class VoiceStateImpl implements VoiceState, RequiresCatnip {
    @JsonIgnore
    private transient Catnip catnip;
    
    @JsonProperty
    private String guildId;
    @JsonProperty
    private String channelId;
    @JsonProperty
    private String userId;
    @JsonProperty
    private String sessionId;
    @JsonProperty
    private boolean deaf;
    @JsonProperty
    private boolean mute;
    @JsonProperty
    private boolean selfDeaf;
    @JsonProperty
    private boolean selfMute;
    @JsonProperty
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
