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
import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.channel.Category;
import com.mewna.catnip.entity.channel.ChannelPinsUpdate;
import com.mewna.catnip.entity.channel.Webhook;
import com.mewna.catnip.entity.guild.UnavailableGuild;
import com.mewna.catnip.entity.guild.audit.ActionType;
import com.mewna.catnip.entity.guild.audit.AuditLogChange;
import com.mewna.catnip.entity.guild.audit.AuditLogEntry;
import com.mewna.catnip.entity.guild.audit.OptionalEntryInfo;
import com.mewna.catnip.entity.impl.*;
import com.mewna.catnip.entity.impl.PresenceImpl.ActivityImpl;
import com.mewna.catnip.entity.message.BulkDeletedMessages;
import com.mewna.catnip.entity.message.BulkRemovedReactions;
import com.mewna.catnip.entity.misc.ApplicationInfo;
import com.mewna.catnip.entity.misc.ApplicationOwner;
import com.mewna.catnip.entity.user.Presence;
import com.mewna.catnip.entity.user.Presence.Activity;
import com.mewna.catnip.entity.user.Presence.ActivityType;
import com.mewna.catnip.entity.user.Presence.OnlineStatus;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.util.JsonPojoCodec;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by napster on 01.01.19.
 * <p>
 * Ensure that all our entities and the data they carry survives being serialized and deserialized through the eventbus.
 * <p>
 * Note: We can't use {@link Object#equals} of most entities because many of them only check the ids for being equal.
 */
class CodecTest {
    
    private <T> void test(final T entity) {
        final Catnip thisCatnip = mockNip();
        @SuppressWarnings("unchecked")
        final Class<T> entityClass = (Class<T>) entity.getClass();
        final JsonPojoCodec<T> codec = new JsonPojoCodec<>(thisCatnip, entityClass);
        final Buffer buffer = Buffer.buffer();
        codec.encodeToWire(buffer, entity);
        final T deserialized = codec.decodeFromWire(0, buffer);
        
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
                .rpcOrigins(Collections.emptyList())//TODO generate some
                .publicBot(true)
                .requiresCodeGrant(false)
                .owner(applicationOwner(mocknip))
                .build();
        
        test(applicationInfo);
    }
    
    @Test
    void applicationOwner() {
        test(applicationOwner(mockNip()));
    }

    @Test
    void auditLogChange() {
        final AuditLogChange auditLogChange = AuditLogChangeImpl.builder()
                .catnip(mockNip())
                .newValue("newValue")
                .oldValue("oldValue")
                .key("key")
                .build();
        
        test(auditLogChange);
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
                .type(ActionType.MEMBER_PRUNE)
                .changes(Collections.emptyList())  //TODO generate
                .webhook(webhook(mocknip))
                .build();
        
        test(auditLogEntry);
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
        
        test(bulkDeletedMessages);
    }
    
    @Test
    void bulkRemovedReactions() {
        final BulkRemovedReactions bulkRemovedReactions = BulkRemovedReactionsImpl.builder()
                .catnip(mockNip())
                .channelId(randomPositiveLongAsString())
                .messageId(randomPositiveLongAsString())
                .guildId(randomPositiveLongAsString())
                .build();
        
        test(bulkRemovedReactions);
    }
    
    @Test
    void category() {
        final Category category = CategoryImpl.builder()
                .catnip(mockNip())
                .idAsLong(randomPositiveLong())
                .name("this is a category")
                .guildIdAsLong(randomPositiveLong())
                .position(4)
                .parentIdAsLong(randomPositiveLong())
                .overrides(Collections.emptyList()) //TODO generate
                .build();
        
        test(category);
    }
    
    @Test
    void channelPinsUpdate() {
        final ChannelPinsUpdate channelPinsUpdate = ChannelPinsUpdateImpl.builder()
                .catnip(mockNip())
                .channelIdAsLong(randomPositiveLong())
                .lastPinTimestamp(OffsetDateTime.now().toString())
                .build();
        
        test(channelPinsUpdate);
    }
    
    //TODO CreatedInvite
    //TODO CustomEmoji
    //TODO DeletedMessage
    //TODO Embed
    //TODO EmojiUpdate
    //TODO GatewayGuildBan
    //TODO GatewayInfo
    //TODO GroupDMChannel
    //TODO GuildBan
    //TODO GuildEmbed
    //TODO Guild
    //TODO Invite
    //TODO Member
    //TODO MemberPruneInfo
    //TODO MemberDeleteInfo
    //TODO MessageEmbedUpdate
    //TODO Message
    //TODO OverrideUpdateInfo
    //TODO PartialGuild
    //TODO PartialMember
    //TODO PartialRole
    //TODO PermissionOverride
    
    @Test
    void presence() {
        final Activity activity = ActivityImpl.builder()
                .name("Waifu Simulator")
                .type(ActivityType.PLAYING)
                .build();
        final Presence presence = PresenceImpl.builder()
                .catnip(mockNip())
                .status(OnlineStatus.DND)
                .activity(activity)
                .mobileStatus(OnlineStatus.ONLINE)
                .webStatus(OnlineStatus.OFFLINE)
                .build();
        
        test(presence);
    }
    
    //TODO PresenceUpdate
    //TODO ReactionUpdate
    //TODO Ready
    //TODO Resumed
    //TODO Role
    //TODO TextChannel
    //TODO TypingUser
    
    @Test
    void unavailableGuild() {
        final UnavailableGuild unavailableGuild = UnavailableGuildImpl.builder()
                .catnip(mockNip())
                .idAsLong(randomPositiveLong())
                .unavailable(false)
                .build();
    
        test(unavailableGuild);
    }
    
    //TODO UnicodeEmoji
    //TODO UserDMChannel
    //TODO User
    //TODO VoiceChannel
    //TODO VoiceRegion
    //TODO VoiceServerUpdate
    //TODO VoiceState
    //TODO Webhook
    //TODO WebhooksUpdate
    
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
    
    private User user(final Catnip catnip) {
        return UserImpl.builder()
                .catnip(catnip)
                .avatar("banana.gif")
                .bot(false)
                .discriminator("0007")
                .idAsLong(randomPositiveLong())
                .username("SAMUEL L. IPSUM")
                .build();
    }
    
    private ApplicationOwner applicationOwner(final Catnip catnip) {
        return ApplicationOwnerImpl.builder()
                .catnip(catnip)
                .avatar("kotlin4lyfe.jpeg")
                .bot(true)
                .discriminator("0001")
                .idAsLong(randomPositiveLong())
                .username("Kotlin4Lyfe")
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
