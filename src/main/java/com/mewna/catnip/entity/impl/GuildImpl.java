package com.mewna.catnip.entity.impl;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.Channel;
import com.mewna.catnip.entity.Emoji.CustomEmoji;
import com.mewna.catnip.entity.Guild;
import com.mewna.catnip.entity.Member;
import com.mewna.catnip.entity.Role;
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
    private List<Role> roles;
    private List<CustomEmoji> emojis;
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
    private List<Member> members;
    private List<Channel> channels;
    
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
}
