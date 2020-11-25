/*
 * Copyright (c) 2020 amy, All rights reserved.
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

package com.mewna.catnip.rest.handler;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.Env;
import com.mewna.catnip.entity.message.Message;
import com.mewna.catnip.entity.message.MessageOptions;
import com.mewna.catnip.rest.ResponseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author amy
 * @since 10/15/20.
 */
@DisabledIfSystemProperty(named = "restTests", matches = "false")
@SuppressWarnings("ResultOfMethodCallIgnored")
public class RestChannelTest {
    public static final String CAKE = "\uD83C\uDF70";
    private static final Catnip catnip = Catnip.catnip(Env.DISCORD_TOKEN);
    
    @Test
    public void messageRoutesTest() {
        final var me = Long.toUnsignedString(Catnip.parseIdFromToken(Env.DISCORD_TOKEN));
        
        // Message creation
        final var message = catnip.rest().channel().createMessage(Env.TEST_CHANNEL, "test").blockingGet();
        assertEquals(Env.TEST_CHANNEL, message.channelId(), "channels not equal");
        assertEquals("test", message.content(), "contents not equal");
        assertEquals(me, message.author().id(), "author snowflakes not equal");
        assertNotNull(message.id());
        
        // Message edits
        final var edited = catnip.rest().channel().editMessage(Env.TEST_CHANNEL, message.id(), "test 2").blockingGet();
        assertEquals("test 2", edited.content(), "edit contents not equal");
        
        // Reaction add
        catnip.rest().channel().addReaction(Env.TEST_CHANNEL, message.id(), CAKE).blockingAwait();
        final var reactor = catnip.rest().channel().getReactions(Env.TEST_CHANNEL, message.id(), CAKE).limit(1).fetch().blockingFirst();
        assertEquals(me, reactor.id(), "reactor ids not equal");
        
        // Reaction remove
        catnip.rest().channel().deleteUserReaction(Env.TEST_CHANNEL, message.id(), me, CAKE).blockingAwait();
        assertThrows(NoSuchElementException.class, () -> catnip.rest().channel()
                        .getReactions(Env.TEST_CHANNEL, message.id(), CAKE).limit(1).fetch().blockingFirst(),
                "getReactions didn't throw");
        
        // Message deletion
        catnip.rest().channel().deleteMessage(Env.TEST_CHANNEL, message.id()).blockingAwait();
        assertThrows(ResponseException.class, () -> {
            // We can't use blockingSubscribe() here because we need the
            // exception to be reraised.
            catnip.rest().channel().getMessage(Env.TEST_CHANNEL, message.id()).blockingGet();
        }, "getMessage didn't throw ResponseException");
        
        // Inline replies
        final Message replyTest = catnip.rest().channel().createMessage(Env.TEST_CHANNEL, "test").blockingGet();
        final Message reply = catnip.rest().channel().createMessage(Env.TEST_CHANNEL,
                new MessageOptions()
                        .content("reply test")
                        .pingReply(true)
                        .referenceMessage(replyTest.asReference()))
                .blockingGet();
        assertTrue(reply.mentionedUsers().stream().anyMatch(u -> u.idAsLong() == replyTest.author().idAsLong()));
        
        catnip.rest().channel().deleteMessage(Env.TEST_CHANNEL, replyTest.id()).blockingAwait();
        catnip.rest().channel().deleteMessage(Env.TEST_CHANNEL, reply.id()).blockingAwait();
        
        final Message replyNoPingTest = catnip.rest().channel().createMessage(Env.TEST_CHANNEL, "test").blockingGet();
        final Message noPingReply = catnip.rest().channel().createMessage(Env.TEST_CHANNEL,
                new MessageOptions()
                        .content("reply test")
                        .pingReply(false)
                        .referenceMessage(replyNoPingTest.asReference()))
                .blockingGet();
        
        assertTrue(noPingReply.mentionedUsers().isEmpty());
        catnip.rest().channel().deleteMessage(Env.TEST_CHANNEL, noPingReply.id()).blockingAwait();
        catnip.rest().channel().deleteMessage(Env.TEST_CHANNEL, replyNoPingTest.id()).blockingAwait();
        
        final Message convenienceTestPing = catnip.rest().channel().createMessage(Env.TEST_CHANNEL, "test").blockingGet();
        final Message conveniencePing = convenienceTestPing.reply("test", true).blockingGet();
        assertFalse(conveniencePing.mentionedUsers().isEmpty());
    
        final Message convenienceTestNoPing = catnip.rest().channel().createMessage(Env.TEST_CHANNEL, "test").blockingGet();
        final Message convenienceNoPing = convenienceTestNoPing.reply("test", false).blockingGet();
        assertTrue(convenienceNoPing.mentionedUsers().isEmpty());
    }
    
    @Test
    public void channelRoutesTest() {
        final var me = Long.toUnsignedString(Catnip.parseIdFromToken(Env.DISCORD_TOKEN));
        assertDoesNotThrow(() -> catnip.rest().channel().triggerTypingIndicator(Env.TEST_CHANNEL).blockingAwait(),
                "triggerTypingIndicator threw");
        final var channel = catnip.rest().channel().getChannelById(Env.TEST_CHANNEL).blockingGet().asGuildChannel();
        assertEquals(Env.TEST_CHANNEL, channel.id(), "channels not equal");
        assertEquals("sandbox", channel.name(), "channel names not equal");
        assertEquals(Env.TEST_GUILD, channel.guildId(), "channel not in test guild");
    }
}
