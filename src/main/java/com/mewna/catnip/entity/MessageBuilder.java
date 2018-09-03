package com.mewna.catnip.entity;

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
        throw new UnsupportedOperationException("Build-from-message is currently unsupported.");
    }
    
    public MessageBuilder content(final String content) {
        this.content = content;
        return this;
    }
    public MessageBuilder embed(final Embed embed) {
        this.embed = embed;
        return this;
    }
    
    public Message build() {
        final Message m = new Message();
        m.setContent(content);
        m.setEmbeds(Collections.singletonList(embed));
        return m;
    }
}
