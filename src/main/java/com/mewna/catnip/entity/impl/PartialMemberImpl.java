package com.mewna.catnip.entity.impl;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.RequiresCatnip;
import com.mewna.catnip.entity.guild.PartialMember;
import com.mewna.catnip.entity.user.User;
import lombok.*;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import java.util.Set;

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
public class PartialMemberImpl implements PartialMember, RequiresCatnip {
    private transient Catnip catnip;
    
    private User user;
    private String guildId;
    private Set<String> roleIds;
    private String nick;
    
    @Override
    public void catnip(@Nonnull final Catnip catnip) {
        this.catnip = catnip;
    }
    
    @Override
    public int hashCode() {
        return id().hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return obj instanceof PartialMember && ((PartialMember) obj).id().equals(id());
    }
    
    @Override
    public String toString() {
        return String.format("PartialMember (%s)", id());
    }
}
