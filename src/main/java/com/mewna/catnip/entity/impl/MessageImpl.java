package com.mewna.catnip.entity.impl;

import com.mewna.catnip.entity.Member;
import com.mewna.catnip.entity.Message;
import com.mewna.catnip.entity.MessageType;
import com.mewna.catnip.entity.User;
import io.vertx.core.json.JsonObject;
import lombok.*;
import lombok.experimental.Accessors;

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
public class MessageImpl implements Message {
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
}
