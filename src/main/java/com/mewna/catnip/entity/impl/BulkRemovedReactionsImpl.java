package com.mewna.catnip.entity.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.RequiresCatnip;
import com.mewna.catnip.entity.message.BulkRemovedReactions;
import lombok.*;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;

/**
 * @author amy
 * @since 10/6/18.
 */
@Getter(onMethod_ = @JsonProperty)
@Setter(onMethod_ = @JsonProperty)
@Builder
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public class BulkRemovedReactionsImpl implements BulkRemovedReactions, RequiresCatnip {
    @JsonIgnore
    private transient Catnip catnip;
    
    private String channelId;
    private String messageId;
    private String guildId;
    
    public void catnip(@Nonnull final Catnip catnip) {
        this.catnip = catnip;
    }
}
