package com.mewna.catnip.entity.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.RequiresCatnip;
import com.mewna.catnip.entity.guild.audit.MemberPruneInfo;
import lombok.*;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;

/**
 * @author SamOphis
 * @since 10/07/2018
 */
@Getter(onMethod_ = @JsonProperty)
@Setter(onMethod_ = @JsonProperty)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(fluent = true)
public class MemberPruneInfoImpl implements MemberPruneInfo, RequiresCatnip {
    @JsonIgnore
    private transient Catnip catnip;
    
    private int deleteMemberDays;
    private int removedMembersCount;
    
    @Override
    public void catnip(@Nonnull final Catnip catnip) {
        this.catnip = catnip;
    }
}
