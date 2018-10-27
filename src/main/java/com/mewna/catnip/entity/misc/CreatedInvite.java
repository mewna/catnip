package com.mewna.catnip.entity.misc;

import com.mewna.catnip.entity.guild.Invite;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.time.OffsetDateTime;

/**
 * @author natanbc
 * @since 9/14/18
 */
public interface CreatedInvite extends Invite {
    @Nonnegative
    int uses();
    
    @Nonnegative
    int maxUses();
    
    int maxAge();
    
    boolean temporary();
    
    @Nonnull
    OffsetDateTime createdAt();
    
    boolean revoked();
}
