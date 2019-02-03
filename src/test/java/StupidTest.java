import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.Entity;
import com.mewna.catnip.entity.message.Message;
import com.mewna.catnip.shard.DiscordEvent;
import io.vertx.core.json.JsonObject;

public class StupidTest {
    public static void main(String[] args) {
        Catnip catnip = Catnip.catnip("NTM1MTE0MDk2NzMxNzUwNDAx.Dy97fA.EOKQFmgysh0wxZC6Hfu-NR5rl28");
        catnip.connect();
        catnip.on(DiscordEvent.READY, System.out::println);
        catnip.on(DiscordEvent.MESSAGE_CREATE, msg -> {
            JsonObject messageJson = msg.toJson();
            Message newMsg = Entity.fromJson(catnip, Message.class, messageJson);
            System.out.println(newMsg.author().catnip());
        });
    }
}
