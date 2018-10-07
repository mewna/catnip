package com.mewna.catnip.entity.guild.audit;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * @author SamOphis
 * @since 10/07/18
 */
@SuppressWarnings("unused")
public interface MessageDeleteInfo extends OptionalEntryInfo {
    @CheckReturnValue
    @Nonnull
    String channelId();
    
    @CheckReturnValue
    int deletedMessagesCount();
}
