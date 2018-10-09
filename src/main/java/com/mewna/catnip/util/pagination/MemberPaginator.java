package com.mewna.catnip.util.pagination;

import com.mewna.catnip.entity.guild.Member;
import com.mewna.catnip.entity.impl.EntityBuilder;

import javax.annotation.Nonnull;

public abstract class MemberPaginator extends ArrayOfObjectPaginator<Member, MemberPaginator> {
    public MemberPaginator(@Nonnull final EntityBuilder builder, @Nonnull final String guildId) {
        super(Member::id, json -> builder.createMember(guildId, json), 100);
    }
}
