package com.mewna.catnip.entity.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.RequiresCatnip;
import com.mewna.catnip.entity.message.BulkDeletedMessages;
import lombok.*;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author amy
 * @since 10/4/18.
 */
@Getter(onMethod_ = @JsonProperty)
@Setter(onMethod_ = @JsonProperty)
@Builder
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public class BulkDeletedMessagesImpl implements BulkDeletedMessages, RequiresCatnip {
    @JsonIgnore
    private transient Catnip catnip;
    
    private List<String> ids;
    private String channelId;
    private String guildId;
    
    @Override
    public void catnip(@Nonnull final Catnip catnip) {
        this.catnip = catnip;
    }
    
    @Override
    public int hashCode() {
        return ids.hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return obj instanceof BulkDeletedMessages && ((BulkDeletedMessages) obj).ids().equals(ids);
    }
    
    @Override
    public String toString() {
        return String.format("DeletedMessage (%s)", String.join(", ", ids));
    }
}
