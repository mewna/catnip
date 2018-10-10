package com.mewna.catnip.entity.impl;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.RequiresCatnip;
import com.mewna.catnip.entity.guild.PartialGuild;
import com.mewna.catnip.entity.util.ImageOptions;
import com.mewna.catnip.entity.util.Permission;
import com.mewna.catnip.util.CDNFormat;
import lombok.*;
import lombok.experimental.Accessors;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

/**
 * @author natanbc
 * @since 9/12/18
 */
@Getter
@Setter
@Builder
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public class PartialGuildImpl implements PartialGuild, RequiresCatnip {
    private transient Catnip catnip;
    
    private String id;
    private String name;
    private String icon;
    private boolean owned;
    private Set<Permission> permissions;
    
    @Override
    public void catnip(@Nonnull final Catnip catnip) {
        this.catnip = catnip;
    }
    
    @Override
    @Nullable
    @CheckReturnValue
    public String iconUrl(@Nonnull final ImageOptions options) {
        return CDNFormat.iconUrl(id, icon, options);
    }
}
