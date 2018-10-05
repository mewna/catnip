package com.mewna.catnip.entity.impl;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.RequiresCatnip;
import com.mewna.catnip.entity.channel.Channel;
import com.mewna.catnip.entity.misc.Emoji.CustomEmoji;
import com.mewna.catnip.entity.guild.Guild;
import com.mewna.catnip.entity.guild.Member;
import com.mewna.catnip.entity.guild.Role;
import com.mewna.catnip.entity.util.ImageOptions;
import com.mewna.catnip.entity.util.Permission;
import com.mewna.catnip.util.CDNFormat;
import lombok.*;
import lombok.experimental.Accessors;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

/**
 * @author natanbc
 * @since 9/6/18.
 */
@Getter
@Setter
@Builder
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public class GuildImpl implements Guild, RequiresCatnip {
    private transient Catnip catnip;
    
    private String id;
    private String name;
    private String icon;
    private String splash;
    private boolean owned;
    private String ownerId;
    private Set<Permission> permissions;
    private String region;
    private String afkChannelId;
    private int afkTimeout;
    private boolean embedEnabled;
    private String embedChannelId;
    private VerificationLevel verificationLevel;
    private NotificationLevel defaultMessageNotifications;
    private ContentFilterLevel explicitContentFilter;
    private List<String> features;
    private MFALevel mfaLevel;
    private String applicationId;
    private boolean widgetEnabled;
    private String widgetChannelId;
    private String systemChannelId;
    private OffsetDateTime joinedAt;
    private boolean large;
    private boolean unavailable;
    private int memberCount;
    
    @Override
    public void catnip(@Nonnull final Catnip catnip) {
        this.catnip = catnip;
    }
    
    @Override
    @Nullable
    @CheckReturnValue
    public String iconUrl(@Nonnull final ImageOptions options) {
        return CDNFormat.iconUrl(id, icon, options);
    }
    
    @Override
    @Nullable
    @CheckReturnValue
    public String splashUrl(@Nonnull final ImageOptions options) {
        return CDNFormat.splashUrl(id, splash, options);
    }
    
    @Nonnull
    @Override
    public List<Role> roles() {
        return catnip.cache().roles(id);
    }
    
    @Nonnull
    @Override
    public List<Member> members() {
        return catnip.cache().members(id);
    }
    
    @Nonnull
    @Override
    public List<Channel> channels() {
        return catnip.cache().channels(id);
    }
    
    @Nonnull
    @Override
    public List<CustomEmoji> emojis() {
        return catnip.cache().emojis(id);
    }
}
