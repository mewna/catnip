package com.mewna.catnip.entity.channel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mewna.catnip.entity.Snowflake;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.rest.RestRequester;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.concurrent.CompletionStage;

/**
 * A webhook on a channel. Allows sending messages to a text channel in a guild
 * without having to have a bot application.
 *
 * @author natanbc
 * @since 9/15/18
 */
@SuppressWarnings("unused")
public interface Webhook extends Snowflake {
    /**
     * @return The id of the guild this webhook is for.
     */
    @Nonnull
    @CheckReturnValue
    String guildId();
    
    /**
     * @return The id of the channel this webhook is for.
     */
    @Nonnull
    @CheckReturnValue
    String channelId();
    
    /**
     * @return The user that created this webhook.
     */
    @Nonnull
    @CheckReturnValue
    User user();
    
    /**
     * @return The name of this webhook.
     */
    @Nullable
    @CheckReturnValue
    String name();
    
    /**
     * @return The default avatar of the webhook.
     */
    @Nullable
    @CheckReturnValue
    String avatar();
    
    /**
     * @return The secure token of the webhook.
     */
    @Nonnull
    @CheckReturnValue
    String token();
    
    /**
     * @return The full URL of the webhook that can be used for requests.
     */
    @Nonnull
    @CheckReturnValue
    default String url() {
        return String.format("%s/webhooks/%s/%s", RestRequester.API_BASE, id(), token());
    }
    
    /**
     * Deletes the webhook.
     *
     * @return A CompletionStage that completes when the webhook is deleted.
     */
    @Nonnull
    @JsonIgnore
    @CheckReturnValue
    default CompletionStage<Void> delete() {
        return catnip().rest().webhook().deleteWebhook(id());
    }
    
    @SuppressWarnings("unused")
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
        public CompletionStage<Webhook> submit() {
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
