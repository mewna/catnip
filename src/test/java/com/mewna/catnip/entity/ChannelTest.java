package com.mewna.catnip.entity;

import com.mewna.catnip.entity.channel.Channel;
import com.mewna.catnip.entity.impl.channel.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChannelTest {
    @Test
    public void testIsGuildMessageChannel() {
        // These five are all expected to be true
        assertTrue(new TextChannelImpl().isGuildMessageChannel());
        assertTrue(new NewsChannelImpl().isGuildMessageChannel());
        assertTrue(new ThreadChannelImpl().type(Channel.ChannelType.NEWS_THREAD).isGuildMessageChannel());
        assertTrue(new ThreadChannelImpl().type(Channel.ChannelType.PUBLIC_THREAD).isGuildMessageChannel());
        assertTrue(new ThreadChannelImpl().type(Channel.ChannelType.PRIVATE_THREAD).isGuildMessageChannel());
        
        // These are guild channels but not message channels
        assertFalse(new VoiceChannelImpl().isGuildMessageChannel());
        assertFalse(new StageChannelImpl().isGuildMessageChannel());
        
        // These are message channels but not guild channels
        assertFalse(new UserDMChannelImpl().isGuildMessageChannel());
        assertFalse(new GroupDMChannelImpl().isGuildMessageChannel());
    }
}
