package com.mewna.catnip.entity.builder;

import com.mewna.catnip.entity.impl.PresenceImpl;
import com.mewna.catnip.entity.impl.PresenceImpl.ActivityImpl;
import com.mewna.catnip.entity.user.Presence;
import com.mewna.catnip.entity.user.Presence.Activity;
import com.mewna.catnip.entity.user.Presence.ActivityType;
import com.mewna.catnip.entity.user.Presence.OnlineStatus;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * @author SamOphis
 * @since 10/12/2018
 */
@Setter(onParam_ = @Nonnull, onMethod_ = {@CheckReturnValue, @Nonnull}) // doesn't work in my ide, but *should* work elsewhere
@NoArgsConstructor
@Accessors(fluent = true, chain = true)
@SuppressWarnings("unused")
public class PresenceBuilder {
    private OnlineStatus status;
    private ActivityType type;
    private String name;
    private String url;
    
    public PresenceBuilder(final Presence presence) {
        status = presence.status();
        final Activity activity = presence.activity();
        if (activity != null) {
            type = activity.type();
            name = activity.name();
            url = activity.url();
        }
        new PresenceBuilder().name(null);
    }
    
    public Presence build() {
        final Activity activity = name != null && type != null
                ? ActivityImpl.builder()
                    .name(name)
                    .type(type)
                    .url(url)
                    .build()
                : null;
        return PresenceImpl.builder()
                .status(status)
                .activity(activity)
                .build();
    }
}
