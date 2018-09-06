package com.mewna.catnip.rest;

import com.mewna.catnip.internal.CatnipImpl;
import com.mewna.catnip.rest.handler.RestChannel;
import com.mewna.catnip.rest.handler.RestEmoji;
import com.mewna.catnip.rest.handler.RestGuild;
import com.mewna.catnip.rest.handler.RestUser;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * @author amy
 * @since 9/1/18.
 */
@Accessors(fluent = true)
@SuppressWarnings({"WeakerAccess", "unused"})
public class Rest {
    @Getter
    private final RestChannel channel;
    @Getter
    private final RestGuild guild;
    @Getter
    private final RestUser user;
    @Getter
    private final RestEmoji emoji;
    
    public Rest(final CatnipImpl catnip) {
        channel = new RestChannel(catnip);
        guild = new RestGuild(catnip);
        user = new RestUser(catnip);
        emoji = new RestEmoji(catnip);
    }
}
