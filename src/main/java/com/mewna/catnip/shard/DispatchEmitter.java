package com.mewna.catnip.shard;

import com.google.common.collect.ImmutableList;
import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.Member;
import com.mewna.catnip.entity.Message;
import com.mewna.catnip.entity.Message.MessageType;
import com.mewna.catnip.entity.User;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.stream.Collectors;

import static com.mewna.catnip.shard.DiscordEvent.*;

/**
 * @author amy
 * @since 9/2/18.
 */
class DispatchEmitter {
    void emit(final JsonObject payload) {
        final String type = payload.getString("t");
        final JsonObject data = payload.getJsonObject("d");
        
        switch(type) {
            case MESSAGE_CREATE: {
                emitMessageCreate(data);
                break;
            }
            case GUILD_CREATE: {
                break;
            }
            case READY: {
                break;
            }
            case MESSAGE_UPDATE: {
                break;
            }
            case MESSAGE_DELETE: {
                break;
            }
            default: {
                break;
            }
        }
    }
    
    private void emitMessageCreate(final JsonObject data) {
        final List<User> mentionedUsers = data.getJsonArray("mentions").stream().filter(e -> e instanceof JsonObject)
                .map(e -> ((JsonObject) e).mapTo(User.class)).collect(Collectors.toList());
        
        final User author = data.getJsonObject("author").mapTo(User.class);
        final JsonObject memberObj = data.getJsonObject("member");
    
        final Member member = Member.builder()
                .id(author.getId())
                .deaf(memberObj.getBoolean("deaf"))
                .mute(memberObj.getBoolean("mute"))
                .nick(memberObj.getString("nick", null))
                .build();
    
        final Message message = Message.builder()
                .type(MessageType.byId(data.getInteger("type")))
                .tts(data.getBoolean("tts"))
                .timestamp(data.getString("timestamp"))
                .pinned(data.getBoolean("pinned"))
                .nonce(data.getString("nonce"))
                .mentionedUsers(mentionedUsers)
                .mentionedRoles(ImmutableList.of())
                .member(member)
                .id(data.getString("id"))
                .embeds(ImmutableList.of())
                .editedTimestamp(data.getString("edited_timestamp", null))
                .content(data.getString("content", null))
                .channelId(data.getString("channel_id"))
                .author(author)
                .attachments(ImmutableList.of())
                .guildId(data.getString("guild_id", null))
                .build();
        
        Catnip.eventBus().send(MESSAGE_CREATE, message);
    }
}
