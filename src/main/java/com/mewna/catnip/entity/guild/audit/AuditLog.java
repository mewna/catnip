package com.mewna.catnip.entity.guild.audit;

import com.mewna.catnip.entity.channel.Webhook;
import com.mewna.catnip.entity.user.User;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author SamOphis
 * @since 10/06/18
 */
public interface AuditLog {
    @CheckReturnValue
    @Nonnull
    List<Webhook> foundWebhooks();
    
    @CheckReturnValue
    @Nonnull
    List<User> foundUsers();
    
    @CheckReturnValue
    @Nonnull
    List<AuditLogEntry> auditLogEntries();
}
