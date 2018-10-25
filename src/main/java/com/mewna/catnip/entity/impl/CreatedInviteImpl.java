package com.mewna.catnip.entity.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.RequiresCatnip;
import com.mewna.catnip.entity.misc.CreatedInvite;
import lombok.*;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import java.time.OffsetDateTime;

/**
 * @author natanbc
 * @since 9/14/18.
 */
@Getter(onMethod_ = @JsonProperty)
@Setter(onMethod_ = @JsonProperty)
@Builder
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public class CreatedInviteImpl implements CreatedInvite, RequiresCatnip {
    @JsonIgnore
    private transient Catnip catnip;
    
    private String code;
    private Inviter inviter;
    private InviteGuild guild;
    private InviteChannel channel;
    private int approximatePresenceCount;
    private int approximateMemberCount;
    private int uses;
    private int maxUses;
    private int maxAge;
    private boolean temporary;
    private OffsetDateTime createdAt;
    private boolean revoked;
    
    @Override
    public void catnip(@Nonnull final Catnip catnip) {
        this.catnip = catnip;
    }
}
