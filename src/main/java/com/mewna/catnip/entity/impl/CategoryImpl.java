package com.mewna.catnip.entity.impl;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.Category;
import lombok.*;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;

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
public class CategoryImpl implements Category, RequiresCatnip {
    private transient Catnip catnip;
    
    private String id;
    private ChannelType type;
    private String name;
    private String guildId;
    private int position;
    private String parentId;
    
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
        return obj instanceof Category && ((Category)obj).id().equals(id);
    }
    
    @Override
    public String toString() {
        return String.format("Category (%s)", name);
    }
}
