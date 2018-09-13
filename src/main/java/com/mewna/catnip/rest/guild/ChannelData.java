package com.mewna.catnip.rest.guild;

import com.mewna.catnip.util.JsonConvertible;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

@Accessors(fluent = true)
@Getter
@Setter
public abstract class ChannelData implements JsonConvertible {
    private final int type;
    private final String name;
    private Integer position;
    private String topic;
    private Boolean nsfw;
    private Integer bitrate;
    private Integer userLimit;
    
    ChannelData(@Nonnegative final int type, @Nonnull final String name) {
        this.type = type;
        this.name = name;
        if(type != 0 && type != 2) {
            throw new IllegalArgumentException("Type must be either 0 (text) or 2 (voice)");
        }
        if(name.length() < 2 || name.length() > 100) {
            throw new IllegalArgumentException("Name must have 2-100 characters");
        }
    }
    
    @Nonnull
    @CheckReturnValue
    public static ChannelData createText(@Nonnull final String name) {
        return new TextChannelData(name.trim());
    }
    
    @Nonnull
    @CheckReturnValue
    public static ChannelData createVoice(@Nonnull final String name) {
        return new VoiceChannelData(name.trim());
    }
    
    @Override
    @Nonnull
    @CheckReturnValue
    public JsonObject toJson() {
        final JsonObject object = new JsonObject()
                .put("name", name)
                .put("type", type);
        if(position != null) {
            object.put("position", position);
        }
        if(topic != null) {
            object.put("topic", topic);
        }
        if(nsfw != null) {
            object.put("nsfw", nsfw);
        }
        if(bitrate != null) {
            object.put("bitrate", bitrate);
        }
        if(userLimit != null) {
            object.put("user_limit", userLimit);
        }
        
        return object;
    }
    
    private static class TextChannelData extends ChannelData {
        TextChannelData(final String name) {
            super(0, name);
        }
    
        @Override
        public ChannelData bitrate(final Integer bitrate) {
            throw new IllegalStateException("Cannot set bitrate on text channels");
        }
    
        @Override
        public ChannelData userLimit(final Integer userLimit) {
            throw new IllegalStateException("Cannot set user limit on text channels");
        }
    }
    
    private static class VoiceChannelData extends ChannelData {
        VoiceChannelData(final String name) {
            super(2, name);
        }
    
        @Override
        public ChannelData topic(final String topic) {
            throw new IllegalStateException("Cannot set topic on voice channels");
        }
    
        @Override
        public Boolean nsfw() {
            throw new IllegalStateException("Cannot set nsfw on voice channels");
        }
    }
}
