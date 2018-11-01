package com.mewna.catnip.entity.channel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mewna.catnip.entity.impl.VoiceChannelImpl;

import javax.annotation.CheckReturnValue;

/**
 * @author natanbc
 * @since 9/12/18
 */
@JsonDeserialize(as = VoiceChannelImpl.class)
public interface VoiceChannel extends GuildChannel {
    @CheckReturnValue
    int bitrate();
    
    @CheckReturnValue
    int userLimit();
    
    @Override
    @JsonIgnore
    @CheckReturnValue
    default boolean isText() {
        return false;
    }
    
    @Override
    @JsonIgnore
    @CheckReturnValue
    default boolean isVoice() {
        return true;
    }
    
    @Override
    @JsonIgnore
    @CheckReturnValue
    default boolean isCategory() {
        return false;
    }
}
