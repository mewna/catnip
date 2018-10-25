package com.mewna.catnip.entity.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.RequiresCatnip;
import com.mewna.catnip.entity.channel.Webhook;
import com.mewna.catnip.entity.guild.audit.ActionType;
import com.mewna.catnip.entity.guild.audit.AuditLogChange;
import com.mewna.catnip.entity.guild.audit.AuditLogEntry;
import com.mewna.catnip.entity.guild.audit.OptionalEntryInfo;
import com.mewna.catnip.entity.user.User;
import lombok.*;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author SamOphis
 * @since 10/07/2018
 */
@Getter(onMethod_ = @JsonProperty)
@Setter(onMethod_ = @JsonProperty)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(fluent = true)
public class AuditLogEntryImpl implements AuditLogEntry, RequiresCatnip {
    @JsonIgnore
    private transient Catnip catnip;
    
    private String id;
    private String targetId;
    private User user;
    private String reason;
    private OptionalEntryInfo options;
    private ActionType type;
    private List<AuditLogChange> changes;
    private Webhook webhook;
    
    @Override
    public void catnip(@Nonnull final Catnip catnip) {
        this.catnip = catnip;
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return obj instanceof AuditLogEntryImpl && ((AuditLogEntryImpl) obj).id().equals(id);
    }
    
    @Override
    public String toString() {
        return String.format("Audit Log Entry (%s)", id);
    }
}
