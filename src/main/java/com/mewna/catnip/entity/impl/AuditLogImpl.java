package com.mewna.catnip.entity.impl;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.channel.Webhook;
import com.mewna.catnip.entity.guild.audit.AuditLog;
import com.mewna.catnip.entity.guild.audit.AuditLogEntry;
import com.mewna.catnip.entity.user.User;
import lombok.*;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(fluent = true)
public class AuditLogImpl implements AuditLog {
    private transient Catnip catnip;
    
    private List<Webhook> foundWebhooks;
    private List<User> foundUsers;
    private List<AuditLogEntry> auditLogEntries;
    
    @Override
    public void catnip(@Nonnull final Catnip catnip) {
        this.catnip = catnip;
    }
}
