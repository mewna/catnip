package com.mewna.catnip.entity;

import lombok.Getter;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

/**
 * @author natanbc
 * @since 9/12/18
 */
@SuppressWarnings("ClassReferencesSubclass")
public interface Channel extends Snowflake {
    @Nonnull
    @CheckReturnValue
    ChannelType type();
    
    @Nonnull
    default CompletableFuture<Channel> delete() {
        return catnip().rest().channel().deleteChannel(id());
    }
    
    @CheckReturnValue
    default boolean isText() {
        return type() == ChannelType.TEXT;
    }
    
    @CheckReturnValue
    default boolean isVoice() {
        return type() == ChannelType.VOICE;
    }
    
    @CheckReturnValue
    default boolean isCategory() {
        return type() == ChannelType.CATEGORY;
    }
    
    @CheckReturnValue
    default boolean isGuild() {
        return type().isGuild();
    }
    
    @CheckReturnValue
    default boolean isUserDM() {
        return type() == ChannelType.DM;
    }
    
    @CheckReturnValue
    default boolean isGroupDM() {
        return type() == ChannelType.GROUP_DM;
    }
    
    @CheckReturnValue
    default boolean isDM() {
        return !type().isGuild();
    }
    
    @Nonnull
    @CheckReturnValue
    default GuildChannel asGuildChannel() {
        if(!isGuild()) {
            throw new UnsupportedOperationException("Not a guild channel");
        }
        return (GuildChannel)this;
    }
    
    @Nonnull
    @CheckReturnValue
    default TextChannel asTextChannel() {
        if(!isText()) {
            throw new UnsupportedOperationException("Not a text channel");
        }
        return (TextChannel)this;
    }
    
    @Nonnull
    @CheckReturnValue
    default VoiceChannel asVoiceChannel() {
        if(!isVoice()) {
            throw new UnsupportedOperationException("Not a voice channel");
        }
        return (VoiceChannel)this;
    }
    
    @Nonnull
    @CheckReturnValue
    default Category asCategory() {
        if(!isCategory()) {
            throw new UnsupportedOperationException("Not a category");
        }
        return (Category)this;
    }
    
    @Nonnull
    @CheckReturnValue
    default DMChannel asDMChannel() {
        if(!isDM()) {
            throw new UnsupportedOperationException("Not a DM channel");
        }
        return (DMChannel)this;
    }
    
    @Nonnull
    @CheckReturnValue
    default UserDMChannel asUserDMChannel() {
        if(!isUserDM()) {
            throw new UnsupportedOperationException("Not an user DM channel");
        }
        return (UserDMChannel)this;
    }
    
    @Nonnull
    @CheckReturnValue
    default GroupDMChannel asGroupDMChannel() {
        if(!isGroupDM()) {
            throw new UnsupportedOperationException("Not a group DM channel");
        }
        return (GroupDMChannel)this;
    }
    
    enum ChannelType {
        TEXT(0, true), DM(1, false), VOICE(2, true), GROUP_DM(3, false), CATEGORY(4, true);
        
        @Getter
        private final int key;
        @Getter
        private final boolean guild;
        
        ChannelType(final int key, final boolean guild) {
            this.key = key;
            this.guild = guild;
        }
    
        @Nonnull
        public static ChannelType byKey(final int key) {
            for(final ChannelType level : values()) {
                if(level.key == key) {
                    return level;
                }
            }
            throw new IllegalArgumentException("No channel type for key " + key);
        }
    }
}
