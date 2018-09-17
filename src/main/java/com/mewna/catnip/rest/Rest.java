package com.mewna.catnip.rest;

import com.mewna.catnip.internal.CatnipImpl;
import com.mewna.catnip.rest.handler.*;
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
    @Getter
    private final RestInvite invite;
    @Getter
    private final RestVoice voice;
    @Getter
    private final RestWebhook webhook;
    
    public Rest(final CatnipImpl catnip) {
        channel = new RestChannel(catnip);
        guild = new RestGuild(catnip);
        user = new RestUser(catnip);
        emoji = new RestEmoji(catnip);
        invite = new RestInvite(catnip);
        voice = new RestVoice(catnip);
        webhook = new RestWebhook(catnip);
    }
}
