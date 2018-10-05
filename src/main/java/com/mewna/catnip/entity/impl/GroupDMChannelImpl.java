package com.mewna.catnip.entity.impl;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.channel.GroupDMChannel;
import com.mewna.catnip.entity.user.User;
import lombok.*;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import java.util.List;

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
public class GroupDMChannelImpl implements GroupDMChannel, RequiresCatnip {
    private transient Catnip catnip;
    
    private String id;
    private ChannelType type;
    private List<User> recipients;
    private String icon;
    private String ownerId;
    private String applicationId;
    
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
        return obj instanceof GroupDMChannel && ((GroupDMChannel)obj).id().equals(id);
    }
    
    @Override
    public String toString() {
        return String.format("GroupDMChannel (%s)", recipients);
    }
}
