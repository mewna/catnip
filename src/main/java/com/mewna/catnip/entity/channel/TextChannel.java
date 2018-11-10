package com.mewna.catnip.entity.channel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mewna.catnip.entity.impl.TextChannelImpl;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * A channel in a guild that can have text messages sent in it.
 *
 * @author natanbc
 * @since 9/12/18
 */
@SuppressWarnings("unused")
@JsonDeserialize(as = TextChannelImpl.class)
public interface TextChannel extends GuildChannel, MessageChannel {
    /**
     * The channel's topic. Shown at the top of the channel. May be
     * {@code null}.
     *
     * @return The channel's topic.
     */
    @Nullable
    @CheckReturnValue
    String topic();
    
    /**
     * @return Whether or not this channel has been marked as nsfw.
     */
    @CheckReturnValue
    boolean nsfw();
    
    /**
     * The slowmode set on this channel. A value of 0 means no slowmode. Bots
     * are not affected by slowmode.
     *
     * @return The slowmode set on this channel, in seconds.
     */
    @CheckReturnValue
    @Nonnegative
    int rateLimitPerUser();
    
    @Override
    @JsonIgnore
    @CheckReturnValue
    default boolean isText() {
        return true;
    }
    
    @Override
    @JsonIgnore
    @CheckReturnValue
    default boolean isVoice() {
        return false;
    }
    
    @Override
    @JsonIgnore
    @CheckReturnValue
    default boolean isCategory() {
        return false;
    }
    
    /**
     * Fetch all webhooks on this channel.
     *
     * @return A not-{@code null}, possibly-empty list of webhooks for this
     * channel.
     */
    @Nonnull
    @JsonIgnore
    @CheckReturnValue
    default CompletionStage<List<Webhook>> fetchWebhooks() {
        return catnip().rest().webhook().getChannelWebhooks(id());
    }
    
    /**
     * @return A mention for this channel that can be sent in a message.
     */
    @Nonnull
    @JsonIgnore
    @CheckReturnValue
    default String asMention() {
        return "<#" + id() + '>';
    }
}
