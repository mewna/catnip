package com.mewna.catnip.entity;

import com.mewna.catnip.rest.RestRequester;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

/**
 * @author natanbc
 * @since 9/15/18
 */
public interface Webhook extends Snowflake {
    @Nonnull
    @CheckReturnValue
    String guildId();
    
    @Nonnull
    @CheckReturnValue
    String channelId();
    
    @Nonnull
    @CheckReturnValue
    User user();
    
    @Nullable
    @CheckReturnValue
    String name();
    
    @Nullable
    @CheckReturnValue
    String avatar();
    
    @Nonnull
    @CheckReturnValue
    String token();
    
    @Nonnull
    @CheckReturnValue
    default String url() {
        return String.format("%s/webhooks/%s/%s", RestRequester.API_BASE, id(), token());
    }
    
    @Nonnull
    @CheckReturnValue
    default CompletableFuture<Void> delete() {
        return catnip().rest().webhook().deleteWebhook(id());
    }
}
