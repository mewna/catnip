package com.mewna.catnip.entity.impl;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.DeletedMessage;
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
public class DeletedMessageImpl implements DeletedMessage {
    private transient Catnip catnip;
    
    private String id;
    private String channelId;
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
        return obj instanceof DeletedMessage && ((DeletedMessage) obj).id().equals(id);
    }
    
    @Override
    public String toString() {
        return String.format("DeletedMessage (%s)", id);
    }
}
