package com.mewna.catnip.entity.guild;

import com.mewna.catnip.entity.Entity;
import com.mewna.catnip.entity.user.User;

/**
 * @author amy
 * @since 10/6/18.
 */
public interface GuildBan extends Entity {
    String guildId();
    
    User user();
}
