package com.mewna.catnip.entity.impl;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.RequiresCatnip;
import com.mewna.catnip.entity.guild.GuildBan;
import com.mewna.catnip.entity.user.User;
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
public class GuildBanImpl implements GuildBan, RequiresCatnip {
    private transient Catnip catnip;
    
    private String guildId;
    
    private User user;
}
