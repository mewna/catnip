package com.mewna.catnip.entity.channel;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mewna.catnip.entity.Entity;
import com.mewna.catnip.entity.impl.WebhooksUpdateImpl;

import javax.annotation.Nonnull;

/**
 * Fired over the event bus when a webhook update event is received.
 *
 * @author amy
 * @since 11/10/18.
 */
@JsonDeserialize(as = WebhooksUpdateImpl.class)
public interface WebhooksUpdate extends Entity {
    /**
     * @return The id of the guild that webhooks were updated in.
     */
    @Nonnull
    String guildId();
    
    /**
     * @return The id of the channel that webhooks were updated in.
     */
    String channelId();
}
