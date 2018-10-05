package com.mewna.catnip.rest.guild;

import com.mewna.catnip.entity.guild.Guild.ContentFilterLevel;
import com.mewna.catnip.entity.guild.Guild.NotificationLevel;
import com.mewna.catnip.entity.guild.Guild.VerificationLevel;
import com.mewna.catnip.util.JsonConvertible;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Accessors(fluent = true)
public class PartialGuild implements JsonConvertible {
    private final List<RoleData> roles = new ArrayList<>();
    private final List<ChannelData> channels = new ArrayList<>();
    @Getter
    /*@Setter already manually generated for length checks*/
    private String name;
    @Getter
    @Setter
    private String region;
    @Getter
    @Setter
    private String base64Icon;
    @Getter
    @Setter
    private VerificationLevel verificationLevel;
    @Getter
    @Setter
    private NotificationLevel defaultNotificationLevel;
    @Getter
    @Setter
    private ContentFilterLevel explicitContentFilter;
    
    public PartialGuild(@Nonnull final String name) {
        name(name);
        createRole(); //public role
    }
    
    @Nonnull
    @CheckReturnValue
    public static PartialGuild create(@Nonnull final String name) {
        return new PartialGuild(name);
    }
    
    @Nonnull
    public PartialGuild name(final String name) {
        final String trimmed = name.trim();
        if(trimmed.length() < 2 || trimmed.length() > 100) {
            throw new IllegalArgumentException("Name must have 2-100 characters");
        }
        this.name = trimmed;
        return this;
    }
    
    @Nonnull
    public RoleData createRole() {
        final RoleData role = RoleData.create(roles.size());
        roles.add(role);
        return role;
    }
    
    @Nonnull
    public PartialGuild createRole(@Nonnull final Consumer<RoleData> configurator) {
        configurator.accept(createRole());
        return this;
    }
    
    @Nonnull
    public RoleData getRole(@Nonnegative final int id) {
        return roles.get(id);
    }
    
    @Nonnull
    public PartialGuild configureRole(@Nonnegative final int id, @Nonnull final Consumer<RoleData> configurator) {
        configurator.accept(getRole(id));
        return this;
    }
    
    @Nonnull
    public RoleData getPublicRole() {
        return getRole(0);
    }
    
    @Nonnull
    public PartialGuild configurePublicRole(@Nonnull final Consumer<RoleData> configurator) {
        configurator.accept(getRole(0));
        return this;
    }
    
    @Nonnull
    public PartialGuild removeRole(@Nonnull final RoleData role) {
        if(role.publicRole()) {
            throw new IllegalArgumentException("Cannot remove public role");
        }
        roles.remove(role);
        return this;
    }
    
    @Nonnull
    public PartialGuild removeRole(@Nonnegative final int id) {
        if(id == 0) {
            throw new IllegalArgumentException("Cannot remove public role");
        }
        roles.remove(id);
        return this;
    }
    
    @Nonnull
    public PartialGuild addRole(@Nonnull final RoleData role) {
        roles.add(role);
        return this;
    }
    
    @Nonnull
    public ChannelData createTextChannel(@Nonnull final String name) {
        final ChannelData channel = ChannelData.createText(name);
        channels.add(channel);
        return channel;
    }
    
    @Nonnull
    public PartialGuild createTextChannel(@Nonnull final String name, @Nonnull final Consumer<ChannelData> configurator) {
        configurator.accept(createTextChannel(name));
        return this;
    }
    
    @Nonnull
    public ChannelData createVoiceChannel(@Nonnull final String name) {
        final ChannelData channel = ChannelData.createVoice(name);
        channels.add(channel);
        return channel;
    }
    
    @Nonnull
    public PartialGuild createVoiceChannel(@Nonnull final String name, @Nonnull final Consumer<ChannelData> configurator) {
        configurator.accept(createVoiceChannel(name));
        return this;
    }
    
    @Nonnull
    public ChannelData getChannel(@Nonnegative final int position) {
        return channels.get(position);
    }
    
    @Nonnull
    public PartialGuild removeChannel(@Nonnull final ChannelData channel) {
        channels.remove(channel);
        return this;
    }
    
    @Nonnull
    public PartialGuild removeChannel(@Nonnegative final int position) {
        channels.remove(position);
        return this;
    }
    
    @Nonnull
    public PartialGuild addChannel(@Nonnull final ChannelData channel) {
        channels.add(channel);
        return this;
    }
    
    @Override
    @Nonnull
    @CheckReturnValue
    public JsonObject toJson() {
        final JsonObject object = new JsonObject().put("name", name);
        if(!channels.isEmpty()) {
            final JsonArray array = new JsonArray();
            for(final ChannelData data : channels) {
                array.add(data.toJson());
            }
            object.put("channels", array);
        }
        if(!roles.isEmpty()) {
            final JsonArray array = new JsonArray();
            for(final RoleData data : roles) {
                array.add(data.toJson());
            }
            object.put("roles", array);
        }
        if(region != null) {
            object.put("region", region);
        }
        if(base64Icon != null) {
            object.put("icon", base64Icon);
        }
        if(verificationLevel != null) {
            object.put("verification_level", verificationLevel.getKey());
        }
        if(defaultNotificationLevel != null) {
            object.put("default_message_notifications", defaultNotificationLevel.getKey());
        }
        if(explicitContentFilter != null) {
            object.put("explicit_content_filter", explicitContentFilter.getKey());
        }
        return object;
    }
}
