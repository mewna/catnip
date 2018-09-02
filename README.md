# catnip

A Discord API wrapper in Java. Fully async / reactive, built on top of
[vert.x](https://vertx.io).

## Basic usage

This is the simplest possible bot you can make right now:

```Java
final Catnip catnip = new Catnip().token(System.getenv("TOKEN")).setup();
Catnip.eventBus().<Message>consumer(DiscordEvent.MESSAGE_CREATE, event -> {
    final Message msg = event.body();
    if(msg.getContent().equalsIgnoreCase("!ping")) {
        catnip.rest().createMessage(msg.getChannelId(), "pong!");
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
- Full REST API coverage (see `Rest`)
