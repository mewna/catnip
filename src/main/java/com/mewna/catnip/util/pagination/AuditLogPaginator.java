package com.mewna.catnip.util.pagination;

import com.mewna.catnip.entity.channel.Webhook;
import com.mewna.catnip.entity.guild.audit.ActionType;
import com.mewna.catnip.entity.guild.audit.AuditLogEntry;
import com.mewna.catnip.entity.impl.EntityBuilder;
import com.mewna.catnip.entity.user.User;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

/**
 * @author natanbc
 * @since 10/9/18.
 */
public abstract class AuditLogPaginator extends BasePaginator<AuditLogEntry, JsonObject, AuditLogPaginator> {
    private final EntityBuilder builder;
    private String userId;
    private ActionType type;
    
    public AuditLogPaginator(@Nonnull final EntityBuilder builder) {
        super(AuditLogEntry::id, 100);
        this.builder = builder;
    }
    
    /**
     * Filters from which user to fetch.
     *
     * @param userId Author of the wanted entries.
     *
     * @return {@code this}, for chaining calls.
     */
    public AuditLogPaginator user(@Nullable final String userId) {
        this.userId = userId;
        return this;
    }
    
    /**
     * Filters what type of entries to fetch.
     *
     * @param type Type of the wanted entries.
     *
     * @return {@code this}, for chaining calls.
     */
    public AuditLogPaginator type(@Nullable final ActionType type) {
        this.type = type;
        return this;
    }
    
    @Nonnull
    @Override
    protected CompletionStage<Void> fetch(@Nonnull final Consumer<AuditLogEntry> action) {
        return fetch(null, new RequestState<>(limit, requestSize, action)
                .extra("user", userId)
                .extra("type", type)
        );
    }
    
    @Override
    protected void update(@Nonnull final RequestState<AuditLogEntry> state, @Nonnull final JsonObject data) {
        //inlined EntityBuilder.immutableListOf and EntityBuilder.createAuditLog
        //this is done so we can do less allocations and only parse the entries
        //we need to.
        final Map<String, Webhook> webhooks = EntityBuilder.immutableMapOf(data.getJsonArray("webhooks"), x -> x.getString("id"), builder::createWebhook);
        final Map<String, User> users = EntityBuilder.immutableMapOf(data.getJsonArray("users"), x -> x.getString("id"), builder::createUser);
        final JsonArray entries = data.getJsonArray("audit_log_entries");
    
        for(final Object object : entries) {
            if(!(object instanceof JsonObject)) {
                throw new IllegalArgumentException("Expected all values to be JsonObjects, but found " +
                        (object == null ? "null" : object.getClass()));
            }
            final AuditLogEntry entry = builder.createAuditLogEntry((JsonObject)object, webhooks, users);
            state.update(entry);
            if(state.done()) {
                return;
            }
        }
    }
}
