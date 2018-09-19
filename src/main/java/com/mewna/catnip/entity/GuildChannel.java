package com.mewna.catnip.entity;

import com.mewna.catnip.entity.PermissionOverride.OverrideType;
import com.mewna.catnip.rest.guild.PermissionOverrideData;
import com.mewna.catnip.rest.invite.InviteCreateOptions;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * @author natanbc
 * @since 9/12/18
 */
public interface GuildChannel extends Channel {
    @Nonnull
    @CheckReturnValue
    String name();
    
    @Nonnull
    @CheckReturnValue
    String guildId();
    
    @CheckReturnValue
    int position();
    
    @Nullable
    @CheckReturnValue
    String parentId();
    
    @Nonnull
    @CheckReturnValue
    List<PermissionOverride> overrides();
    
    @Override
    @CheckReturnValue
    default boolean isDM() {
        return false;
    }
    
    @Override
    @CheckReturnValue
    default boolean isGroupDM() {
        return false;
    }
    
    @Override
    @CheckReturnValue
    default boolean isGuild() {
        return true;
    }
    
    @Nonnull
    @CheckReturnValue
    default CompletableFuture<CreatedInvite> createInvite(@Nullable final InviteCreateOptions options) {
        return catnip().rest().channel().createInvite(id(), options);
    }
    
    @Nonnull
    @CheckReturnValue
    default CompletableFuture<CreatedInvite> createInvite() {
        return createInvite(null);
    }
    
    @Nonnull
    @CheckReturnValue
    default CompletableFuture<List<CreatedInvite>> fetchInvites() {
        return catnip().rest().channel().getChannelInvites(id());
    }
    
    @Getter
    @Setter
    @Accessors(fluent = true)
    class ChannelEditFields {
        private final GuildChannel channel;
        
        public ChannelEditFields(@Nullable final GuildChannel channel) {
            this.channel = channel;
        }
        
        public ChannelEditFields() {
            this(null);
        }
        
        private String name;
        private Integer position;
        private String topic;
        private Boolean nsfw;
        private Integer bitrate;
        private Integer userLimit;
        private Map<String, PermissionOverrideData> overrides = new HashMap<>();
        private String parentId;
        private Integer rateLimitPerUser;
    
        @Nonnull
        @CheckReturnValue
        public PermissionOverrideData override(@Nonnull final String id, @Nonnull final OverrideType type) {
            return overrides.computeIfAbsent(id, __ -> new PermissionOverrideData(type, id));
        }
    
        @Nonnull
        @CheckReturnValue
        public PermissionOverrideData memberOverride(@Nonnull final String id) {
            return override(id, OverrideType.MEMBER);
        }
    
        @Nonnull
        @CheckReturnValue
        public PermissionOverrideData roleOverride(@Nonnull final String id) {
            return override(id, OverrideType.ROLE);
        }
    
        @Nonnull
        public ChannelEditFields override(@Nonnull final String id, @Nonnull final OverrideType type, @Nonnull final Consumer<PermissionOverrideData> configurator) {
            configurator.accept(override(id, type));
            return this;
        }
    
        @Nonnull
        public ChannelEditFields memberOverride(@Nonnull final String id, @Nonnull final Consumer<PermissionOverrideData> configurator) {
            return override(id, OverrideType.MEMBER, configurator);
        }
    
        @Nonnull
        public ChannelEditFields roleOverride(@Nonnull final String id, @Nonnull final Consumer<PermissionOverrideData> configurator) {
            return override(id, OverrideType.ROLE, configurator);
        }
    
        @Nonnull
        public CompletableFuture<GuildChannel> submit() {
            if(channel == null) {
                throw new IllegalStateException("Cannot submit edit without a channel object! Please use RestChannel directly instead");
            }
            return channel.catnip().rest().channel().modifyChannel(channel.id(), this);
        }
        
        @Nonnull
        @CheckReturnValue
        public JsonObject payload() {
            final JsonObject payload = new JsonObject();
            if(name != null && (channel == null || !Objects.equals(name, channel.name()))) {
                payload.put("name", name);
            }
            if(position != null && (channel == null || !Objects.equals(position, channel.position()))) {
                payload.put("position", position);
            }
            if(parentId != null && (channel == null || !Objects.equals(parentId, channel.parentId()))) {
                payload.put("parent_id", parentId);
            }
            if(overrides != null && !overrides.isEmpty()) {
                final Map<String, PermissionOverrideData> finalOverrides = new HashMap<>();
                if(channel != null) {
                    channel.overrides().forEach(override -> {
                        finalOverrides.put(override.id(), PermissionOverrideData.create(override));
                    });
                }
                overrides.forEach(finalOverrides::put);
                final JsonObject object = new JsonObject();
                finalOverrides.forEach((k, v) -> {
                    object.put(k, v.toJson());
                });
                payload.put("permission_overwrites", object);
            }
            if(channel != null) {
                //TODO: throw if fields set on an unsupported channel type? (eg nsfw on voice channel)
                if(channel.isText()) {
                    final TextChannel text = channel.asTextChannel();
                    if(topic != null && !Objects.equals(topic, text.topic())) {
                        payload.put("topic", topic);
                    }
                    if(nsfw != null && !Objects.equals(nsfw, text.nsfw())) {
                        payload.put("nsfw", nsfw);
                    }
                    if(rateLimitPerUser != null && !Objects.equals(rateLimitPerUser, text.rateLimitPerUser())) {
                        payload.put("rate_limit_per_user", rateLimitPerUser);
                    }
                } else if(channel.isVoice()) {
                    final VoiceChannel voice = channel.asVoiceChannel();
                    if(bitrate != null && !Objects.equals(bitrate, voice.bitrate())) {
                        payload.put("bitrate", bitrate);
                    }
                    if(userLimit != null && !Objects.equals(userLimit, voice.userLimit())) {
                        payload.put("user_limit", userLimit);
                    }
                }
            } else {
                if(topic != null) {
                    payload.put("topic", topic);
                }
                if(nsfw != null) {
                    payload.put("nsfw", nsfw);
                }
                if(bitrate != null) {
                    payload.put("bitrate", bitrate);
                }
                if(userLimit != null) {
                    payload.put("user_limit", userLimit);
                }
                if(rateLimitPerUser != null) {
                    payload.put("rate_limit_per_user", rateLimitPerUser);
                }
            }
            return payload;
        }
    }
}
