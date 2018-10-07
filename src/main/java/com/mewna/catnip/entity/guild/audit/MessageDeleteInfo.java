package com.mewna.catnip.entity.guild.audit;

import javax.annotation.CheckReturnValue;

/**
 * @author SamOphis
 * @since 10/07/18
 */
@SuppressWarnings("unused")
public interface MessageDeleteInfo extends OptionalEntryInfo {
    @CheckReturnValue
    long channelId();
    
    @CheckReturnValue
    int deletedMessagesCount();
}
