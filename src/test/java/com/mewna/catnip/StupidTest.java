package com.mewna.catnip;

import com.mewna.catnip.shard.DiscordEvent;

public class StupidTest {
    
    public static void main(String[] args) {
        Catnip.catnipAsync("NTM1MTE0MDk2NzMxNzUwNDAx.D0WHwQ.9_hBq_ZtSeaDlF8bAI5lGycH9TI").thenAccept(catnip -> {
            catnip.connect();
            catnip.on(DiscordEvent.READY, ready -> System.out.println("READY"));
            catnip.on(DiscordEvent.MESSAGE_CREATE, msg -> {
               msg.guild().ban(msg.guild().members().getById(416902379598774273L), "HUI", 7);
            });
        });
    }
}
