package com.mewna.catnip.entity.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.RequiresCatnip;
import com.mewna.catnip.entity.misc.ApplicationInfo;
import com.mewna.catnip.entity.user.User;
import lombok.*;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author amy
 * @since 10/17/18.
 */
@Getter(onMethod_ = @JsonProperty)
@Setter(onMethod_ = @JsonProperty)
@Builder
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationInfoImpl implements ApplicationInfo, RequiresCatnip {
    @JsonIgnore
    private transient Catnip catnip;
    
    private String id;
    private String icon;
    private String name;
    private String description;
    private List<String> rpcOrigins;
    private boolean publicBot;
    private boolean requiresCodeGrant;
    private User owner;
    
    @Override
    public void catnip(@Nonnull final Catnip catnip) {
        this.catnip = catnip;
    }
}
