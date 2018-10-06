package com.mewna.catnip.entity.impl;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.RequiresCatnip;
import com.mewna.catnip.entity.voice.VoiceServerUpdate;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * @author amy
 * @since 10/6/18.
 */
@Getter
@Setter
@Builder
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public class VoiceServerUpdateImpl implements VoiceServerUpdate, RequiresCatnip {
    private transient Catnip catnip;
    
    private String token;
    private String guildId;
    private String endpoint;
}
