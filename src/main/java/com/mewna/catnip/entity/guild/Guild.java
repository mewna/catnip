/*
 * Copyright (c) 2018 amy, All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.mewna.catnip.entity.guild;

import com.grack.nanojson.JsonObject;
import com.mewna.catnip.cache.view.CacheView;
import com.mewna.catnip.cache.view.NamedCacheView;
import com.mewna.catnip.entity.channel.*;
import com.mewna.catnip.entity.misc.CreatedInvite;
import com.mewna.catnip.entity.misc.Emoji.CustomEmoji;
import com.mewna.catnip.entity.partials.*;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.entity.user.VoiceState;
import com.mewna.catnip.entity.util.ImageOptions;
import com.mewna.catnip.entity.util.Permission;
import com.mewna.catnip.rest.guild.ChannelData;
import com.mewna.catnip.rest.guild.MemberData;
import com.mewna.catnip.rest.guild.PositionUpdater;
import com.mewna.catnip.rest.guild.RoleData;
import com.mewna.catnip.util.PermissionUtil;
import com.mewna.catnip.util.Utils;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a Discord guild. A guild is colloquially referred to as a server,
 * both in the client UI and by users.
 *
 * @author natanbc
 * @since 9/6/18
 */
@SuppressWarnings({"unused", "RedundantSuppression"})
public interface Guild extends Snowflake, Nameable, NullDescribable, HasIcon, HasApplication {
    int NICKNAME_MAX_LENGTH = 32;
    
    /**
     * @return The splash image for the guild. May be {@code null}.
     */
    @Nullable
    @CheckReturnValue
    String splash();
    
    /**
     * The CDN URL of the guild's splash image.
     *
     * @param options The options to configure the URL returned.
     *
     * @return The CDN URL.
     */
    @Nullable
    @CheckReturnValue
    String splashUrl(@Nonnull ImageOptions options);
    
    /**
     * @return The CDN URL of the guild's splash image.
     */
    @Nullable
    @CheckReturnValue
    default String splashUrl() {
        return splashUrl(new ImageOptions());
    }
    
    /**
     * @return The self member as an user of the guild.
     */
    @Nonnull
    @CheckReturnValue
    default Maybe<Member> selfMember() {
        return catnip().selfUser().flatMap(user -> catnip().cache().member(idAsLong(),
                Objects.requireNonNull(user, "Self user is null. This shouldn't ever happen").idAsLong()));
    }
    
    /**
     * @return The guild owner of the guild.
     */
    @Nonnull
    @CheckReturnValue
    default Maybe<Member> owner() {
        return catnip().cache().member(id(), ownerId());
    }
    
    /**
     * @return Whether the guild is owned by the current user.
     */
    @CheckReturnValue
    boolean owned();
    
    /**
     * @return The id of the user who owns the guild.
     */
    @Nonnull
    @CheckReturnValue
    default String ownerId() {
        return Long.toUnsignedString(ownerIdAsLong());
    }
    
    /**
     * @return The id of the user who owns the guild.
     */
    @CheckReturnValue
    long ownerIdAsLong();
    
    /**
     * @return Total permissions for the user in the guild. Does NOT include
     * channel overrides.
     */
    @Nonnull
    @CheckReturnValue
    Set<Permission> permissions();
    
    /**
     * @return The region that the guild's voice servers are located in.
     */
    @Nonnull
    @CheckReturnValue
    String region();
    
    /**
     * @return The id of the afk voice channel for the guild.
     */
    @Nullable
    @CheckReturnValue
    default String afkChannelId() {
        final long id = afkChannelIdAsLong();
        if(id == 0) {
            return null;
        }
        return Long.toUnsignedString(id);
    }
    
    /**
     * @return The id of the afk voice channel for the guild.
     */
    @CheckReturnValue
    long afkChannelIdAsLong();
    
    /**
     * @return The amount of time a user must be afk for before they're moved
     * to the afk channel.
     */
    @CheckReturnValue
    int afkTimeout();
    
    /**
     * @return The verification level set for the guild.
     */
    @Nonnull
    @CheckReturnValue
    VerificationLevel verificationLevel();
    
    /**
     * @return The notification level set for the guild.
     */
    @Nonnull
    @CheckReturnValue
    NotificationLevel defaultMessageNotifications();
    
    /**
     * @return The explicit content filter level set for the guild.
     */
    @Nonnull
    @CheckReturnValue
    ContentFilterLevel explicitContentFilter();
    
    /**
     * @return The list of features enabled for the guild.
     */
    @Nonnull
    @CheckReturnValue
    List<GuildFeature> features();
    
    /**
     * @return The MFA level set for guild administrators.
     */
    @Nonnull
    @CheckReturnValue
    MFALevel mfaLevel();
    
    /**
     * @return Whether or not the guild's widget is enabled.
     */
    @CheckReturnValue
    boolean widgetEnabled();
    
    /**
     * @return The channel the guild's widget is set for, if enabled.
     */
    @Nullable
    @CheckReturnValue
    default String widgetChannelId() {
        final long id = widgetChannelIdAsLong();
        if(id == 0) {
            return null;
        }
        return Long.toUnsignedString(id);
    }
    
    /**
     * @return The channel the guild's widget is set for, if enabled.
     */
    @CheckReturnValue
    long widgetChannelIdAsLong();
    
    /**
     * @return The id of the channel used for system messages (ex. the built-in
     * member join messages).
     */
    @Nullable
    @CheckReturnValue
    default String systemChannelId() {
        final long id = systemChannelIdAsLong();
        if(id == 0) {
            return null;
        }
        return Long.toUnsignedString(id);
    }
    
    /**
     * @return The id of the channel used for system messages (ex. the built-in
     * member join messages).
     */
    @CheckReturnValue
    long systemChannelIdAsLong();
    
    //The following fields are only sent in GUILD_CREATE
    
    /**
     * @return The date/time that the current user joined the guild at.
     */
    @CheckReturnValue
    OffsetDateTime joinedAt();
    
    /**
     * @return Whether or not this guild is considered "large."
     */
    @CheckReturnValue
    boolean large();
    
    /**
     * @return The maximum number of presences the guild can have. Will be
     * {@code 0} if no value was present.
     */
    @Nonnegative
    @CheckReturnValue
    int maxPresences();
    
    /**
     * @return The maximum number of members the guild can have. Will be
     * {@code 0} if no value was present.
     */
    @Nonnegative
    @CheckReturnValue
    int maxMembers();
    
    /**
     * @return The approximate member count the guild currently has. Will be
     * {@code 0} if no value was present.
     */
    @Nonnegative
    @CheckReturnValue
    int approximateMemberCount();
    
    /**
     * @return The approximate presence count the guild currently has. Will be
     * {@code 0} if no value was present.
     */
    @Nonnegative
    @CheckReturnValue
    int approximatePresenceCount();
    
    /**
     * @return The vanity invite code for this guild, ie.
     * {@code discord.gg/vanity_code}.
     */
    @Nullable
    @CheckReturnValue
    String vanityUrlCode();
    
    /**
     * @return The guild's banner hash.
     *
     * @apiNote See https://discord.com/developers/docs/reference#image-formatting "Guild Banner"
     */
    @Nullable
    @CheckReturnValue
    String banner();
    
    /**
     * @return The guild's Nitro Boost tier.
     *
     * @apiNote See https://support.discord.com/hc/en-us/articles/360028038352-Server-Boosting-
     */
    @Nonnull
    @CheckReturnValue
    PremiumTier premiumTier();
    
    /**
     * @return The number of members providing Nitro Boosts to this guild.
     *
     * @apiNote See https://support.discord.com/hc/en-us/articles/360028038352-Server-Boosting-
     */
    @Nonnegative
    @CheckReturnValue
    int premiumSubscriptionCount();
    
    /**
     * @return The preferred locale for this guild. Defaults to {@code en-US}.
     * Is only modifiable on guilds that have {@link GuildFeature#DISCOVERABLE}
     * set.
     */
    @Nonnull
    @CheckReturnValue
    String preferredLocale();
    
    /**
     * @return Whether or not this guild is currently unavailable.
     */
    @CheckReturnValue
    default boolean unavailable() {
        return catnip().isUnavailable(id());
    }
    
    /**
     * @return The number of users in this guild.
     */
    @Nonnegative
    @CheckReturnValue
    default long memberCount() {
        return members().size();
    }
    
    /**
     * @return All roles in this guild.
     */
    @Nonnull
    @CheckReturnValue
    default NamedCacheView<Role> roles() {
        return catnip().cache().roles(id());
    }
    
    /**
     * @return All custom emoji in this guild.
     */
    @Nonnull
    @CheckReturnValue
    default NamedCacheView<CustomEmoji> emojis() {
        return catnip().cache().emojis(id());
    }
    
    /**
     * @return All members in this guild.
     */
    @Nonnull
    @CheckReturnValue
    default NamedCacheView<Member> members() {
        return catnip().cache().members(id());
    }
    
    /**
     * @return All channels in this guild.
     */
    @Nonnull
    @CheckReturnValue
    default NamedCacheView<GuildChannel> channels() {
        return catnip().cache().channels(id());
    }
    
    /**
     * @return All voice states for this guild.
     */
    @Nonnull
    @CheckReturnValue
    default CacheView<VoiceState> voiceStates() {
        return catnip().cache().voiceStates(id());
    }
    
    // Convenience methods
    
    /**
     * @param id The id of the member to get.
     *
     * @return The member object for the user with the given id. May be
     * {@code null} if the user is not a member of the guild.
     */
    @Nonnull
    @CheckReturnValue
    default Maybe<Member> member(final String id) {
        return catnip().cache().member(id(), id);
    }
    
    /**
     * @param id The id of the member to get.
     *
     * @return The member object for the user with the given id. May be
     * {@code null} if the user is not a member of the guild.
     */
    @Nonnull
    @CheckReturnValue
    default Maybe<Member> member(final long id) {
        return catnip().cache().member(idAsLong(), id);
    }
    
    /**
     * @param id The id of the role to get.
     *
     * @return The role object with the given id, or {@code null} if no such
     * role exists.
     */
    @Nonnull
    @CheckReturnValue
    default Maybe<Role> role(final String id) {
        return catnip().cache().role(id(), id);
    }
    
    /**
     * @param id The id of the role to get.
     *
     * @return The role object with the given id, or {@code null} if no such
     * role exists.
     */
    @Nonnull
    @CheckReturnValue
    default Maybe<Role> role(final long id) {
        return catnip().cache().role(idAsLong(), id);
    }
    
    /**
     * @param id The id of the channel to get.
     *
     * @return The channel, or {@code null} if it isn't present.
     */
    @Nonnull
    @CheckReturnValue
    default Maybe<GuildChannel> channel(final String id) {
        return catnip().cache().channel(id(), id);
    }
    
    /**
     * @param id The id of the channel to get.
     *
     * @return The channel, or {@code null} if it isn't present.
     */
    @Nonnull
    @CheckReturnValue
    default GuildChannel channel(final long id) {
        return channels().getById(id);
    }
    
    /**
     * @param id The id of the channel to get.
     *
     * @return The channel, or {@code null} if it isn't present.
     *
     * @throws IllegalArgumentException If the channel is not a text channel.
     */
    @Nonnull
    @CheckReturnValue
    default Maybe<TextChannel> textChannel(final String id) {
        return catnip().cache().channel(id(), id).map(Channel::asTextChannel);
    }
    
    /**
     * @param id The id of the channel to get.
     *
     * @return The channel, or {@code null} if it isn't present.
     *
     * @throws IllegalArgumentException If the channel is not a text channel.
     */
    @Nonnull
    @CheckReturnValue
    default Maybe<TextChannel> textChannel(final long id) {
        return catnip().cache().channel(idAsLong(), id).map(Channel::asTextChannel);
    }
    
    /**
     * @param id The id of the channel to get.
     *
     * @return The channel, or {@code null} if it isn't present.
     *
     * @throws IllegalArgumentException If the channel is not a voice channel.
     */
    @Nonnull
    @CheckReturnValue
    default Maybe<VoiceChannel> voiceChannel(final String id) {
        return catnip().cache().channel(id(), id).map(Channel::asVoiceChannel);
    }
    
    /**
     * @param id The id of the channel to get.
     *
     * @return The channel, or {@code null} if it isn't present.
     *
     * @throws IllegalArgumentException If the channel is not a voice channel.
     */
    @Nonnull
    @CheckReturnValue
    default Maybe<VoiceChannel> voiceChannel(final long id) {
        return catnip().cache().channel(idAsLong(), id).map(Channel::asVoiceChannel);
    }
    
    /**
     * @param id The id of the channel to get.
     *
     * @return The channel, or {@code null} if it isn't present.
     *
     * @throws IllegalArgumentException If the channel is not a category.
     */
    @Nonnull
    @CheckReturnValue
    default Maybe<Category> category(final String id) {
        return catnip().cache().channel(id(), id).map(Channel::asCategory);
    }
    
    /**
     * @param id The id of the channel to get.
     *
     * @return The channel, or {@code null} if it isn't present.
     *
     * @throws IllegalArgumentException If the channel is not a category.
     */
    @Nonnull
    @CheckReturnValue
    default Maybe<Category> category(final long id) {
        return catnip().cache().channel(idAsLong(), id).map(Channel::asCategory);
    }
    
    /**
     * @param id The id of the emoji to fetch.
     *
     * @return The emoji, or {@code null} if it isn't present.
     */
    @Nonnull
    @CheckReturnValue
    default Maybe<CustomEmoji> emoji(final String id) {
        return catnip().cache().emoji(id(), id);
    }
    
    /**
     * @param id The id of the emoji to fetch.
     *
     * @return The emoji, or {@code null} if it isn't present.
     */
    @Nonnull
    @CheckReturnValue
    default Maybe<CustomEmoji> emoji(final long id) {
        return catnip().cache().emoji(idAsLong(), id);
    }
    
    /**
     * @param name The name of the emoji to fetch
     *
     * @return A possibly-empty collection of all emojis with matching names.
     */
    @Nonnull
    @CheckReturnValue
    default Collection<CustomEmoji> emojiByName(final String name) {
        return emojis().findByName(name);
    }
    
    // REST methods
    
    /**
     * Fetch all roles for this guild from the API.
     *
     * @return A Observable that completes when the roles are fetched.
     */
    @Nonnull
    @CheckReturnValue
    default Observable<Role> fetchRoles() {
        return catnip().rest().guild().getGuildRoles(id());
    }
    
    /**
     * Fetch all channels for this guild from the API.
     *
     * @return A Observable that completes when the channels are fetched.
     */
    @Nonnull
    @CheckReturnValue
    default Observable<GuildChannel> fetchChannels() {
        return catnip().rest().guild().getGuildChannels(id());
    }
    
    /**
     * Fetch all invites for this guild from the API.
     *
     * @return A Observable that completes when the invites are fetched.
     */
    @Nonnull
    @CheckReturnValue
    default Observable<CreatedInvite> fetchInvites() {
        PermissionUtil.checkPermissions(catnip(), id(), Permission.MANAGE_GUILD);
        return catnip().rest().guild().getGuildInvites(id());
    }
    
    /**
     * Fetch all webhooks for this guild.
     *
     * @return A Observable that completes when the webhooks are fetched.
     */
    @Nonnull
    @CheckReturnValue
    default Observable<Webhook> fetchWebhooks() {
        PermissionUtil.checkPermissions(catnip(), id(), Permission.MANAGE_WEBHOOKS);
        return catnip().rest().webhook().getGuildWebhooks(id());
    }
    
    /**
     * Fetch all emojis for this guild.
     *
     * @return A Observable that completes when the emojis are fetched.
     */
    @Nonnull
    @CheckReturnValue
    default Observable<CustomEmoji> fetchEmojis() {
        return catnip().rest().emoji().listGuildEmojis(id());
    }
    
    /**
     * Fetch a single emoji from this guild.
     *
     * @param emojiId The id of the emoji to fetch.
     *
     * @return A Observable that completes when the emoji is fetched.
     */
    @Nonnull
    @CheckReturnValue
    default Single<CustomEmoji> fetchEmoji(@Nonnull final String emojiId) {
        return catnip().rest().emoji().getGuildEmoji(id(), emojiId);
    }
    
    /**
     * Create a new emoji on the guild.
     *
     * @param name   The new emoji's name.
     * @param image  The image for the new emoji.
     * @param roles  The roles that can use the new emoji.
     * @param reason The reason that will be displayed in audit log.
     *
     * @return A Observable that completes when the emoji is created.
     */
    @Nonnull
    @CheckReturnValue
    default Single<CustomEmoji> createEmoji(@Nonnull final String name, @Nonnull final byte[] image,
                                            @Nonnull final Collection<String> roles, @Nullable final String reason) {
        PermissionUtil.checkPermissions(catnip(), id(), Permission.MANAGE_EMOJIS);
        return catnip().rest().emoji().createGuildEmoji(id(), name, image, roles, reason);
    }
    
    /**
     * Create a new emoji on the guild.
     *
     * @param name  The new emoji's name.
     * @param image The image for the new emoji.
     * @param roles The roles that can use the new emoji.
     *
     * @return A Observable that completes when the emoji is created.
     */
    @Nonnull
    @CheckReturnValue
    default Single<CustomEmoji> createEmoji(@Nonnull final String name, @Nonnull final byte[] image,
                                            @Nonnull final Collection<String> roles) {
        return createEmoji(name, image, roles, null);
    }
    
    /**
     * Create a new emoji on the guild.
     *
     * @param name      The new emoji's name.
     * @param imageData The image for the new emoji.
     * @param roles     The roles that can use the new emoji.
     * @param reason    The reason that will be displayed in audit log.
     *
     * @return A Observable that completes when the emoji is created.
     */
    @Nonnull
    @CheckReturnValue
    default Single<CustomEmoji> createEmoji(@Nonnull final String name, @Nonnull final URI imageData,
                                            @Nonnull final Collection<String> roles, @Nullable final String reason) {
        PermissionUtil.checkPermissions(catnip(), id(), Permission.MANAGE_EMOJIS);
        return catnip().rest().emoji().createGuildEmoji(id(), name, imageData, roles, reason);
    }
    
    /**
     * Create a new emoji on the guild.
     *
     * @param name      The new emoji's name.
     * @param imageData The image for the new emoji.
     * @param roles     The roles that can use the new emoji.
     *
     * @return A Observable that completes when the emoji is created.
     */
    @Nonnull
    @CheckReturnValue
    default Single<CustomEmoji> createEmoji(@Nonnull final String name, @Nonnull final URI imageData,
                                            @Nonnull final Collection<String> roles) {
        return createEmoji(name, imageData, roles, null);
    }
    
    /**
     * Modify the given emoji.
     *
     * @param emojiId The id of the emoji to modify.
     * @param name    The name of the emoji. To not change it, pass the old name.
     * @param roles   The roles that can use the emoji. To not change it, pass
     *                the old roles.
     * @param reason  The reason that will be displayed in audit log.
     *
     * @return A Observable that completes when the emoji is modified.
     */
    @Nonnull
    @CheckReturnValue
    default Single<CustomEmoji> modifyEmoji(@Nonnull final String emojiId, @Nonnull final String name,
                                            @Nonnull final Collection<String> roles, @Nullable final String reason) {
        PermissionUtil.checkPermissions(catnip(), id(), Permission.MANAGE_EMOJIS);
        return catnip().rest().emoji().modifyGuildEmoji(id(), emojiId, name, roles, reason);
    }
    
    /**
     * Modify the given emoji.
     *
     * @param emojiId The id of the emoji to modify.
     * @param name    The name of the emoji. To not change it, pass the old name.
     * @param roles   The roles that can use the emoji. To not change it, pass
     *                the old roles.
     *
     * @return A Observable that completes when the emoji is modified.
     */
    @Nonnull
    @CheckReturnValue
    default Single<CustomEmoji> modifyEmoji(@Nonnull final String emojiId, @Nonnull final String name,
                                            @Nonnull final Collection<String> roles) {
        return modifyEmoji(emojiId, name, roles, null);
    }
    
    /**
     * Delete the given emoji from the guild.
     *
     * @param emojiId The id of the emoji to delete.
     * @param reason  The reason that will be displayed in audit log.
     *
     * @return A Observable that completes when the emoji is deleted.
     */
    @Nonnull
    @CheckReturnValue
    default Completable deleteEmoji(@Nonnull final String emojiId, @Nullable final String reason) {
        PermissionUtil.checkPermissions(catnip(), id(), Permission.MANAGE_EMOJIS);
        return catnip().rest().emoji().deleteGuildEmoji(id(), emojiId, reason);
    }
    
    /**
     * Delete the given emoji from the guild.
     *
     * @param emojiId The id of the emoji to delete.
     *
     * @return A Observable that completes when the emoji is deleted.
     */
    @Nonnull
    @CheckReturnValue
    default Completable deleteEmoji(@Nonnull final String emojiId) {
        return deleteEmoji(emojiId, null);
    }
    
    /**
     * Leave this guild.
     *
     * @return A Observable that completes when the guild is left.
     */
    @Nonnull
    @CheckReturnValue
    default Completable leave() {
        return catnip().rest().user().leaveGuild(id());
    }
    
    /**
     * Delete this guild.
     *
     * @return A Observable that completes when the guild is deleted.
     */
    @Nonnull
    @CheckReturnValue
    default Completable delete() {
        return catnip().rest().guild().deleteGuild(id());
    }
    
    /**
     * Closes the voice connection for this guild. This method is equivalent to
     * {@code guild.catnip().{@link com.mewna.catnip.Catnip#closeVoiceConnection(String) closeVoiceConnection}(guild.id())}
     *
     * @see com.mewna.catnip.Catnip#closeVoiceConnection(String)
     */
    default void closeVoiceConnection() {
        catnip().closeVoiceConnection(id());
    }
    
    /**
     * Edit this guild.
     *
     * @return A guild editor that can complete the editing.
     */
    @Nonnull
    @CheckReturnValue
    default GuildEditFields edit() {
        PermissionUtil.checkPermissions(catnip(), id(), Permission.MANAGE_GUILD);
        return new GuildEditFields(this);
    }
    
    /**
     * Bans a user from a guild.
     * Needs {@link Permission#BAN_MEMBERS} and permission to interact with the target
     * {@link PermissionUtil#canInteract(Member, Member)}
     *
     * @param userId            The id of the user to ban.
     * @param reason            The reason of the ban.
     * @param deleteMessageDays The history of messages, in days, that will be deleted
     *
     * @return A Observable that completes when the member got banned
     */
    @Nonnull
    @CheckReturnValue
    default Completable ban(@Nonnull final String userId,
                            @Nullable final String reason,
                            @Nonnegative final int deleteMessageDays) {
        return Completable.fromMaybe(
                catnip().cache()
                        .member(id(), userId)
                        .map(member -> {
                            if(member == null) {
                                return Maybe.error(new NullPointerException("No member in " + id() + " with id " + userId));
                            } else {
                                PermissionUtil.checkPermissions(catnip(), id(), Permission.BAN_MEMBERS);
                                PermissionUtil.checkHierarchy(member, this);
                                return member;
                            }
                        })
                        .map(__ -> catnip().rest().guild().createGuildBan(id(), userId, reason, deleteMessageDays))
        );
    }
    
    /**
     * Bans a user from a guild.
     * Needs {@link Permission#BAN_MEMBERS} and permission to interact with the target
     * {@link PermissionUtil#canInteract(Member, Member)}
     *
     * @param userId            The id of the user to ban.
     * @param reason            The reason of the ban.
     * @param deleteMessageDays The history of messages, in days, that will be deleted
     *
     * @return A Observable that completes when the member got banned
     */
    @Nonnull
    @CheckReturnValue
    default Completable ban(final long userId,
                            @Nullable final String reason,
                            @Nonnegative final int deleteMessageDays) {
        return ban(Long.toUnsignedString(userId), reason, deleteMessageDays);
    }
    
    /**
     * Bans a user from a guild.
     * Needs {@link Permission#BAN_MEMBERS} and permission to interact with the target
     * {@link PermissionUtil#canInteract(Member, Member)}
     *
     * @param userId            The id of the user to ban.
     * @param deleteMessageDays The history of messages, in days, that will be deleted
     *
     * @return A Observable that completes when the member got banned
     */
    @Nonnull
    @CheckReturnValue
    default Completable ban(final long userId,
                            @Nonnegative final int deleteMessageDays) {
        return ban(userId, null, deleteMessageDays);
    }
    
    /**
     * Bans a user from a guild.
     * Needs {@link Permission#BAN_MEMBERS} and permission to interact with the target
     * {@link PermissionUtil#canInteract(Member, Member)}
     *
     * @param member            The member to ban.
     * @param reason            The reason of the ban.
     * @param deleteMessageDays The history of messages, in days, that will be deleted
     *
     * @return A Observable that completes when the member got banned
     */
    @Nonnull
    @CheckReturnValue
    default Completable ban(@Nonnull final Member member,
                            @Nullable final String reason,
                            @Nonnegative final int deleteMessageDays) {
        return ban(member.id(), reason, deleteMessageDays);
    }
    
    /**
     * Removes a users ban from the Guild
     * Needs {@link Permission#BAN_MEMBERS} and permission to interact with the target
     * {@link PermissionUtil#canInteract(Member, Member)}
     *
     * @param userId The id of the user to unban
     * @param reason The reason for the unban
     *
     * @return A Observable that completes when the ban got removed
     */
    @Nonnull
    @CheckReturnValue
    default Completable unban(@Nonnull final String userId, @Nullable final String reason) {
        PermissionUtil.checkPermissions(catnip(), id(), Permission.BAN_MEMBERS);
        return catnip().rest().guild().removeGuildBan(id(), userId, reason);
    }
    
    /**
     * Removes a users ban from the Guild
     * Needs {@link Permission#BAN_MEMBERS} and permission to interact with the target
     * {@link PermissionUtil#canInteract(Member, Member)}
     *
     * @param userId The id of the user to unban
     *
     * @return A Observable that completes when the ban got removed
     */
    @Nonnull
    @CheckReturnValue
    default Completable unban(@Nonnull final String userId) {
        return unban(userId, null);
    }
    
    /**
     * Removes a users ban from the Guild
     * Needs {@link Permission#BAN_MEMBERS} and permission to interact with the target
     * {@link PermissionUtil#canInteract(Member, Member)}
     *
     * @param userId The id of the user to unban
     * @param reason The reason for the unban
     *
     * @return A Observable that completes when the ban got removed
     */
    @Nonnull
    @CheckReturnValue
    default Completable unban(final long userId, @Nullable final String reason) {
        return unban(Long.toUnsignedString(userId), reason);
    }
    
    /**
     * Removes a users ban from the Guild
     * Needs {@link Permission#BAN_MEMBERS} and permission to interact with the target
     * {@link PermissionUtil#canInteract(Member, Member)}
     *
     * @param userId The id of the user to unban
     *
     * @return A Observable that completes when the ban got removed
     */
    @Nonnull
    @CheckReturnValue
    default Completable unban(final long userId) {
        return unban(userId, null);
    }
    
    /**
     * Removes a users ban from the Guild
     * Needs {@link Permission#BAN_MEMBERS} and permission to interact with the target
     * {@link PermissionUtil#canInteract(Member, Member)}
     *
     * @param user   The the user to unban
     * @param reason The that will be displayed in audit log
     *
     * @return A Observable that completes when the ban got removed
     */
    @Nonnull
    @CheckReturnValue
    default Completable unban(@Nonnull final User user, @Nullable final String reason) {
        PermissionUtil.checkPermissions(catnip(), id(), Permission.BAN_MEMBERS);
        return catnip().rest().guild().removeGuildBan(id(), user.id(), reason);
    }
    
    /**
     * Removes a users ban from the Guild
     * Needs {@link Permission#BAN_MEMBERS} and permission to interact with the target
     * {@link PermissionUtil#canInteract(Member, Member)}
     *
     * @param user The the user to unban
     *
     * @return A Observable that completes when the ban got removed
     */
    @Nonnull
    @CheckReturnValue
    default Completable unban(@Nonnull final User user) {
        return unban(user, null);
    }
    
    /**
     * Kicks a member from the Guild.
     * Needs {@link Permission#KICK_MEMBERS} and permission to interact with the target
     * {@link PermissionUtil#canInteract(Member, Member)}
     *
     * @param userId The id of the user to kick
     * @param reason The reason for the kick
     *
     * @return A Observable that is finished when the user got kicked
     */
    @Nonnull
    @CheckReturnValue
    default Completable kick(@Nonnull final String userId, @Nullable final String reason) {
        return Completable.fromMaybe(
                catnip().cache().member(id(), userId)
                        .map(member -> {
                            if(member == null) {
                                return Maybe.error(new NullPointerException("No such member in " + id() + " with id " + userId));
                            } else {
                                PermissionUtil.checkPermissions(catnip(), id(), Permission.KICK_MEMBERS);
                                PermissionUtil.checkHierarchy(member, this);
                                return member;
                            }
                        })
                        .map(__ -> catnip().rest().guild().removeGuildMember(id(), userId, reason))
        );
    }
    
    /**
     * Kicks a member from the Guild.
     * Needs {@link Permission#KICK_MEMBERS} and permission to interact with the target
     * {@link PermissionUtil#canInteract(Member, Member)}
     *
     * @param userId The id of the user to kick
     *
     * @return A Observable that is finished when the user got kicked
     */
    @Nonnull
    @CheckReturnValue
    default Completable kick(@Nonnull final String userId) {
        return kick(userId, null);
    }
    
    /**
     * Kicks a member from the Guild.
     * Needs {@link Permission#KICK_MEMBERS} and permission to interact with the target
     * {@link PermissionUtil#canInteract(Member, Member)}
     *
     * @param userId The id of the user to kick
     * @param reason The reason for the kick
     *
     * @return A Observable that is finished when the user got kicked
     */
    @Nonnull
    @CheckReturnValue
    default Completable kick(final long userId, @Nullable final String reason) {
        return kick(Long.toUnsignedString(userId), reason);
    }
    
    /**
     * Kicks a member from the Guild.
     * Needs {@link Permission#KICK_MEMBERS} and permission to interact with the target
     * {@link PermissionUtil#canInteract(Member, Member)}
     *
     * @param userId The id of the user to kick
     *
     * @return A Observable that is finished when the user got kicked
     */
    @Nonnull
    @CheckReturnValue
    default Completable kick(final long userId) {
        return kick(userId, null);
    }
    
    /**
     * Kicks a member from the Guild.
     * Needs {@link Permission#KICK_MEMBERS} and permission to interact with the target
     * {@link PermissionUtil#canInteract(Member, Member)}
     *
     * @param member The member to kick
     * @param reason The reason for the kick
     *
     * @return A Observable that is finished when the user got kicked
     */
    @Nonnull
    @CheckReturnValue
    default Completable kick(final Member member, @Nullable final String reason) {
        return kick(member.id(), reason);
    }
    
    /**
     * Kicks a member from the Guild.
     * Needs {@link Permission#KICK_MEMBERS} and permission to interact with the target
     * {@link PermissionUtil#canInteract(Member, Member)}
     *
     * @param member The member to kick
     *
     * @return A Observable that is finished when the user got kicked
     */
    @Nonnull
    @CheckReturnValue
    default Completable kick(final Member member) {
        return kick(member.id(), null);
    }
    
    /**
     * Changes the nick of the bots user on the guild
     * Needs {@link Permission#CHANGE_NICKNAME}
     *
     * @param nickname The new nickname
     * @param reason   The reason of the nickname change
     *
     * @return A Observable containing the new nickname
     *
     * @throws IllegalArgumentException If the nickname is longer than {@link Guild#NICKNAME_MAX_LENGTH}
     */
    @Nonnull
    @CheckReturnValue
    default Single<String> changeNickName(@Nonnull final String nickname, @Nullable final String reason) {
        if(nickname.length() > NICKNAME_MAX_LENGTH) {
            throw new IllegalArgumentException("Nickname must not be longer than" + NICKNAME_MAX_LENGTH);
        }
        PermissionUtil.checkPermissions(catnip(), id(), Permission.CHANGE_NICKNAME);
        return catnip().rest().guild().modifyCurrentUsersNick(id(), nickname, reason);
    }
    
    /**
     * Modifies the guild member with the specified updates.
     * Needs {@link Permission#MANAGE_NICKNAMES}, {@link Permission#MANAGE_ROLES},
     * {@link Permission#MUTE_MEMBERS}, {@link Permission#DEAFEN_MEMBERS},
     * and {@link Permission#MOVE_MEMBERS}.
     *
     * @param member The member to modify.
     * @param data   The parts of the member to update.
     *
     * @return A {@link Completable} that completes when the update is completed.
     */
    @Nonnull
    @CheckReturnValue
    default Completable modifyGuildMember(@Nonnull final Member member, @Nonnull final MemberData data) {
        return modifyGuildMember(member, data, null);
    }
    
    /**
     * Modifies the guild member with the specified updates.
     * Needs {@link Permission#MANAGE_NICKNAMES}, {@link Permission#MANAGE_ROLES},
     * {@link Permission#MUTE_MEMBERS}, {@link Permission#DEAFEN_MEMBERS},
     * and {@link Permission#MOVE_MEMBERS}.
     *
     * @param member The member to modify.
     * @param data   The parts of the member to update.
     * @param reason The audit log reason for the update.
     *
     * @return A {@link Completable} that completes when the update is completed.
     */
    @Nonnull
    @CheckReturnValue
    default Completable modifyGuildMember(@Nonnull final Member member, @Nonnull final MemberData data,
                                          @Nullable final String reason) {
        // TODO: Fine-grained perms checking would be neat.
        PermissionUtil.checkPermissions(catnip(), id(), Permission.MANAGE_ROLES, Permission.MANAGE_NICKNAMES,
                Permission.MUTE_MEMBERS, Permission.DEAFEN_MEMBERS, Permission.MOVE_MEMBERS);
        return catnip().rest().guild().modifyGuildMember(id(), member.id(), data, reason);
    }
    
    /**
     * @return A new updater for channels in this guild. See
     * {@link PositionUpdater} for how to use it.
     */
    @Nonnull
    @CheckReturnValue
    default PositionUpdater channelPositionUpdater() {
        return new PositionUpdater(id(), false);
    }
    
    /**
     * @return A new updater for roles in this guild. See
     * {@link PositionUpdater} for how to use it.
     */
    @Nonnull
    @CheckReturnValue
    default PositionUpdater rolePositionUpdater() {
        return new PositionUpdater(id(), true);
    }
    
    /**
     * Modifies the positions of channels. See the docs on
     * {@link PositionUpdater} for how to use it.
     *
     * @param updater The position updater.
     *
     * @return A {@link Completable} that completes when the update is completed.
     */
    @Nonnull
    @CheckReturnValue
    default Completable modifyChannelPositions(@Nonnull final PositionUpdater updater) {
        return modifyChannelPositions(updater, null);
    }
    
    /**
     * Modifies the positions of channels. See the docs on
     * {@link PositionUpdater} for how to use it.
     *
     * @param updater The position updater.
     * @param reason  The reason for the update.
     *
     * @return A {@link Completable} that completes when the update is completed.
     */
    @Nonnull
    @CheckReturnValue
    default Completable modifyChannelPositions(@Nonnull final PositionUpdater updater, @Nullable final String reason) {
        PermissionUtil.checkPermissions(catnip(), id(), Permission.MANAGE_CHANNELS);
        return catnip().rest().guild().modifyGuildChannelPositions(updater, reason);
    }
    
    /**
     * Modifies the positions of roles. See the docs on
     * {@link PositionUpdater} for how to use it.
     *
     * @param updater The position updater.
     *
     * @return A {@link Completable} that completes when the update is completed.
     */
    @Nonnull
    @CheckReturnValue
    default Completable modifyRolePositions(@Nonnull final PositionUpdater updater) {
        return modifyRolePositions(updater, null);
    }
    
    /**
     * Modifies the positions of roles. See the docs on
     * {@link PositionUpdater} for how to use it.
     *
     * @param updater The position updater.
     * @param reason  The reason for the update.
     *
     * @return A {@link Completable} that completes when the update is completed.
     */
    @Nonnull
    @CheckReturnValue
    default Completable modifyRolePositions(@Nonnull final PositionUpdater updater, @Nullable final String reason) {
        PermissionUtil.checkPermissions(catnip(), id(), Permission.MANAGE_ROLES);
        return catnip().rest().guild().modifyGuildRolePositions(updater, reason);
    }
    
    /**
     * Changes the nick of the bots user on the guild
     * Needs {@link Permission#CHANGE_NICKNAME}
     *
     * @param nickname The new nickname
     *
     * @return A Observable containing the new nickname
     *
     * @throws IllegalArgumentException If the nickname is longer than {@link Guild#NICKNAME_MAX_LENGTH}
     */
    @Nonnull
    @CheckReturnValue
    default Single<String> changeNickName(@Nonnull final String nickname) {
        return changeNickName(nickname, null);
    }
    
    /**
     * Adds a role to a member.
     * Needs {@link Permission#MANAGE_ROLES} and permission to interact with the role
     * {@link PermissionUtil#canInteract(Member, Role)}
     *
     * @param role   The role to assign
     * @param member The member to assign the role to
     * @param reason The reason that will be displayed in audit log
     *
     * @return A Observable that completes when the role got added
     */
    @Nonnull
    @CheckReturnValue
    default Completable addRoleToMember(@Nonnull final Role role, @Nonnull final Member member, @Nullable final String reason) {
        PermissionUtil.checkPermissions(catnip(), id(), Permission.MANAGE_ROLES);
        PermissionUtil.checkHierarchy(role, this);
        return catnip().rest().guild().addGuildMemberRole(id(), member.id(), role.id(), reason);
    }
    
    /**
     * Adds a role to a member.
     * Needs {@link Permission#MANAGE_ROLES} and permission to interact with the role
     * {@link PermissionUtil#canInteract(Member, Role)}
     *
     * @param role   The role to assign
     * @param member The member to assign the role to
     *
     * @return A Observable that completes when the role got added
     */
    @Nonnull
    @CheckReturnValue
    default Completable addRoleToMember(@Nonnull final Role role, @Nonnull final Member member) {
        return addRoleToMember(role, member, null);
    }
    
    /**
     * Removes a role from a member.
     * Needs {@link Permission#MANAGE_ROLES} and permission to interact with the role
     * {@link PermissionUtil#canInteract(Member, Role)}
     *
     * @param role   The role to remove
     * @param member The member to remove the role from
     * @param reason The reason that will be displayed in audit log
     *
     * @return A Observable that completes when the role got removed
     */
    @Nonnull
    @CheckReturnValue
    default Completable removeRoleFromMember(@Nonnull final Role role, @Nonnull final Member member,
                                             @Nullable final String reason) {
        PermissionUtil.checkPermissions(catnip(), id(), Permission.MANAGE_ROLES);
        PermissionUtil.checkHierarchy(role, this);
        return catnip().rest().guild().removeGuildMemberRole(id(), member.id(), role.id(), reason);
    }
    
    /**
     * Removes a role from a member.
     * Needs {@link Permission#MANAGE_ROLES} and permission to interact with the role
     * {@link PermissionUtil#canInteract(Member, Role)}
     *
     * @param role   The role to remove
     * @param member The member to remove the role from
     *
     * @return A Observable that completes when the role got removed
     */
    @Nonnull
    @CheckReturnValue
    default Completable removeRoleFromMember(@Nonnull final Role role, @Nonnull final Member member) {
        return removeRoleFromMember(role, member, null);
    }
    
    /**
     * Creates a new channel in this guild.
     *
     * @param data The data to create the channel with.
     *
     * @return A {@link Single} that completes with the created channel.
     */
    @Nonnull
    @CheckReturnValue
    default Single<GuildChannel> createChannel(@Nonnull final ChannelData data) {
        return createChannel(data, null);
    }
    
    /**
     * Creates a new channel in this guild.
     *
     * @param data   The data to create the channel with.
     * @param reason The reason for creating the channel.
     *
     * @return A {@link Single} that completes with the created channel.
     */
    @Nonnull
    @CheckReturnValue
    default Single<GuildChannel> createChannel(@Nonnull final ChannelData data, @Nullable final String reason) {
        PermissionUtil.checkPermissions(catnip(), id(), Permission.MANAGE_CHANNELS);
        return catnip().rest().guild().createGuildChannel(id(), data, reason);
    }
    
    /**
     * @return A {@link Single} that completes with the embed for this guild.
     */
    @Nonnull
    @CheckReturnValue
    default Single<GuildEmbed> getEmbed() {
        return catnip().rest().guild().getGuildEmbed(id());
    }
    
    /**
     * @param channel The channel to invite users to.
     * @param enabled Whether the embed is enabled.
     *
     * @return A {@link Single} that completes with the modified embed.
     */
    @Nonnull
    @CheckReturnValue
    default Single<GuildEmbed> modifyGuildEmbed(@Nonnull final GuildChannel channel, final boolean enabled) {
        return modifyGuildEmbed(channel, enabled, null);
    }
    
    /**
     * @param channel The channel to invite users to.
     * @param enabled Whether the embed is enabled.
     * @param reason  The reason for the modification.
     *
     * @return A {@link Single} that completes with the modified embed.
     */
    @Nonnull
    @CheckReturnValue
    default Single<GuildEmbed> modifyGuildEmbed(@Nonnull final GuildChannel channel, final boolean enabled,
                                                @Nullable final String reason) {
        return catnip().rest().guild().modifyGuildEmbed(id(), channel.id(), enabled, reason);
    }
    
    /**
     * @return The vanity invite for this guild, if any.
     */
    @Nonnull
    @CheckReturnValue
    default Single<CreatedInvite> vanityInvite() {
        // TODO: This needs to be a Maybe
        return catnip().rest().guild().getGuildVanityURL(id());
    }
    
    /**
     * @param data The data to create the role with.
     *
     * @return A {@link Single} that completes with the created role.
     */
    @Nonnull
    @CheckReturnValue
    default Single<Role> createRole(@Nonnull final RoleData data) {
        return createRole(data, null);
    }
    
    /**
     * @param data   The data to create the role with.
     * @param reason The reason for creating this role.
     *
     * @return A {@link Single} that completes with the created role.
     */
    @Nonnull
    @CheckReturnValue
    default Single<Role> createRole(@Nonnull final RoleData data, @Nullable final String reason) {
        return catnip().rest().guild().createGuildRole(id(), data, reason);
    }
    
    /**
     * Searches guild members for a member matching the given query.
     *
     * @param query The search query to use
     *
     * @return A {@link Maybe} with the member that matches, if any.
     */
    @Nonnull
    @CheckReturnValue
    default Maybe<Member> searchMembers(@Nonnull final String query) {
        return searchMembers(query, 1).singleElement();
    }
    
    /**
     * Searches guild members for all members matching the given query.
     *
     * @param query The search query to use.
     * @param limit The maximum number of members to return. Must be 1 <= N <= 100.
     *
     * @return An {@link Observable} containing the matched members, if any.
     */
    @Nonnull
    @CheckReturnValue
    default Observable<Member> searchMembers(@Nonnull final String query, @Nonnegative final int limit) {
        return catnip().rest().guild().searchGuildMembers(id(), query, limit);
    }
    
    /**
     * The notification level for a guild.
     */
    enum NotificationLevel {
        /**
         * Users get notifications for all messages.
         */
        ALL_MESSAGES(0),
        /**
         * Users only get notifications for mentions.
         */
        ONLY_MENTIONS(1);
        
        @Getter
        private final int key;
        
        NotificationLevel(final int key) {
            this.key = key;
        }
        
        @Nonnull
        public static NotificationLevel byKey(final int key) {
            for(final NotificationLevel level : values()) {
                if(level.key == key) {
                    return level;
                }
            }
            throw new IllegalArgumentException("No verification level for key " + key);
        }
    }
    
    /**
     * The content filter level for a guild.
     */
    enum ContentFilterLevel {
        /**
         * No messages are filtered.
         */
        DISABLED(0),
        /**
         * Only messages from members with no roles are filtered.
         */
        MEMBERS_WITHOUT_ROLES(1),
        /**
         * Messages from all members are filtered.
         */
        ALL_MEMBERS(2);
        
        @Getter
        private final int key;
        
        ContentFilterLevel(final int key) {
            this.key = key;
        }
        
        @Nonnull
        public static ContentFilterLevel byKey(final int key) {
            for(final ContentFilterLevel level : values()) {
                if(level.key == key) {
                    return level;
                }
            }
            throw new IllegalArgumentException("No content filter level for key " + key);
        }
    }
    
    /**
     * The 2FA level required for this guild.
     */
    enum MFALevel {
        /**
         * 2FA is not required.
         */
        NONE(0),
        /**
         * 2FA is required for admin actions.
         */
        ELEVATED(1);
        
        @Getter
        private final int key;
        
        MFALevel(final int key) {
            this.key = key;
        }
        
        @Nonnull
        public static MFALevel byKey(final int key) {
            for(final MFALevel level : values()) {
                if(level.key == key) {
                    return level;
                }
            }
            throw new IllegalArgumentException("No MFA level for key " + key);
        }
    }
    
    /**
     * The verification level for a guild.
     */
    enum VerificationLevel {
        /**
         * No restrictions.
         */
        NONE(0),
        /**
         * Members must have a verified email on their account.
         */
        LOW(1),
        /**
         * Members must also be registered on Discord for more than 5 minutes.
         */
        MEDIUM(2),
        /**
         * Members must also have been a member of this guild for more than 10
         * minutes.
         */
        HIGH(3),
        /**
         * Members must also have a verified phone number on their account.
         */
        VERY_HIGH(4);
        
        @Getter
        private final int key;
        
        VerificationLevel(final int key) {
            this.key = key;
        }
        
        @Nonnull
        public static VerificationLevel byKey(final int key) {
            for(final VerificationLevel level : values()) {
                if(level.key == key) {
                    return level;
                }
            }
            throw new IllegalArgumentException("No verification level for key " + key);
        }
    }
    
    /**
     * The Nitro Boost level of a guild.
     */
    enum PremiumTier {
        NONE(0),
        TIER_1(1),
        TIER_2(2),
        TIER_3(3),
        ;
        
        @Getter
        private final int key;
        
        PremiumTier(final int key) {
            this.key = key;
        }
        
        @Nonnull
        public static PremiumTier byKey(final int key) {
            for(final PremiumTier tier : values()) {
                if(tier.key == key) {
                    return tier;
                }
            }
            throw new IllegalArgumentException("No premium tier for key " + key);
        }
    }
    
    @SuppressWarnings({"unused", "RedundantSuppression"})
    @Getter
    @Setter
    @Accessors(fluent = true)
    class GuildEditFields {
        private final Guild guild;
        private String name;
        private String region;
        private VerificationLevel verificationLevel;
        private NotificationLevel defaultMessageNotifications;
        private ContentFilterLevel explicitContentFilter;
        private String afkChannelId;
        private Integer afkTimeout;
        private URI icon;
        private String ownerId;
        private URI splash;
        private String systemChannelId;
        
        public GuildEditFields(@Nullable final Guild guild) {
            this.guild = guild;
        }
        
        public GuildEditFields() {
            this(null);
        }
        
        @Nonnull
        public GuildEditFields icon(@Nullable final URI iconData) {
            if(iconData != null) {
                Utils.validateImageUri(iconData);
            }
            icon = iconData;
            return this;
        }
        
        @Nonnull
        public GuildEditFields icon(@Nullable final byte[] iconData) {
            return icon(iconData == null ? null : Utils.asImageDataUri(iconData));
        }
        
        @Nonnull
        public GuildEditFields splash(@Nullable final URI splashData) {
            if(splashData != null) {
                Utils.validateImageUri(splashData);
            }
            splash = splashData;
            return this;
        }
        
        @Nonnull
        public GuildEditFields splash(@Nullable final byte[] splashData) {
            return splash(splashData == null ? null : Utils.asImageDataUri(splashData));
        }
        
        @Nonnull
        public Single<Guild> submit() {
            if(guild == null) {
                throw new IllegalStateException("Cannot submit edit without a guild object! Please use RestGuild directly instead");
            }
            return guild.catnip().rest().guild().modifyGuild(guild.id(), this);
        }
        
        @Nonnull
        @CheckReturnValue
        public JsonObject payload() {
            final JsonObject payload = new JsonObject();
            if(name != null && (guild == null || !Objects.equals(name, guild.name()))) {
                payload.put("name", name);
            }
            if(region != null && (guild == null || !Objects.equals(region, guild.region()))) {
                payload.put("region", region);
            }
            if(verificationLevel != null && (guild == null || verificationLevel != guild.verificationLevel())) {
                payload.put("verification_level", verificationLevel.key());
            }
            if(defaultMessageNotifications != null && (guild == null || defaultMessageNotifications != guild.defaultMessageNotifications())) {
                payload.put("default_message_notifications", defaultMessageNotifications.key());
            }
            if(explicitContentFilter != null && (guild == null || explicitContentFilter != guild.explicitContentFilter())) {
                payload.put("explicit_content_filter", explicitContentFilter.key());
            }
            if(afkChannelId != null && (guild == null || !Objects.equals(afkChannelId, guild.afkChannelId()))) {
                payload.put("afk_channel_id", afkChannelId);
            }
            if(afkTimeout != null && (guild == null || !Objects.equals(afkTimeout, guild.afkTimeout()))) {
                payload.put("afk_timeout", afkTimeout);
            }
            if(icon != null) {
                payload.put("icon", icon.toString());
            }
            if(ownerId != null && (guild == null || !Objects.equals(ownerId, guild.ownerId()))) {
                payload.put("owner_id", ownerId);
            }
            if(splash != null) {
                payload.put("splash", splash.toString());
            }
            if(systemChannelId != null && (guild == null || !Objects.equals(systemChannelId, guild.systemChannelId()))) {
                payload.put("system_channel_id", systemChannelId);
            }
            return payload;
        }
    }
}
