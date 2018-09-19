package com.mewna.catnip.entity;

import com.mewna.catnip.rest.RestRequester;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
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
    
    @Getter
    @Setter
    @Accessors(fluent = true)
    class WebhookEditFields {
        private final Webhook webhook;
        private String name;
        private String avatar;
        private String channelId;
        
        public WebhookEditFields(@Nullable final Webhook webhook) {
            this.webhook = webhook;
        }
    
        public WebhookEditFields() {
            this(null);
        }
    
        @Nonnull
        public CompletableFuture<Webhook> submit() {
            if(webhook == null) {
                throw new IllegalStateException("Cannot submit edit without a webhook object! Please use RestWebhook directly instead");
            }
            return webhook.catnip().rest().webhook().modifyWebhook(webhook.id(), this);
        }
    
        @Nonnull
        @CheckReturnValue
        public JsonObject payload() {
            final JsonObject payload = new JsonObject();
            if(name != null && (webhook == null || !Objects.equals(name, webhook.name()))) {
                payload.put("name", name);
            }
            if(avatar != null && (webhook == null || !Objects.equals(avatar, webhook.avatar()))) {
                payload.put("avatar", avatar);
            }
            if(channelId != null && (webhook == null || !Objects.equals(channelId, webhook.channelId()))) {
                payload.put("channel_id", channelId);
            }
            return payload;
        }
    }
}
