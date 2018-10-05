package com.mewna.catnip.entity.message;

import com.mewna.catnip.entity.RequiresCatnip;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author amy
 * @since 10/4/18.
 */
public interface BulkDeletedMessages extends RequiresCatnip {
    @Nonnull
    List<String> ids();
    
    @Nonnull
    String channelId();
    
    @Nullable
    String guildId();
}
