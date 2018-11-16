package com.mewna.catnip.entity.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.RequiresCatnip;
import com.mewna.catnip.entity.Timestamped;
import com.mewna.catnip.entity.guild.Member;
import lombok.*;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import java.time.OffsetDateTime;
import java.util.Set;

/**
 * @author amy
 * @since 9/1/18.
 */
@Getter(onMethod_ = @JsonProperty)
@Setter(onMethod_ = @JsonProperty)
@Builder
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public class MemberImpl implements Member, RequiresCatnip, Timestamped {
    @JsonIgnore
    private transient Catnip catnip;
    
    private String id;
    private String guildId;
    private String nick;
    private Set<String> roleIds;
    private String joinedAt;
    private boolean deaf;
    private boolean mute;
    
    @Nonnull
    @Override
    public OffsetDateTime joinedAt() {
        return parseTimestamp(joinedAt);
    }
    
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
        return obj instanceof Member && ((Member)obj).id().equals(id);
    }
    
    @Override
    public String toString() {
        return String.format("Member (%s, %s)", id, nick);
    }
}
