package com.mewna.catnip.entity.builder;

import com.mewna.catnip.entity.message.Message;
import com.mewna.catnip.entity.impl.MessageImpl;
import com.mewna.catnip.entity.message.Embed;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;

/**
 * @author amy
 * @since 9/2/18.
 */
@Setter(onParam_ = @Nullable, onMethod_ = {@CheckReturnValue, @Nonnull})
@NoArgsConstructor
@Accessors(fluent = true, chain = true)
@SuppressWarnings("unused")
public class MessageBuilder {
    private String content;
    private Embed embed;
    
    public MessageBuilder(final Message from) {
        content = from.content();
        embed = !from.embeds().isEmpty() ? from.embeds().get(0) : null;
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
