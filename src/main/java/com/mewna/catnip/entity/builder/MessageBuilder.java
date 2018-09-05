package com.mewna.catnip.entity.builder;

import com.mewna.catnip.entity.Message;
import com.mewna.catnip.entity.impl.MessageImpl;
import com.mewna.catnip.entity.Embed;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;

/**
 * TODO: Add file support
 *
 * @author amy
 * @since 9/2/18.
 */
@SuppressWarnings("unused")
public class MessageBuilder {
    private String content;
    private Embed embed;
    
    public MessageBuilder() {
    }
    
    public MessageBuilder(final Message from) {
        content = from.content();
        embed = !from.embeds().isEmpty() ? from.embeds().get(0) : null;
    }
    
    @Nonnull
    @CheckReturnValue
    public MessageBuilder content(@Nullable final String content) {
        this.content = content;
        return this;
    }
    
    @Nonnull
    @CheckReturnValue
    public MessageBuilder embed(@Nullable final Embed embed) {
        this.embed = embed;
        return this;
    }
    
    @Nonnull
    @CheckReturnValue
    public Message build() {
        final MessageImpl m = new MessageImpl();
        m.content(content);
        if(embed != null) {
            m.embeds(Collections.singletonList(embed));
        }
        return m;
    }
}
