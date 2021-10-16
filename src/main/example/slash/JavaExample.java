package slash;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.builder.command.CommandOptionBuilder;
import com.mewna.catnip.entity.interaction.InteractionResponseType;
import com.mewna.catnip.entity.interaction.command.ApplicationCommandInteraction;
import com.mewna.catnip.entity.interaction.command.ApplicationCommandOptionType;
import com.mewna.catnip.entity.interaction.command.ApplicationCommandType;
import com.mewna.catnip.entity.message.MessageOptions;
import com.mewna.catnip.shard.DiscordEvent;

import java.util.List;

public class JavaExample {
    public static void main(String[] args) {
        Catnip catnip = Catnip.catnip("your token goes here");
        
        catnip.rest().interaction().createGlobalApplicationCommand(
                ApplicationCommandType.CHAT_INPUT, "test", "my cool command",
                List.of(
                        new CommandOptionBuilder()
                                .name("option")
                                .description("my cool option")
                                .type(ApplicationCommandOptionType.STRING)
                                .required(false)
                                .build()
                )
        ).subscribe();
        
        catnip.observable(DiscordEvent.INTERACTION_CREATE).subscribe(interaction -> {
            if(interaction instanceof ApplicationCommandInteraction command) {
                if(command.data().name().equals("test")) {
                    catnip.rest().interaction().createInteractionInitialResponse(
                            InteractionResponseType.CHANNEL_MESSAGE_WITH_SOURCE,
                            command.id(), command.token(),
                            new MessageOptions().content("test response!")
                    );
                }
            }
        });
        
        catnip.connect();
    }
}