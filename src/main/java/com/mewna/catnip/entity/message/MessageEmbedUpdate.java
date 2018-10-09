package com.mewna.catnip.entity.message;

import com.mewna.catnip.entity.Snowflake;
import jdk.internal.jline.internal.Nullable;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * When a message's embeds are updated, Discord sends a {@code MESSAGE_UPDATE}
 * event that only has id/channel_id/guild_id/embeds in the inner payload.
 * Because of this, we can't just use {@link Message} to represent this event.
 *
 * @author amy
 * @since 10/9/18.
 */
public interface MessageEmbedUpdate extends Snowflake {
    @Nullable
    String guildId();
    
    @Nonnull
    String channelId();
    
    @Nonnull
    List<Embed> embeds();
}
