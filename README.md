# catnip

[![CircleCI](https://circleci.com/gh/mewna/catnip.svg?style=svg)](https://circleci.com/gh/mewna/catnip)
[![powered by potato](https://img.shields.io/badge/powered%20by-potato-%23db325c.svg)](https://mewna.com/)

A Discord API wrapper in Java. Fully async / reactive, built on top of
[vert.x](https://vertx.io).

We have a Discord! https://discord.gg/QTZajS

## Installation

Run `mvn clean test install`

If this is too hard for you and / or you use gradle, get it on Jitpack here: https://jitpack.io/#mewna/catnip

## Basic usage

This is the simplest possible bot you can make right now:

```Java
final Catnip catnip = Catnip.catnip("your token goes here");
catnip.on(DiscordEvent.MESSAGE_CREATE, msg -> {
    if(msg.content().startsWith("!ping")) {
        catnip.rest().channel().sendMessage(msg.channelId(), "pong!");
    }
});
catnip.startShards();
```

catnip returns `CompletionStage`s from all REST methods. For example,
editing your ping message to include time it took to create the
message:

```Java
final Catnip catnip = Catnip.catnip("your token goes here");
catnip.on(DiscordEvent.MESSAGE_CREATE, msg -> {
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

If you want to customize it more, look into the `CatnipOptions` class.

## Features

- Automatic sharding
- Proper support for REST / ratelimits, `RESUME`, ...

## TODO

- Full REST API coverage (see `Rest`, #3)
