package com.mewna.catnip.cache;

/**
 * Flags that control how catnip caches entities.
 *
 * @author amy
 * @since 9/21/18.
 */
public enum CacheFlag {
    /**
     * Don't cache any custom emojis from guilds.
     */
    DROP_EMOJI,
    /**
     * Don't cache user voice states. Don't set this flag if you need ex. to
     * know if a user is in a voice channel.
     */
    DROP_VOICE_STATES,
    /**
     * Don't cache any game statuses from presences. Set this cache flag if you
     * need to save memory.
     */
    DROP_GAME_STATUSES,
}
