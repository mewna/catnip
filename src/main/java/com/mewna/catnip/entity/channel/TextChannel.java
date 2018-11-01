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
 * @author natanbc
 * @since 9/12/18
 */
@SuppressWarnings("unused")
@JsonDeserialize(as = TextChannelImpl.class)
public interface TextChannel extends GuildChannel, MessageChannel {
    @Nullable
    @CheckReturnValue
    String topic();
    
    @CheckReturnValue
    boolean nsfw();
    
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
    
    @Nonnull
    @JsonIgnore
    @CheckReturnValue
    default CompletionStage<List<Webhook>> fetchWebhooks() {
        return catnip().rest().webhook().getChannelWebhooks(id());
    }
    
    @Nonnull
    @JsonIgnore
    @CheckReturnValue
    default String asMention() {
        return "<#" + id() + '>';
    }
}
