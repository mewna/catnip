package com.mewna.catnip.entity;

import com.mewna.catnip.entity.impl.RichEmbed;
import io.vertx.core.json.JsonObject;

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
