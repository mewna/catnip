package com.mewna.catnip.util.pagination;

import com.mewna.catnip.entity.impl.EntityBuilder;
import com.mewna.catnip.entity.user.User;
import javax.annotation.Nonnull;

/**
 * @author natanbc
 * @since 10/9/18.
 */
public abstract class ReactionPaginator extends ArrayOfObjectPaginator<User, ReactionPaginator> {
    protected ReactionPaginator(@Nonnull final EntityBuilder builder) {
        super(User::id, builder::createUser, 100);
    }
}
