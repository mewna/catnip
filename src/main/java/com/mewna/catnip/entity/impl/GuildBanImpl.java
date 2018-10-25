package com.mewna.catnip.entity.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.RequiresCatnip;
import com.mewna.catnip.entity.guild.GuildBan;
import com.mewna.catnip.entity.user.User;
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
public class GuildBanImpl implements GuildBan, RequiresCatnip {
    @JsonIgnore
    private transient Catnip catnip;
    
    private String reason;
    private User user;
    
    @Override
    public void catnip(@Nonnull final Catnip catnip) {
        this.catnip = catnip;
    }
}
