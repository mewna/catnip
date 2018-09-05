package com.mewna.catnip.entity.impl;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.*;
import lombok.*;
import lombok.experimental.Accessors;

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
    private List<? extends Message.Attachment> attachments;
    private List<Embed> embeds;
    private List<? extends Message.Reaction> reactions;
    private String nonce;
    private boolean pinned;
    private String webhookId;
    private MessageType type;
    
    //not present in discord docs
    private Member member;
    private String guildId;
    
    @Override
    public void catnip(@Nonnull final Catnip catnip) {
        this.catnip = catnip;
    }
    
    @Override
    @Nonnull
    @SuppressWarnings("unchecked")
    public List<Message.Attachment> attachments() {
        return (List<Message.Attachment>)attachments;
    }
    
    @Override
    @Nonnull
    @SuppressWarnings("unchecked")
    public List<Message.Reaction> reactions() {
        return (List<Message.Reaction>)reactions;
    }
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Attachment implements Message.Attachment {
        private String id;
        private String fileName;
        private int size;
        private String url;
        private String proxyUrl;
        private int height;
        private int width;
    
        @Override
        public int hashCode() {
            return id.hashCode();
        }
    
        @Override
        public boolean equals(final Object obj) {
            return obj instanceof Message.Attachment && ((Message.Attachment)obj).id().equals(id);
        }
    
        @Override
        public String toString() {
            return String.format("Attachment (%s)", fileName);
        }
    }
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Reaction implements Message.Reaction {
        private int count;
        private boolean self;
        private Emoji emoji;
    
        @Override
        public int hashCode() {
            return emoji.hashCode();
        }
    
        @Override
        public boolean equals(final Object obj) {
            return obj instanceof Message.Reaction && ((Message.Reaction)obj).emoji().equals(emoji);
        }
    
        @Override
        public String toString() {
            return String.format("Reaction (%d x %s, self = %b)", count, emoji, self);
        }
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return obj instanceof Message && ((Message)obj).id().equals(id);
    }
    
    @Override
    public String toString() {
        return String.format("Message (%s)", content);
    }
}
