package com.mewna.catnip.entity.impl;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.RequiresCatnip;
import com.mewna.catnip.entity.guild.PartialRole;
import lombok.*;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;

/**
 * @author amy
 * @since 10/4/18.
 */
@Getter
@Setter
@Builder
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public class PartialRoleImpl implements PartialRole, RequiresCatnip {
    private transient Catnip catnip;
    
    private String id;
    private String guildId;
    
    @Override
    public void catnip(@Nonnull final Catnip catnip) {
        this.catnip = catnip;
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return obj instanceof PartialRole && ((PartialRole) obj).id().equals(id);
    }
    
    @Override
    public String toString() {
        return String.format("PartialRole (%s)", id);
    }
}
