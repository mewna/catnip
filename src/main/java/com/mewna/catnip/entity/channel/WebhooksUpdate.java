package com.mewna.catnip.entity.channel;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mewna.catnip.entity.Entity;
import com.mewna.catnip.entity.impl.WebhooksUpdateImpl;

/**
 * @author amy
 * @since 11/10/18.
 */
@JsonDeserialize(as = WebhooksUpdateImpl.class)
public interface WebhooksUpdate extends Entity {
    String guildId();
    
    String channelId();
}
