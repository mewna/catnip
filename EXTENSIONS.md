# Extensions

catnip supports *extensions* to itself. That is, it is possible to write code
at a higher level than the library itself, and hook into its internals to do
various things anyway. The primary use-case for writing extensions is things 
like custom external caching - an external cache extension can register its
cache worker with catnip with zero intervention from the end-user; all the user
has to do is load the extension.

If you've ever written code for Minecraft servers, you likely know about Bukkit
plugins. You can think of extensions sort-of like that - they let you 
un/register event listeners, option overrides, hooks, ... dynamically, so that
you can distribute your code in a single coherent package that can easily be 
used by end-users.

Extensions allow you to override some catnip options dynamically, as well as 
hook into the raw gateway / REST request-response cycle, so that you can inject
values into the JSON if needed. Since extensions can override the user-set
options dynamically, there is an option to log when this occurs -
`CatnipOptions#logExtensionOverrides`. Extensions are not able to override the
token or the override logging. 

## Using extensions

Using extensions is VERY easy:

1. Load the extension: `catnip.loadExtension(MyExtension.class);`.
2. Use the extension for something:
   `catnip.extension(MyExtension.class).doCoolStuff()`.

Note that some extensions may register their own event listeners. For example,
you could implement a generic, reusable command system as an extension, and 
then others could use it with just `catnip.loadExtension(CoolCommands.class);`.

## Example: Logging messages

This is a sample extension that logs every message that the bot receives.

```Java
import com.mewna.catnip.extension.AbstractExtension;
import com.mewna.catnip.shard.DiscordEvent;

public class MessageLoggingExtension extends AbstractExtension {
    public Scratch() {
        super("message logger");
    }
    
    @Override
    public void start() {
        on(DiscordEvent.MESSAGE_CREATE, msg -> System.out.println("Received message: " + msg.content()));
    }
}
```

## Example: Reduced caching

This is a sample extension that changes the cache flags automatically, to help
lower memory usage.

```Java
package my.cool.extension;

import com.mewna.catnip.cache.CacheFlag;
import com.mewna.catnip.extension.AbstractExtension;

import java.util.EnumSet;

/**
 * An extension that reduces memory usage by dropping voice states and game
 * statuses.
 */
public class ReducedCachingExtension extends AbstractExtension {
    public ReducedCachingExtension() {
        super("reduced caching extension");
    }
    
    @Override
    public void start() {
        injectOptions(opts -> {
            opts.cacheFlags(EnumSet.of(CacheFlag.DROP_VOICE_STATES, CacheFlag.DROP_GAME_STATUSES));
            return opts;
        });
    }
}
```

## Example: Raw gateway event logging

This is a sample extension that logs the raw events that Discord sends to the
bot, before any processing is done.

```Java
package my.cool.extension;

import com.mewna.catnip.extension.AbstractExtension;
import com.mewna.catnip.extension.hook.CatnipHook;
import com.grack.nanojson.JsonObject;

import javax.annotation.Nonnull;

public class GatewayLoggingExtension extends AbstractExtension {
    public GatewayLoggingExtension() {
        super("gateway logging extension");
    }
    
    @Override
    public void start() {
        registerHook(new CatnipHook() {
            @Override
            public JsonObject rawGatewayReceiveHook(@Nonnull final JsonObject json) {
                System.out.println("Got gateway event: " + json.encode());
                return json;
            }
        });
    }
}
```