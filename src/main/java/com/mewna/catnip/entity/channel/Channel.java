package com.mewna.catnip.entity.channel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mewna.catnip.entity.Snowflake;
import lombok.Getter;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.concurrent.CompletionStage;

/**
 * A Discord channel. A channel may not be attached to a guild (ex. in the case
 * of DMs).
 *
 * @author natanbc
 * @since 9/12/18
 */
@SuppressWarnings({"ClassReferencesSubclass", "unused"})
public interface Channel extends Snowflake {
    /**
     * @return The type of this channel.
     */
    @Nonnull
    @CheckReturnValue
    ChannelType type();
    
    /**
     * Deletes the channel. This operation cannot be undone.
     *
     * @return A {@link CompletionStage} that is completed when the channel is
     * deleted.
     */
    @Nonnull
    default CompletionStage<Channel> delete() {
        return catnip().rest().channel().deleteChannel(id());
    }
    
    /**
     * @return Whether or not this channel is a text channel.
     */
    @JsonIgnore
    @CheckReturnValue
    default boolean isText() {
        return type() == ChannelType.TEXT;
    }
    
    /**
     * @return Whether or not this channel is a voice channel.
     */
    @JsonIgnore
    @CheckReturnValue
    default boolean isVoice() {
        return type() == ChannelType.VOICE;
    }
    
    /**
     * @return Whether or not this channel is a category.
     */
    @JsonIgnore
    @CheckReturnValue
    default boolean isCategory() {
        return type() == ChannelType.CATEGORY;
    }
    
    /**
     * @return Whether or not this channel is in a guild.
     */
    @JsonIgnore
    @CheckReturnValue
    default boolean isGuild() {
        return type().isGuild();
    }
    
    /**
     * @return Whether or not this channel is a DM with a single user.
     */
    @JsonIgnore
    @CheckReturnValue
    default boolean isUserDM() {
        return type() == ChannelType.DM;
    }
    
    /**
     * @return Whether or not this channel is a group DM with at least 1 other
     * user.
     */
    @JsonIgnore
    @CheckReturnValue
    default boolean isGroupDM() {
        return type() == ChannelType.GROUP_DM;
    }
    
    /**
     * @return Whether or not this channel is a DM; see {@link #isUserDM()} and
     * {@link #isGroupDM()} for more.
     */
    @JsonIgnore
    @CheckReturnValue
    default boolean isDM() {
        return !type().isGuild();
    }
    
    /**
     * @return This channel instance as a {@link GuildChannel}.
     */
    @Nonnull
    @JsonIgnore
    @CheckReturnValue
    default GuildChannel asGuildChannel() {
        if(!isGuild()) {
            throw new UnsupportedOperationException("Not a guild channel");
        }
        return (GuildChannel) this;
    }
    
    /**
     * @return This channel instance as a {@link TextChannel}.
     */
    @Nonnull
    @JsonIgnore
    @CheckReturnValue
    default TextChannel asTextChannel() {
        if(!isText()) {
            throw new UnsupportedOperationException("Not a text channel");
        }
        return (TextChannel) this;
    }
    
    /**
     * @return This channel instance as a {@link VoiceChannel}.
     */
    @Nonnull
    @JsonIgnore
    @CheckReturnValue
    default VoiceChannel asVoiceChannel() {
        if(!isVoice()) {
            throw new UnsupportedOperationException("Not a voice channel");
        }
        return (VoiceChannel) this;
    }
    
    /**
     * @return This channel instance as a {@link Category}.
     */
    @Nonnull
    @JsonIgnore
    @CheckReturnValue
    default Category asCategory() {
        if(!isCategory()) {
            throw new UnsupportedOperationException("Not a category");
        }
        return (Category) this;
    }
    
    /**
     * @return This channel instance as a {@link DMChannel}.
     */
    @Nonnull
    @JsonIgnore
    @CheckReturnValue
    default DMChannel asDMChannel() {
        if(!isDM()) {
            throw new UnsupportedOperationException("Not a DM channel");
        }
        return (DMChannel) this;
    }
    
    /**
     * @return This channel instance as a {@link UserDMChannel}.
     */
    @Nonnull
    @JsonIgnore
    @CheckReturnValue
    default UserDMChannel asUserDMChannel() {
        if(!isUserDM()) {
            throw new UnsupportedOperationException("Not an user DM channel");
        }
        return (UserDMChannel) this;
    }
    
    /**
     * @return This channel instance as a {@link GroupDMChannel}.
     */
    @Nonnull
    @JsonIgnore
    @CheckReturnValue
    default GroupDMChannel asGroupDMChannel() {
        if(!isGroupDM()) {
            throw new UnsupportedOperationException("Not a group DM channel");
        }
        return (GroupDMChannel) this;
    }
    
    /**
     * The type of a channel.
     */
    enum ChannelType {
        /**
         * A text channel in a guild.
         */
        TEXT(0, true),
        /**
         * A DM with a single user.
         */
        DM(1, false),
        /**
         * A voice channel in a guild.
         */
        VOICE(2, true),
        /**
         * A DM with multiple users.
         */
        GROUP_DM(3, false),
        /**
         * A guild channel category with zero or more child channels.
         */
        CATEGORY(4, true);
        
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
