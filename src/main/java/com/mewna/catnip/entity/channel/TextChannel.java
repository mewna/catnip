package com.mewna.catnip.entity.channel;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author natanbc
 * @since 9/12/18
 */
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
    @CheckReturnValue
    default boolean isText() {
        return true;
    }
    
    @Override
    @CheckReturnValue
    default boolean isVoice() {
        return false;
    }
    
    @Override
    @CheckReturnValue
    default boolean isCategory() {
        return false;
    }
    
    @Nonnull
    @CheckReturnValue
    default CompletableFuture<List<Webhook>> fetchWebhooks() {
        return catnip().rest().webhook().getChannelWebhooks(id());
    }
}
