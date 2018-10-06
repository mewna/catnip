package com.mewna.catnip.entity.voice;

import com.mewna.catnip.entity.Entity;

/**
 * @author amy
 * @since 10/6/18.
 */
public interface VoiceServerUpdate extends Entity {
    String token();
    
    String guildId();
    
    String endpoint();
}
