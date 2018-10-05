package com.mewna.catnip.entity.impl;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.RequiresCatnip;
import com.mewna.catnip.entity.guild.Role;
import com.mewna.catnip.entity.util.Permission;
import lombok.*;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * @author Julia Rogers
 * @since 9/2/18
 */
@Getter
@Setter
@Builder
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public class RoleImpl implements Role, RequiresCatnip {
    private transient Catnip catnip;
    
    private String id;
    private String guildId;
    private String name;
    private int color;
    private boolean hoist;
    private int position;
    private Set<Permission> permissions;
    private boolean managed;
    private boolean mentionable;
    
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
        return obj instanceof Role && ((Role)obj).id().equals(id);
    }
    
    @Override
    public String toString() {
        return String.format("Role (%s)", name);
    }
}
