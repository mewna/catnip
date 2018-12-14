package com.mewna.catnip.entity.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.RequiresCatnip;
import com.mewna.catnip.entity.user.PresenceUpdate;
import lombok.*;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * @author natanbc
 * @since 12/14/18
 */
@Getter(onMethod_ = @JsonProperty)
@Setter(onMethod_ = @JsonProperty)
@Builder
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public class PresenceUpdateImpl implements PresenceUpdate, RequiresCatnip {
    @JsonIgnore
    private transient Catnip catnip;
    
    private OnlineStatus status;
    private Activity activity;
    private String id;
    private String guildId;
    private Set<String> roles;
    private String nick;
    private OnlineStatus mobileStatus;
    
    public void catnip(@Nonnull final Catnip catnip) {
        this.catnip = catnip;
    }
}
