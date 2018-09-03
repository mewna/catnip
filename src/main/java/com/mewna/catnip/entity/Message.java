package com.mewna.catnip.entity;

import io.vertx.core.json.JsonObject;
import lombok.*;
import lombok.experimental.Accessors;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * @author amy
 * @since 9/2/18.
 */
@Getter
@Setter
@Builder
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private MessageType type;
    private boolean tts;
    private OffsetDateTime timestamp;
    private boolean pinned;
    private String nonce;
    private List<User> mentionedUsers;
    private List<String> mentionedRoles;
    private Member member;
    private String id;
    private List<Embed> embeds;
    private OffsetDateTime editedTimestamp;
    private String content;
    private String channelId;
    private User author;
    private List<JsonObject> attachments;
    private String guildId;
    
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
}
