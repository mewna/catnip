# catnip

A Discord API wrapper in Java. Fully async / reactive, built on top of
[vert.x](https://vertx.io).

We have a Discord! https://discord.gg/kCsBCjK

## Basic usage

This is the simplest possible bot you can make right now:

```Java
final Catnip catnip = Catnip.catnip().token(System.getenv("TOKEN"));
catnip.eventBus().<Message>consumer(DiscordEvent.MESSAGE_CREATE, event -> {
    final Message msg = event.body();
    if(msg.content().equalsIgnoreCase("!ping")) {
        catnip.rest().channel().sendMessage(msg.channelId(), "pong!");
    }
});
catnip.startShards();
```

catnip returns `CompletableFuture`s from all REST methods. For example,
editing your ping message to include time it took to create the
message:

```Java
final Catnip catnip = Catnip.catnip().token(System.getenv("TOKEN"));
catnip.eventBus().<Message>consumer(DiscordEvent.MESSAGE_CREATE, event -> {
    final Message msg = event.body();
    if(msg.content().equalsIgnoreCase("!ping")) {
        final long start = System.currentTimeMillis();
        catnip.rest().channel().sendMessage(msg.channelId(), "pong!")
                .thenAccept(ping -> {
                    final long end = System.currentTimeMillis();
                    catnip.rest().channel().editMessage(msg.channelId(), ping.id(),
                            "pong! (took " + (end - start) + "ms)");
                });
    }
});
catnip.startShards();
```

## Features

- Automatic sharding
- Proper support for REST / ratelimits, `RESUME`, ...

## TODO

- Create entity classes for guilds, channels, ...
- Finish handling all `DISPATCH` events (see `DispatchEmitter`)
- Full REST API coverage (see `Rest`, #3)
