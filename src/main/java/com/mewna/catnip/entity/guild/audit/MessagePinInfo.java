package com.mewna.catnip.entity.guild.audit;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * @author kjp12
 * @since March 18th, 2020
 */
public interface MessagePinInfo extends OptionalEntryInfo {
    @Nonnull
    @CheckReturnValue
    default String channelId() {
        return Long.toUnsignedString(channelIdAsLong());
    }
    
    @CheckReturnValue
    long channelIdAsLong();
    
    @Nonnull
    @CheckReturnValue
    default String messageId() {
        return Long.toUnsignedString(messageIdAsLong());
    }
    
    @CheckReturnValue
    long messageIdAsLong();
}
