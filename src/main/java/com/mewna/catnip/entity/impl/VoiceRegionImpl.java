package com.mewna.catnip.entity.impl;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.VoiceRegion;
import lombok.*;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;

/**
 * @author natanbc
 * @since 9/14/18
 */
@Getter
@Setter
@Builder
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public class VoiceRegionImpl implements VoiceRegion, RequiresCatnip {
    private transient Catnip catnip;
    
    private String id;
    private String name;
    private boolean vip;
    private boolean optimal;
    private boolean deprecated;
    private boolean custom;
    
    @Override
    public void catnip(@Nonnull final Catnip catnip) {
        this.catnip = catnip;
    }
}
