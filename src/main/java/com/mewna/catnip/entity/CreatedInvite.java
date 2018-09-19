package com.mewna.catnip.entity;

import java.time.OffsetDateTime;

/**
 * @author natanbc
 * @since 9/14/18
 */
public interface CreatedInvite extends Invite {
    int uses();
    int maxUses();
    int maxAge();
    boolean temporary();
    OffsetDateTime createdAt();
    boolean revoked();
}
