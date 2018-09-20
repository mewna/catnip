package com.mewna.catnip.cache;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.*;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * If you're looking to implement your own caching system, you want
 * {@link EntityCacheWorker} instead.
 *
 * @author amy
 * @since 9/13/18.
 */
@SuppressWarnings("unused")
public interface EntityCache {
    /**
     * Get the guild with the specified ID. May be {@code null}.
     *
     * @param id The ID of the guild to fetch.
     *
     * @return The guild, or {@code null} if it isn't cached.
     */
    @Nullable
    Guild guild(@Nonnull String id);
    
    /**
     * Get the user with the specified ID. May be {@code null}.
     *
     * @param id The ID of the user to fetch.
     *
     * @return The user, or {@code null} if it isn't cached.
     */
    @Nullable
    User user(@Nonnull String id);
    
    /**
     * Get the member with the given ID from the guild with the given ID. May
     * be {@code null}.
     *
     * @param guildId The ID of the guild the desired member is in.
     * @param id      The ID of the desired member.
     *
     * @return The member, or {@code null} if it isn't cached.
     */
    @Nullable
    Member member(@Nonnull String guildId, @Nonnull String id);
    
    /**
     * Get all members for the guild with the given ID. The list returned by
     * this method will never be {@code null}, but may be empty.
     *
     * @param guildId The ID of the guild to fetch members for.
     *
     * @return A non-{@code null}, possibly-empty list of guild members.
     */
    @Nonnull
    List<Member> members(@Nonnull String guildId);
    
    /**
     * Get the role with the given ID from the guild with the given ID. May be
     * {@code null}.
     *
     * @param guildId The ID of the guild the desired role is from.
     * @param id The ID of the desired role.
     * @return The role, or {@code null} if it isn't cached.
     */
    @Nullable
    Role role(@Nonnull String guildId, @Nonnull String id);
    
    /**
     * Get all roles for the guild with the given ID. The list returned by this
     * method will never be {@code null}, but may be empty.
     *
     * @param guildId The ID of the guild to fetch roles for.
     *
     * @return A non-{@code null}, possibly-empty list of guild roles.
     */
    @Nonnull
    List<Role> roles(@Nonnull String guildId);
    
    /**
     * Get the channel with the given ID from the guild with the given ID. May
     * be {@code null}.
     *
     * @param guildId The ID of the guild the desired channel is from.
     * @param id The ID of the desired channel.
     * @return The channel, or {@code null} if it isn't cached.
     */
    @Nullable
    Channel channel(@Nonnull String guildId, @Nonnull String id);
    
    /**
     * Get all channels for the guild with the given ID. The list returned by
     * this method will never be {@code null}, but may be empty.
     *
     * @param guildId The ID of the guild to fetch channels for.
     *
     * @return A non-{@code null}, possibly-empty list of guild channels.
     */
    @Nonnull
    List<Channel> channels(@Nonnull String guildId);
}
