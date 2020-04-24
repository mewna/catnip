package com.mewna.catnip.entity.guild.audit;

import javax.annotation.CheckReturnValue;

/**
 * @author kjp12
 * @since March 18th, 2020
 */
public interface MemberDisconnectInfo extends OptionalEntryInfo {
    @CheckReturnValue
    int membersDisconnectedCount();
}
