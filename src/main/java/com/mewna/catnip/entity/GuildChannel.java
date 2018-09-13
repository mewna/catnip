package com.mewna.catnip.entity;

/**
 * @author natanbc
 * @since 9/12/18
 */
public interface GuildChannel extends Channel {
    String name();
    String guildId();
    int position();
    String parentId();
    
    @Override
    default boolean isDM() {
        return false;
    }
    
    @Override
    default boolean isGroupDM() {
        return false;
    }
    
    @Override
    default boolean isGuild() {
        return true;
    }
    
    @Override
    default GuildChannel asGuildChannel() {
        return this;
    }
}
