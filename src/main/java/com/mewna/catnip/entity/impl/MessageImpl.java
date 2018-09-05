package com.mewna.catnip.entity.impl;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.*;
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
public class MessageImpl implements Message, RequiresCatnip {
    private transient Catnip catnip;
    
    private String id;
    private String channelId;
    private User author;
    private String content;
    private OffsetDateTime timestamp;
    private OffsetDateTime editedTimestamp;
    private boolean tts;
    private boolean mentionsEveryone;
    private List<User> mentionedUsers;
    private List<String> mentionedRoles;
    private List<JsonObject> attachments;
    private List<Embed> embeds;
    private String nonce;
    private boolean pinned;
    private MessageType type;
    
    //not present in discord docs
    private Member member;
    private String guildId;
    
    @Override
    public void catnip(final Catnip catnip) {
        this.catnip = catnip;
    }
}
