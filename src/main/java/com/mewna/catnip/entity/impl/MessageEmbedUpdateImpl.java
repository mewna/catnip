package com.mewna.catnip.entity.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.RequiresCatnip;
import com.mewna.catnip.entity.message.Embed;
import com.mewna.catnip.entity.message.MessageEmbedUpdate;
import lombok.*;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author amy
 * @since 10/9/18.
 */
@Getter(onMethod_ = @JsonProperty)
@Setter(onMethod_ = @JsonProperty)
@Builder
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public class MessageEmbedUpdateImpl implements MessageEmbedUpdate, RequiresCatnip {
    @JsonIgnore
    private transient Catnip catnip;
    
    private String id;
    private String guildId;
    private String channelId;
    private List<Embed> embeds;
    
    @Override
    public void catnip(@Nonnull final Catnip catnip) {
        this.catnip = catnip;
    }
}
