package com.mewna.catnip.entity;

import com.mewna.catnip.entity.impl.RichEmbed;
import io.vertx.core.json.JsonObject;
import lombok.Getter;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * @author amy
 * @since 9/4/18.
 */
public interface Message {
    @Nonnull
    MessageType type();
    
    public enum MessageType {
        DEFAULT(0),
        RECIPIENT_ADD(1),
        RECIPIENT_REMOVE(2),
        CALL(3),
        CHANNEL_NAME_CHANGE(4),
        CHANNEL_ICON_CHANGE(5),
        CHANNEL_PINNED_MESSAGE(6),
        GUILD_MEMBER_JOIN(7),;
        @Getter
        private final int id;
        
        MessageType(final int id) {
            this.id = id;
        }
        
        @Nonnull
        @CheckReturnValue
        public static MessageType byId(final int id) {
            for(final MessageType m : values()) {
                if(m.id == id) {
                    return m;
                }
            }
            throw new IllegalArgumentException("No such MessageType: " + id);
        }
    }
    
    boolean tts();
    
    @Nonnull
    OffsetDateTime timestamp();
    
    boolean pinned();
    
    @Nullable
    String nonce();
    
    @Nonnull
    List<User> mentionedUsers();
    
    @Nonnull
    List<String> mentionedRoles();
    
    @Nullable
    Member member();
    
    @Nonnull
    String id();
    
    @Nonnull
    List<RichEmbed> embeds();
    
    @Nullable
    OffsetDateTime editedTimestamp();
    
    @Nonnull
    String content();
    
    @Nonnull
    String channelId();
    
    @Nonnull
    User author();
    
    @Nonnull
    List<JsonObject> attachments();
    
    @Nullable
    String guildId();
}
