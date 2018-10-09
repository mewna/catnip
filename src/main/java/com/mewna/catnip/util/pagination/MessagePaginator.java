package com.mewna.catnip.util.pagination;

import com.mewna.catnip.entity.impl.EntityBuilder;
import com.mewna.catnip.entity.message.Message;

import javax.annotation.Nonnull;

/**
 * @author natanbc
 * @since 10/9/18.
 */
public abstract class MessagePaginator extends ArrayOfObjectPaginator<Message, MessagePaginator> {
    protected MessagePaginator(@Nonnull final EntityBuilder builder) {
        super(Message::id, builder::createMessage, 100);
    }
}
