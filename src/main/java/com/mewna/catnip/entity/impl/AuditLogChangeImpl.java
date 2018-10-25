package com.mewna.catnip.entity.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.RequiresCatnip;
import com.mewna.catnip.entity.guild.audit.AuditLogChange;
import lombok.*;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;

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
public class AuditLogChangeImpl implements AuditLogChange, RequiresCatnip {
    @JsonIgnore
    private transient Catnip catnip;
    
    private Object oldValue;
    private Object newValue;
    private String key;
    
    @Override
    public void catnip(@Nonnull final Catnip catnip) {
        this.catnip = catnip;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T oldValue() {
        return (T) oldValue;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T newValue() {
        return (T) newValue;
    }
}
