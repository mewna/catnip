package com.mewna.catnip.entity.impl;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.RequiresCatnip;
import com.mewna.catnip.entity.user.TypingUser;
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
public class TypingUserImpl implements TypingUser, RequiresCatnip {
    private transient Catnip catnip;
    
    private String id;
    private String channelId;
    private String guildId;
    private long timestamp;
}
