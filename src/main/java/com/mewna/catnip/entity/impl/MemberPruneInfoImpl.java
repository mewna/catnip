package com.mewna.catnip.entity.impl;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.guild.audit.MemberPruneInfo;
import lombok.*;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;

/**
 * @author SamOphis
 * @since 10/07/2018
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(fluent = true)
public class MemberPruneInfoImpl implements MemberPruneInfo {
    private transient Catnip catnip;
    
    private int deleteMemberDays;
    private int removedMembersCount;
    
    @Override
    public void catnip(@Nonnull final Catnip catnip) {
        this.catnip = catnip;
    }
}
