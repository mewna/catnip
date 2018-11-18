package com.mewna.catnip.entity.channel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mewna.catnip.entity.impl.VoiceChannelImpl;

import javax.annotation.CheckReturnValue;

/**
 * A voice channel in a guild.
 *
 * @author natanbc
 * @since 9/12/18
 */
@JsonDeserialize(as = VoiceChannelImpl.class)
public interface VoiceChannel extends GuildChannel {
    /**
     * @return The bitrate of this channel. Will be from 8 to 96.
     */
    @CheckReturnValue
    int bitrate();
    
    /**
     * @return The maxmium number of users allowed in this voice channel at
     * once.
     */
    @CheckReturnValue
    int userLimit();
    
    /**
     * Opens a voice connection to this channel. This method is equivalent to
     * {@code channel.catnip().{@link com.mewna.catnip.Catnip#openVoiceConnection(String, String) openVoiceConnection}(channel.guildId(), channel.id())}
     *
     * @see com.mewna.catnip.Catnip#openVoiceConnection(String, String)
     */
    default void openVoiceConnection() {
        catnip().openVoiceConnection(guildId(), id());
    }
    
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
