package com.mewna.catnip.entity.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.RequiresCatnip;
import com.mewna.catnip.entity.guild.PermissionOverride;
import com.mewna.catnip.entity.util.Permission;
import lombok.*;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * @author natanbc
 * @since 9/15/18
 */
@Getter(onMethod_ = @JsonProperty)
@Setter(onMethod_ = @JsonProperty)
@Builder
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public class PermissionOverrideImpl implements PermissionOverride, RequiresCatnip {
    @JsonIgnore
    private transient Catnip catnip;
    
    private String id;
    private OverrideType type;
    private Set<Permission> allow;
    private Set<Permission> deny;
    
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
        return obj instanceof PermissionOverride && ((PermissionOverride)obj).id().equals(id);
    }
    
    @Override
    public String toString() {
        return String.format("PermissionOverride (%s)", id);
    }
}
