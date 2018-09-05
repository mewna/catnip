package com.mewna.catnip.entity.impl;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.Member;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;
import java.util.Set;

/**
 * @author amy
 * @since 9/1/18.
 */
@Getter
@Setter
@Builder
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public class MemberImpl implements Member, RequiresCatnip {
    private transient Catnip catnip;
    
    private String id;
    private String nick;
    private Set<String> roles;
    private OffsetDateTime joinedAt;
    private boolean deaf;
    private boolean mute;
    
    @Override
    public void catnip(final Catnip catnip) {
        this.catnip = catnip;
    }
}
