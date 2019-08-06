/*
 * Copyright (c) 2018-2019 amy, All rights reserved.
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

package com.mewna.catnip.entity;

import com.cedarsoftware.util.DeepEquals;
import com.grack.nanojson.JsonObject;
import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.channel.*;
import com.mewna.catnip.entity.channel.Channel.ChannelType;
import com.mewna.catnip.entity.guild.*;
import com.mewna.catnip.entity.guild.Guild.ContentFilterLevel;
import com.mewna.catnip.entity.guild.Guild.MFALevel;
import com.mewna.catnip.entity.guild.Guild.NotificationLevel;
import com.mewna.catnip.entity.guild.Guild.VerificationLevel;
import com.mewna.catnip.entity.guild.Invite.InviteChannel;
import com.mewna.catnip.entity.guild.Invite.InviteGuild;
import com.mewna.catnip.entity.guild.Invite.Inviter;
import com.mewna.catnip.entity.guild.PermissionOverride.OverrideType;
import com.mewna.catnip.entity.guild.audit.*;
import com.mewna.catnip.entity.impl.channel.*;
import com.mewna.catnip.entity.impl.guild.*;
import com.mewna.catnip.entity.impl.guild.InviteImpl.InviteChannelImpl;
import com.mewna.catnip.entity.impl.guild.InviteImpl.InviteGuildImpl;
import com.mewna.catnip.entity.impl.guild.InviteImpl.InviterImpl;
import com.mewna.catnip.entity.impl.guild.audit.*;
import com.mewna.catnip.entity.impl.message.*;
import com.mewna.catnip.entity.impl.message.EmbedImpl.*;
import com.mewna.catnip.entity.impl.message.AttachmentImpl;
import com.mewna.catnip.entity.impl.message.ReactionImpl;
import com.mewna.catnip.entity.impl.misc.*;
import com.mewna.catnip.entity.impl.user.*;
import com.mewna.catnip.entity.impl.user.PresenceImpl.ActivityImpl;
import com.mewna.catnip.entity.impl.voice.VoiceRegionImpl;
import com.mewna.catnip.entity.impl.voice.VoiceServerUpdateImpl;
import com.mewna.catnip.entity.message.*;
import com.mewna.catnip.entity.message.Embed.EmbedType;
import com.mewna.catnip.entity.message.Embed.Field;
import com.mewna.catnip.entity.message.Message.Attachment;
import com.mewna.catnip.entity.message.Message.Reaction;
import com.mewna.catnip.entity.misc.*;
import com.mewna.catnip.entity.misc.Emoji.CustomEmoji;
import com.mewna.catnip.entity.misc.Emoji.UnicodeEmoji;
import com.mewna.catnip.entity.user.*;
import com.mewna.catnip.entity.user.Presence.Activity;
import com.mewna.catnip.entity.user.Presence.ActivityType;
import com.mewna.catnip.entity.user.Presence.OnlineStatus;
import com.mewna.catnip.entity.util.Permission;
import com.mewna.catnip.entity.voice.VoiceRegion;
import com.mewna.catnip.entity.voice.VoiceServerUpdate;
import com.mewna.catnip.util.JsonUtil;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.awt.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by napster on 01.01.19.
 * <p>
 * Ensure that all our entities and the data they carry survives being serialized and deserialized through the eventbus.
 * <p>
 * Note: We can't use {@link Object#equals} of most entities because many of them only check the ids for being equal.
 */
@SuppressWarnings("OverlyCoupledClass")
class EntityTest {
    private <T extends Entity> void testEntity(final T entity) {
        final Catnip thisCatnip = mockNip();
        @SuppressWarnings("unchecked")
        final Class<T> entityClass = (Class<T>) entity.getClass();
        final T deserialized = Entity.fromJson(thisCatnip, entityClass, entity.toJson());
        
        final Map<Object, Object> deepEqualsOptions = new HashMap<>();
        deepEqualsOptions.put(DeepEquals.IGNORE_CUSTOM_EQUALS, Collections.emptySet()); //empty set = ignore all
        // If you end up here with a really bad and undescriptive message of a test failing, I'm sorry.
        // As of adding this test, none of the usual suspects/proper testing libs that make nice messages upon failure
        // like JUnit, Hamcrest, AssertJ, Shazamcrest had a proper deep equals feature.
        assertTrue(DeepEquals.deepEquals(entity, deserialized, deepEqualsOptions));
        
        //check equals implementation
//        assertEquals(entity, deserialized); TODO ideally all entities should have an equals method
        
        //check that our catnip has been set on the entity
        if(deserialized instanceof RequiresCatnip) {
            assertSame(thisCatnip, deserialized.catnip());
        } //TODO run same check on nested entities and collections of nested entities
    }
    
    private <T> void testObject(final T entity) {
        final Catnip thisCatnip = mockNip();
        @SuppressWarnings("unchecked")
        final Class<T> entityClass = (Class<T>) entity.getClass();
        final JsonObject data = JsonUtil.destringifySnowflakes(JsonUtil.stringifySnowflakes(JsonUtil.mapFrom(entity)));
        final T deserialized = JsonUtil.mapTo(data, entityClass);
        if(deserialized instanceof RequiresCatnip) {
            ((RequiresCatnip) deserialized).catnip(thisCatnip);
        }
        
        final Map<Object, Object> deepEqualsOptions = new HashMap<>();
        deepEqualsOptions.put(DeepEquals.IGNORE_CUSTOM_EQUALS, Collections.emptySet()); //empty set = ignore all
        // If you end up here with a really bad and undescriptive message of a test failing, I'm sorry.
        // As of adding this test, none of the usual suspects/proper testing libs that make nice messages upon failure
        // like JUnit, Hamcrest, AssertJ, Shazamcrest had a proper deep equals feature.
        assertTrue(DeepEquals.deepEquals(entity, deserialized, deepEqualsOptions));
        
        //check equals implementation
//        assertEquals(entity, deserialized); TODO ideally all entities should have an equals method
        
        //check that our catnip has been set on the entity
        if(deserialized instanceof RequiresCatnip) {
            assertSame(thisCatnip, ((RequiresCatnip) deserialized).catnip());
        } //TODO run same check on nested entities and collections of nested entities
    }
    
    @Test
    void applicationInfo() {
        final Catnip mocknip = Mockito.mock(Catnip.class);
        final ApplicationInfo applicationInfo = ApplicationInfoImpl.builder()
                .catnip(mocknip)
                .idAsLong(randomPositiveLong())
                .name("SAMUEL L. IPSUM bot")
                .icon("whatever.jpeg")
                .description("Well, the way they make shows is, they make one show. That show's called a pilot. Then they show that show to the people who make shows, and on the strength of that one show they decide if they're going to make more shows. Some pilots get picked and become television programs. Some don't, become nothing. She starred in one of the ones that became nothing.")
                .rpcOrigins(Arrays.asList(randomPositiveLongAsString(), randomPositiveLongAsString()))
                .publicBot(ThreadLocalRandom.current().nextBoolean())
                .requiresCodeGrant(ThreadLocalRandom.current().nextBoolean())
                .owner(applicationOwner(mocknip))
                .build();
        
        testEntity(applicationInfo);
    }
    
    @Test
    void applicationOwner() {
        testEntity(applicationOwner(mockNip()));
    }
    
    @Test
    void auditLogChange() {
        testEntity(auditLogChange(mockNip()));
    }
    
    @Test
    void auditLogEntry() {
        final Catnip mocknip = mockNip();
        final AuditLogEntry auditLogEntry = AuditLogEntryImpl.builder()
                .catnip(mocknip)
                .idAsLong(randomPositiveLong())
                .targetIdAsLong(randomPositiveLong())
                .user(user(mocknip))
                .reason("Good reason")
                .options(optionalEntryInfo(mocknip))
                .type(random(ActionType.values()))
                .changes(Arrays.asList(auditLogChange(mocknip), auditLogChange(mocknip)))
                .webhook(webhook(mocknip))
                .build();
        
        testEntity(auditLogEntry);
    }
    
    @Test
    void bulkDeletedMessages() {
        final BulkDeletedMessages bulkDeletedMessages = BulkDeletedMessagesImpl.builder()
                .catnip(mockNip())
                .ids(Arrays.asList(
                        Long.toString(randomPositiveLong()),
                        Long.toString(randomPositiveLong())
                ))
                .channelIdAsLong(randomPositiveLong())
                .guildIdAsLong(randomPositiveLong())
                .build();
        
        testEntity(bulkDeletedMessages);
    }
    
    @Test
    void bulkRemovedReactions() {
        final BulkRemovedReactions bulkRemovedReactions = BulkRemovedReactionsImpl.builder()
                .catnip(mockNip())
                .channelId(randomPositiveLongAsString())
                .messageId(randomPositiveLongAsString())
                .guildId(randomPositiveLongAsString())
                .build();
        
        testEntity(bulkRemovedReactions);
    }
    
    @Test
    void category() {
        final Catnip mockNip = mockNip();
        final Category category = CategoryImpl.builder()
                .catnip(mockNip)
                .idAsLong(randomPositiveLong())
                .name("this is a category")
                .guildIdAsLong(randomPositiveLong())
                .position(4)
                .parentIdAsLong(randomPositiveLong())
                .overrides(Arrays.asList(permissionOverride(mockNip), permissionOverride(mockNip)))
                .build();
        
        testEntity(category);
    }
    
    @Test
    void channelPinsUpdate() {
        final ChannelPinsUpdate channelPinsUpdate = ChannelPinsUpdateImpl.builder()
                .catnip(mockNip())
                .channelIdAsLong(randomPositiveLong())
                .lastPinTimestamp(OffsetDateTime.now().toString())
                .build();
        
        testEntity(channelPinsUpdate);
    }
    
    @Test
    void createdInvite() {
        final Catnip mockNip = mockNip();
        final CreatedInvite createdInvite = CreatedInviteImpl.builder()
                .catnip(mockNip)
                .code("qwert")
                .inviter(inviter(mockNip))
                .guild(inviteGuild(mockNip))
                .channel(inviteChannel(mockNip))
                .approximatePresenceCount(4096)
                .approximateMemberCount(8192)
                .uses(12)
                .maxUses(20)
                .temporary(ThreadLocalRandom.current().nextBoolean())
                .createdAt(OffsetDateTime.now().toString())
                .revoked(ThreadLocalRandom.current().nextBoolean())
                .build();
        
        testEntity(createdInvite);
    }
    
    @Test
    void customEmoji() {
        testEntity(customEmoji(mockNip()));
    }
    
    @Test
    void deletedMessage() {
        final DeletedMessage deletedMessage = DeletedMessageImpl.builder()
                .catnip(mockNip())
                .idAsLong(randomPositiveLong())
                .channelIdAsLong(randomPositiveLong())
                .guildIdAsLong(randomPositiveLong())
                .build();
        testEntity(deletedMessage);
    }
    
    @Test
    void embed() {
        testObject(embed(mockNip()));
    }
    
    @Test
    void emojiUpdate() {
        final Catnip mockNip = mockNip();
        final EmojiUpdate emojiUpdate = EmojiUpdateImpl.builder()
                .catnip(mockNip)
                .guildIdAsLong(randomPositiveLong())
                .emojis(Arrays.asList(customEmoji(mockNip), customEmoji(mockNip)))
                .build();
        
        testEntity(emojiUpdate);
    }
    
    @Test
    void gatewayGuildBan() {
        final Catnip mockNip = mockNip();
        final GatewayGuildBan gatewayGuildBan = GatewayGuildBanImpl.builder()
                .catnip(mockNip)
                .guildIdAsLong(randomPositiveLong())
                .user(user(mockNip))
                .build();
        
        testEntity(gatewayGuildBan);
    }
    
    @Test
    void gatewayInfo() {
        final GatewayInfo gatewayInfo = GatewayInfoImpl.builder()
                .catnip(mockNip())
                .url(url())
                .shards(42)
                .totalSessions(1337)
                .remainingSessions(256)
                .resetAfter(TimeUnit.HOURS.toMillis(3))
                .valid(ThreadLocalRandom.current().nextBoolean())
                .build();
        
        testEntity(gatewayInfo);
    }
    
    @Test
    void groupDmChannel() {
        final Catnip mocknip = mockNip();
        final GroupDMChannel groupDMChannel = GroupDMChannelImpl.builder()
                .catnip(mocknip)
                .idAsLong(randomPositiveLong())
                .recipients(Collections.singletonList(user(mocknip)))
                .icon(imageUrl())
                .ownerIdAsLong(randomPositiveLong())
                .applicationIdAsLong(randomPositiveLong())
                .build();
        
        testEntity(groupDMChannel);
    }
    
    @Test
    void guildBan() {
        final Catnip mockNip = mockNip();
        final GuildBan guildBan = GuildBanImpl.builder()
                .catnip(mockNip)
                .reason("posting bad memes")
                .user(user(mockNip))
                .build();
        
        testEntity(guildBan);
    }
    
    @Test
    void guildEmbed() {
        final GuildEmbed guildEmbed = GuildEmbedImpl.builder()
                .catnip(mockNip())
                .channelIdAsLong(randomPositiveLong())
                .enabled(ThreadLocalRandom.current().nextBoolean())
                .build();
        
        testEntity(guildEmbed);
    }
    
    @Test
    void guild() {
        final Guild guild = GuildImpl.builder()
                .catnip(mockNip())
                .idAsLong(randomPositiveLong())
                .name("Best Guild EUW")
                .icon(imageUrl())
                .splash(imageUrl())
                .owned(ThreadLocalRandom.current().nextBoolean())
                .ownerIdAsLong(randomPositiveLong())
                .permissions(EnumSet.allOf(Permission.class))
                .region("Sealand")
                .afkChannelIdAsLong(randomPositiveLong())
                .afkTimeout(60)
                .embedEnabled(ThreadLocalRandom.current().nextBoolean())
                .embedChannelIdAsLong(randomPositiveLong())
                .verificationLevel(random(VerificationLevel.values()))
                .defaultMessageNotifications(random(NotificationLevel.values()))
                .explicitContentFilter(random(ContentFilterLevel.values()))
                .features(Arrays.asList(randomPositiveLongAsString(), randomPositiveLongAsString()))
                .mfaLevel(random(MFALevel.values()))
                .applicationIdAsLong(randomPositiveLong())
                .widgetEnabled(ThreadLocalRandom.current().nextBoolean())
                .widgetChannelIdAsLong(randomPositiveLong())
                .systemChannelIdAsLong(randomPositiveLong())
                .joinedAt(OffsetDateTime.now().toString())
                .large(ThreadLocalRandom.current().nextBoolean())
                .unavailable(ThreadLocalRandom.current().nextBoolean())
                .maxPresences(100_000)
                .maxMembers(100_000)
                .vanityUrlCode(null)
                .description(null)
                .banner(null)
                .build();
        
        testEntity(guild);
    }
    
    @Test
    void invite() {
        final Catnip mocknip = mockNip();
        final Invite invite = InviteImpl.builder()
                .catnip(mocknip)
                .code("asdfg")
                .inviter(inviter(mocknip))
                .guild(inviteGuild(mocknip))
                .channel(inviteChannel(mocknip))
                .approximatePresenceCount(123)
                .approximateMemberCount(456)
                .build();
    }
    
    @Test
    void member() {
        testEntity(member(mockNip()));
    }
    
    @Test
    void memberPruneInfo() {
        final MemberPruneInfo memberPruneInfo = MemberPruneInfoImpl.builder()
                .catnip(mockNip())
                .deleteMemberDays(7)
                .removedMembersCount(1024)
                .build();
        
        testEntity(memberPruneInfo);
    }
    
    @Test
    void messageDeleteInfo() {
        final MessageDeleteInfo messageDeleteInfo = MessageDeleteInfoImpl.builder()
                .catnip(mockNip())
                .channelIdAsLong(randomPositiveLong())
                .deletedMessagesCount(2048)
                .build();
        
        testEntity(messageDeleteInfo);
    }
    
    @Test
    void messageEmbedUpdate() {
        final Catnip mockNip = mockNip();
        final MessageEmbedUpdate messageEmbedUpdate = MessageEmbedUpdateImpl.builder()
                .catnip(mockNip)
                .idAsLong(randomPositiveLong())
                .guildIdAsLong(randomPositiveLong())
                .channelIdAsLong(randomPositiveLong())
                .embeds(Arrays.asList(embed(mockNip), embed(mockNip)))
                .build();
        
        testEntity(messageEmbedUpdate);
    }
    
    @Test
    void message() {
        final Catnip mockNip = mockNip();
        
        final Message message = MessageImpl.builder()
                .catnip(mockNip)
                .idAsLong(randomPositiveLong())
                .channelIdAsLong(randomPositiveLong())
                .author(user(mockNip))
                .content("Woah! easy there.")
                .timestamp(OffsetDateTime.now().toString())
                .editedTimestamp(OffsetDateTime.now().toString())
                .tts(ThreadLocalRandom.current().nextBoolean())
                .mentionsEveryone(ThreadLocalRandom.current().nextBoolean())
                .mentionedUsers(Arrays.asList(user(mockNip), user(mockNip)))
                .mentionedRoleIds(List.of(randomPositiveLongAsString(), randomPositiveLongAsString(),
                        randomPositiveLongAsString(), randomPositiveLongAsString()))
                .attachments(Arrays.asList(attachment(mockNip), attachment(mockNip)))
                .embeds(Arrays.asList(embed(mockNip), embed(mockNip)))
                .reactions(Arrays.asList(reaction(mockNip), reaction(mockNip)))
                .nonce("this is a nonce")
                .pinned(ThreadLocalRandom.current().nextBoolean())
                .webhookIdAsLong(randomPositiveLong())
                .type(random(MessageType.values()))
                .member(member(mockNip))
                .guildIdAsLong(randomPositiveLong())
                .build();
        
        testEntity(message);
    }
    
    @Test
    void newsChannel() {
        final Catnip mockNip = mockNip();
        final NewsChannel newsChannel = NewsChannelImpl.builder()
                .catnip(mockNip)
                .idAsLong(randomPositiveLong())
                .name("This is a text channel")
                .guildIdAsLong(randomPositiveLong())
                .position(2)
                .parentIdAsLong(randomPositiveLong())
                .overrides(Arrays.asList(permissionOverride(mockNip), permissionOverride(mockNip)))
                .topic("No Anime Allowed")
                .nsfw(ThreadLocalRandom.current().nextBoolean())
                .build();
        
        testEntity(newsChannel);
    }
    
    @Test
    void overrideUpdateInfo() {
        final OverrideUpdateInfo overrideUpdateInfo = OverrideUpdateInfoImpl.builder()
                .roleName("This is a role")
                .overrideType(random(OverrideType.values()))
                .overriddenEntityIdAsLong(randomPositiveLong())
                .build();
        
        testEntity(overrideUpdateInfo);
    }
    
    @Test
    void partialGuild() {
        final PartialGuild partialGuild = PartialGuildImpl.builder()
                .catnip(mockNip())
                .idAsLong(randomPositiveLong())
                .name("Best partial guild EUW")
                .icon(imageUrl())
                .owned(ThreadLocalRandom.current().nextBoolean())
                .permissions(EnumSet.allOf(Permission.class))
                .build();
        
        testEntity(partialGuild);
    }
    
    @Test
    void partialMember() {
        final Catnip mockNip = mockNip();
        final PartialMember partialMember = PartialMemberImpl.builder()
                .catnip(mockNip)
                .user(user(mockNip))
                .guildIdAsLong(randomPositiveLong())
                .roleIds(new HashSet<>(Arrays.asList(randomPositiveLongAsString(), randomPositiveLongAsString())))
                .nick("Nik")
                .build();
        
        testEntity(partialMember);
    }
    
    @Test
    void partialRole() {
        final PartialRole partialRole = PartialRoleImpl.builder()
                .catnip(mockNip())
                .idAsLong(randomPositiveLong())
                .guildIdAsLong(randomPositiveLong())
                .build();
        
        testEntity(partialRole);
    }
    
    @Test
    void permissionOverride() {
        testEntity(permissionOverride(mockNip()));
    }
    
    @Test
    void presence() {
        final Catnip mockNip = mockNip();
        final Presence presence = PresenceImpl.builder()
                .catnip(mockNip)
                .status(random(OnlineStatus.values()))
                .activity(activity(mockNip))
                .mobileStatus(random(OnlineStatus.values()))
                .webStatus(random(OnlineStatus.values()))
                .build();
        
        testObject(presence);
    }
    
    @Test
    void presenceUpdate() {
        final Catnip mockNip = mockNip();
        final PresenceUpdate presenceUpdate = PresenceUpdateImpl.builder()
                .catnip(mockNip)
                .idAsLong(randomPositiveLong())
                .guildIdAsLong(randomPositiveLong())
                .status(random(OnlineStatus.values()))
                .activity(activity(mockNip))
                .roles(new HashSet<>(Arrays.asList(randomPositiveLongAsString(), randomPositiveLongAsString())))
                .nick("xXx_Ch1cksT3rm1n4t0r_69_xXx")
                .mobileStatus(random(OnlineStatus.values()))
                .webStatus(random(OnlineStatus.values()))
                .desktopStatus(random(OnlineStatus.values()))
                .build();
        
        testEntity(presenceUpdate);
    }
    
    @Test
    void reactionUpdate() {
        final Catnip mocknip = mockNip();
        final ReactionUpdate reactionUpdate = ReactionUpdateImpl.builder()
                .catnip(mocknip)
                .userId(randomPositiveLongAsString())
                .channelId(randomPositiveLongAsString())
                .messageId(randomPositiveLongAsString())
                .guildId(randomPositiveLongAsString())
                .emoji(customEmoji(mocknip))
                .build();
        
        testEntity(reactionUpdate);
    }
    
    @Test
    void ready() {
        final Catnip mockNip = mockNip();
        final Ready ready = ReadyImpl.builder()
                .catnip(mockNip)
                .version(3)
                .user(user(mockNip))
                .guilds(new HashSet<>(Arrays.asList(unavailableGuild(mockNip), unavailableGuild(mockNip))))
                .build();
        
        testEntity(ready);
    }
    
    @Test
    void resumed() {
        final Resumed resumed = ResumedImpl.builder()
                .catnip(mockNip())
                .trace(Arrays.asList(randomPositiveLongAsString(), randomPositiveLongAsString()))
                .build();
        
        testEntity(resumed);
    }
    
    @Test
    void role() {
        final Role role = RoleImpl.builder()
                .catnip(mockNip())
                .idAsLong(randomPositiveLong())
                .guildIdAsLong(randomPositiveLong())
                .name("This is a role")
                .color(Color.blue.getRGB())
                .hoist(ThreadLocalRandom.current().nextBoolean())
                .position(6)
                .permissionsRaw(256)
                .managed(ThreadLocalRandom.current().nextBoolean())
                .mentionable(ThreadLocalRandom.current().nextBoolean())
                .build();
        
        testEntity(role);
    }
    
    @Test
    void storeChannel() {
        final Catnip mockNip = mockNip();
        final StoreChannel storeChannel = StoreChannelImpl.builder()
                .catnip(mockNip)
                .idAsLong(randomPositiveLong())
                .name("This is a store channel")
                .guildIdAsLong(randomPositiveLong())
                .nsfw(false)
                .overrides(Arrays.asList(permissionOverride(mockNip), permissionOverride(mockNip)))
                .parentIdAsLong(randomPositiveLong())
                .position(2)
                .build();
        
        testEntity(storeChannel);
    }
    
    @Test
    void textChannel() {
        final Catnip mockNip = mockNip();
        final TextChannel textChannel = TextChannelImpl.builder()
                .catnip(mockNip)
                .idAsLong(randomPositiveLong())
                .name("This is a text channel")
                .guildIdAsLong(randomPositiveLong())
                .position(2)
                .parentIdAsLong(randomPositiveLong())
                .overrides(Arrays.asList(permissionOverride(mockNip), permissionOverride(mockNip)))
                .topic("No Anime Allowed")
                .nsfw(ThreadLocalRandom.current().nextBoolean())
                .rateLimitPerUser(40)
                .build();
        
        testEntity(textChannel);
    }
    
    @Test
    void typingUser() {
        final TypingUser typingUser = TypingUserImpl.builder()
                .catnip(mockNip())
                .idAsLong(randomPositiveLong())
                .channelIdAsLong(randomPositiveLong())
                .guildIdAsLong(randomPositiveLong())
                .timestamp(System.currentTimeMillis())
                .build();
        
        testEntity(typingUser);
    }
    
    @Test
    void unavailableGuild() {
        final UnavailableGuild unavailableGuild = UnavailableGuildImpl.builder()
                .catnip(mockNip())
                .idAsLong(randomPositiveLong())
                .unavailable(ThreadLocalRandom.current().nextBoolean())
                .build();
        
        testEntity(unavailableGuild);
    }
    
    @Test
    void unicodeEmoji() {
        final UnicodeEmoji unicodeEmoji = UnicodeEmojiImpl.builder()
                .catnip(mockNip())
                .name("thonk")
                .requiresColons(ThreadLocalRandom.current().nextBoolean())
                .build();
        
        testEntity(unicodeEmoji);
    }
    
    @Test
    void userDMChannel() {
        final UserDMChannel userDMChannel = UserDMChannelImpl.builder()
                .catnip(mockNip())
                .idAsLong(randomPositiveLong())
                .userIdAsLong(randomPositiveLong())
                .build();
        
        testEntity(userDMChannel);
    }
    
    @Test
    void user() {
        testEntity(user(mockNip()));
    }
    
    @Test
    void channel() {
        final Catnip mockNip = mockNip();
        final Channel channel = VoiceChannelImpl.builder()
                .catnip(mockNip)
                .idAsLong(randomPositiveLong())
                .name("This is a voice channel")
                .guildIdAsLong(randomPositiveLong())
                .position(1)
                .parentIdAsLong(randomPositiveLong())
                .overrides(Arrays.asList(permissionOverride(mockNip), permissionOverride(mockNip)))
                .bitrate(64)
                .userLimit(20)
                .build();
        testEntity(channel);
    }
    
    @Test
    void voiceChannel() {
        final Catnip mockNip = mockNip();
        final VoiceChannel voiceChannel = VoiceChannelImpl.builder()
                .catnip(mockNip)
                .idAsLong(randomPositiveLong())
                .name("This is a voice channel")
                .guildIdAsLong(randomPositiveLong())
                .position(1)
                .parentIdAsLong(randomPositiveLong())
                .overrides(Arrays.asList(permissionOverride(mockNip), permissionOverride(mockNip)))
                .bitrate(64)
                .userLimit(20)
                .build();
        
        testEntity(voiceChannel);
    }
    
    @Test
    void voiceRegion() {
        final VoiceRegion voiceRegion = VoiceRegionImpl.builder()
                .catnip(mockNip())
                .id(randomPositiveLongAsString())
                .name("This is a voice region")
                .vip(ThreadLocalRandom.current().nextBoolean())
                .optimal(ThreadLocalRandom.current().nextBoolean())
                .deprecated(ThreadLocalRandom.current().nextBoolean())
                .custom(ThreadLocalRandom.current().nextBoolean())
                .build();
        
        testEntity(voiceRegion);
    }
    
    @Test
    void voiceServerUpdate() {
        final VoiceServerUpdate voiceServerUpdate = VoiceServerUpdateImpl.builder()
                .catnip(mockNip())
                .guildIdAsLong(randomPositiveLong())
                .token("this is a top secret token")
                .endpoint(url())
                .build();
        
        testEntity(voiceServerUpdate);
    }
    
    @Test
    void voiceState() {
        final VoiceState voiceState = VoiceStateImpl.builder()
                .catnip(mockNip())
                .guildIdAsLong(randomPositiveLong())
                .channelIdAsLong(randomPositiveLong())
                .userIdAsLong(randomPositiveLong())
                .sessionId(randomPositiveLongAsString())
                .deaf(ThreadLocalRandom.current().nextBoolean())
                .mute(ThreadLocalRandom.current().nextBoolean())
                .selfDeaf(ThreadLocalRandom.current().nextBoolean())
                .selfMute(ThreadLocalRandom.current().nextBoolean())
                .suppress(ThreadLocalRandom.current().nextBoolean())
                .build();
        
        testEntity(voiceState);
    }
    
    @Test
    void webhook() {
        testEntity(webhook(mockNip()));
    }
    
    @Test
    void webhooksUpdate() {
        final WebhooksUpdate webhooksUpdate = WebhooksUpdateImpl.builder()
                .catnip(mockNip())
                .channelIdAsLong(randomPositiveLong())
                .guildIdAsLong(randomPositiveLong())
                .build();
        
        testEntity(webhooksUpdate);
    }
    // below are methods helpful for generating entities in this test file
    
    private Catnip mockNip() {
        return Mockito.mock(Catnip.class);
    }
    
    private long randomPositiveLong() {
        return ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
    }
    
    private String randomPositiveLongAsString() {
        return Long.toUnsignedString(randomPositiveLong());
    }
    
    private String url() {
        return "https://http.cat";
    }
    
    private String imageUrl() {
        return "https://http.cat/500";
    }
    
    private <T extends Enum<T>> T random(final T[] values) {
        return values[ThreadLocalRandom.current().nextInt(values.length)];
    }
    
    private Activity activity(final Catnip catnip) {
        return ActivityImpl.builder()
                .name("Waifu Simulator")
                .type(random(ActivityType.values()))
                .build();
    }
    
    private ApplicationOwner applicationOwner(final Catnip catnip) {
        return ApplicationOwnerImpl.builder()
                .catnip(catnip)
                .avatar("kotlin4lyfe.jpeg")
                .bot(ThreadLocalRandom.current().nextBoolean())
                .discriminator("0001")
                .idAsLong(randomPositiveLong())
                .username("Kotlin4Lyfe")
                .build();
    }
    
    private Attachment attachment(final Catnip catnip) {
        return AttachmentImpl.builder()
                .catnip(catnip)
                .idAsLong(randomPositiveLong())
                .fileName("neko.png")
                .size(256)
                .url(imageUrl())
                .proxyUrl(imageUrl())
                .height(200)
                .width(100)
                .build();
    }
    
    private AuditLogChange auditLogChange(final Catnip catnip) {
        return AuditLogChangeImpl.builder()
                .catnip(catnip)
                .newValue("This is a new value")
                .oldValue("This is an old value")
                .key("This is a key")
                .build();
    }
    
    private CustomEmoji customEmoji(final Catnip catnip) {
        return CustomEmojiImpl.builder()
                .catnip(catnip)
                .idAsLong(randomPositiveLong())
                .guildIdAsLong(randomPositiveLong())
                .name("SmilingBlackGuyWithQuestionMarks")
                .roles(Arrays.asList(randomPositiveLongAsString(), randomPositiveLongAsString()))
                .user(user(catnip))
                .requiresColons(ThreadLocalRandom.current().nextBoolean())
                .managed(ThreadLocalRandom.current().nextBoolean())
                .animated(ThreadLocalRandom.current().nextBoolean())
                .build();
    }
    
    private Embed embed(final Catnip catnip) {
        return EmbedImpl.builder()
                .title("This is an embed")
                .type(random(EmbedType.values()))
                .description("This is a description")
                .url(url())
                .timestamp(OffsetDateTime.now().toString())
                .color(Color.red.getRGB())
                .footer(FooterImpl.builder()
                        .text("This is a footer")
                        .iconUrl(url())
                        .proxyIconUrl(url())
                        .build())
                .image(ImageImpl.builder()
                        .url(imageUrl())
                        .proxyUrl(imageUrl())
                        .height(500)
                        .width(500)
                        .build())
                .thumbnail(ThumbnailImpl.builder()
                        .url(url())
                        .proxyUrl(url())
                        .height(400)
                        .width(400)
                        .build())
                .video(VideoImpl.builder()
                        .url(url())
                        .height(300)
                        .width(300)
                        .build())
                .provider(ProviderImpl.builder()
                        .name("This is a name")
                        .url(url())
                        .build())
                .author(AuthorImpl.builder()
                        .name("B1nzy")
                        .url(url())
                        .iconUrl(imageUrl())
                        .proxyIconUrl(imageUrl())
                        .build())
                .fields(Arrays.asList(field(catnip), field(catnip)))
                .build();
    }
    
    private Field field(final Catnip catnip) {
        return FieldImpl.builder()
                .name("This is a field name")
                .value("This is a field value")
                .inline(ThreadLocalRandom.current().nextBoolean())
                .build();
    }
    
    private InviteChannel inviteChannel(final Catnip catnip) {
        return InviteChannelImpl.builder()
                .catnip(catnip)
                .idAsLong(randomPositiveLong())
                .name("Channel McChannelface")
                .type(random(ChannelType.values()))
                .build();
    }
    
    private Inviter inviter(final Catnip catnip) {
        return InviterImpl.builder()
                .catnip(catnip)
                .idAsLong(randomPositiveLong())
                .username("Invity McInviterface")
                .discriminator("9999")
                .avatar(imageUrl())
                .build();
    }
    
    private InviteGuild inviteGuild(final Catnip catnip) {
        return InviteGuildImpl.builder()
                .catnip(catnip)
                .idAsLong(randomPositiveLong())
                .name("Guildy McGuildface")
                .icon(imageUrl())
                .splash(imageUrl())
                .features(Arrays.asList(randomPositiveLongAsString(), randomPositiveLongAsString()))
                .verificationLevel(random(VerificationLevel.values()))
                .build();
    }
    
    private Member member(final Catnip catnip) {
        return MemberImpl.builder()
                .catnip(catnip)
                .idAsLong(randomPositiveLong())
                .guildIdAsLong(randomPositiveLong())
                .nick("Nik")
                .roleIds(new HashSet<>(Arrays.asList(randomPositiveLongAsString(), randomPositiveLongAsString())))
                .joinedAt(OffsetDateTime.now().toString())
                .deaf(ThreadLocalRandom.current().nextBoolean())
                .mute(ThreadLocalRandom.current().nextBoolean())
                .build();
    }
    
    private PermissionOverride permissionOverride(final Catnip catnip) {
        return PermissionOverrideImpl.builder()
                .catnip(catnip)
                .idAsLong(randomPositiveLong())
                .type(random(OverrideType.values()))
                .allowRaw(8)
                .denyRaw(131072)
                .build();
    }
    
    private Reaction reaction(final Catnip catnip) {
        return ReactionImpl.builder()
                .count(42)
                .self(ThreadLocalRandom.current().nextBoolean())
                .emoji(customEmoji(catnip))
                .build();
    }
    
    private UnavailableGuild unavailableGuild(final Catnip catnip) {
        return UnavailableGuildImpl.builder()
                .catnip(catnip)
                .idAsLong(randomPositiveLong())
                .unavailable(ThreadLocalRandom.current().nextBoolean())
                .build();
    }
    
    private User user(final Catnip catnip) {
        return UserImpl.builder()
                .catnip(catnip)
                .avatar("banana.gif")
                .bot(ThreadLocalRandom.current().nextBoolean())
                .discriminator("0007")
                .idAsLong(randomPositiveLong())
                .username("SAMUEL L. IPSUM")
                .build();
    }
    
    private Role role(final Catnip catnip) {
        return RoleImpl.builder()
                .catnip(catnip)
                .idAsLong(randomPositiveLong())
                .guildIdAsLong(randomPositiveLong())
                .name("test")
                .color(0)
                .hoist(false)
                .position(0)
                .permissionsRaw(0)
                .managed(false)
                .mentionable(false)
                .build();
    }
    
    private Webhook webhook(final Catnip catnip) {
        return WebhookImpl.builder()
                .catnip(catnip)
                .avatar("bait.jpeg")
                .channelIdAsLong(randomPositiveLong())
                .guildIdAsLong(randomPositiveLong())
                .idAsLong(randomPositiveLong())
                .name("this is a webhook")
                .token("top secret token")
                .user(user(catnip))
                .build();
    }
    
    private OptionalEntryInfo optionalEntryInfo(final Catnip catnip) {
        return MemberPruneInfoImpl.builder()
                .catnip(catnip)
                .removedMembersCount(69)
                .deleteMemberDays(42)
                .build();
    }
}
