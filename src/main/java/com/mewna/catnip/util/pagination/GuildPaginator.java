package com.mewna.catnip.util.pagination;

import com.mewna.catnip.entity.guild.PartialGuild;
import com.mewna.catnip.entity.impl.EntityBuilder;

import javax.annotation.Nonnull;

public abstract class GuildPaginator extends ArrayOfObjectPaginator<PartialGuild, GuildPaginator> {
    public GuildPaginator(@Nonnull final EntityBuilder builder) {
        super(PartialGuild::id, builder::createPartialGuild, 100);
    }
}
