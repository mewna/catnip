package com.mewna.catnip.rest;

import com.mewna.catnip.internal.CatnipImpl;
import com.mewna.catnip.rest.type.RestChannel;
import com.mewna.catnip.rest.type.RestGuild;
import com.mewna.catnip.rest.type.RestUser;
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
    
    public Rest(final CatnipImpl catnip) {
        channel = new RestChannel(catnip);
        guild = new RestGuild(catnip);
        user = new RestUser(catnip);
    }
}
