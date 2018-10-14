package com.mewna.catnip.cache;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.misc.Emoji.CustomEmoji;
import com.mewna.catnip.entity.channel.GuildChannel;
import com.mewna.catnip.entity.guild.Member;
import com.mewna.catnip.entity.guild.Role;
import com.mewna.catnip.entity.user.Presence;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.entity.user.VoiceState;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;

/**
 * If you plan to write your own implementation of this class, be aware that
 * the contracts implied by the JSR-305 are *expected* to be followed, and you
 * *will* break things if you don't follow them.
 *
 * @author amy
 * @since 9/19/18.
 */
@SuppressWarnings("UnusedReturnValue")
public interface EntityCacheWorker extends EntityCache {
    /**
     * Update cache with a single gateway event.
     *
     * @param eventType Type of the event.
     * @param payload   Data payload contained in the event
     *
     * @return Itself.
     */
    @Nonnull
    @SuppressWarnings("UnusedReturnValue")
    EntityCache updateCache(@Nonnull String eventType, @Nonnull JsonObject payload);
    
    @Nonnull
    EntityCache bulkCacheUsers(@Nonnull Collection<User> users);
    
    @Nonnull
    EntityCache bulkCacheChannels(@Nonnull Collection<GuildChannel> channels);
    
    @Nonnull
    EntityCache bulkCacheRoles(@Nonnull Collection<Role> roles);
    
    @Nonnull
    EntityCache bulkCacheMembers(@Nonnull Collection<Member> members);
    
    @Nonnull
    EntityCache bulkCacheEmoji(@Nonnull Collection<CustomEmoji> emoji);
    
    @Nonnull
    EntityCache bulkCachePresences(@Nonnull Map<String, Presence> presences);
    
    @Nonnull
    EntityCache bulkCacheVoiceStates(@Nonnull Collection<VoiceState> voiceStates);

    @Nonnull
    EntityCache catnip(@Nonnull Catnip catnip);
}
