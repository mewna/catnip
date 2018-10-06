package com.mewna.catnip.entity.impl;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.RequiresCatnip;
import com.mewna.catnip.entity.message.BulkRemovedReactions;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * @author amy
 * @since 10/6/18.
 */
@Getter
@Setter
@Builder
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public class BulkRemovedReactionsImpl implements BulkRemovedReactions, RequiresCatnip {
    private transient Catnip catnip;
    
    private String channelId;
    private String messageId;
    private String guildId;
}
