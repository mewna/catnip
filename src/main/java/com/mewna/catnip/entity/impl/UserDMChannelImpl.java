package com.mewna.catnip.entity.impl;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.User;
import com.mewna.catnip.entity.UserDMChannel;
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
public class UserDMChannelImpl implements UserDMChannel, RequiresCatnip {
    private transient Catnip catnip;
    
    private String id;
    private ChannelType type;
    private User recipient;
    
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
        return obj instanceof UserDMChannel && ((UserDMChannel)obj).id().equals(id);
    }
    
    @Override
    public String toString() {
        return String.format("UserDMChannel (%s)", recipient);
    }
}
