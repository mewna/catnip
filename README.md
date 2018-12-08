# catnip

[![CircleCI](https://circleci.com/gh/mewna/catnip.svg?style=svg)](https://circleci.com/gh/mewna/catnip)
[![powered by potato](https://img.shields.io/badge/powered%20by-potato-%23db325c.svg)](https://mewna.com/)

A Discord API wrapper in Java. Fully async / reactive, built on top of
[vert.x](https://vertx.io).

We have a Discord! https://discord.gg/yeF2HpP

Licensed under the [BSD 3-Clause License](https://tldrlegal.com/license/bsd-3-clause-license-(revised)).

## Installation



If you want to keep up with the latest changes, get it on Jitpack here: 
https://jitpack.io/#mewna/catnip

## Basic usage

This is the simplest possible bot you can make right now:

```Java
final Catnip catnip = Catnip.catnip("your token goes here");
catnip.on(DiscordEvent.MESSAGE_CREATE, msg -> {
    if(msg.content().startsWith("!ping")) {
        msg.channel().sendMessage("pong!");
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
        msg.channel().sendMessage("pong!")
                .thenAccept(ping -> {
                    final long end = System.currentTimeMillis();
                    ping.edit("pong! (took " + (end - start) + "ms)");
                });
    }
});
catnip.startShards();
```

If you want to customize it more, look into the `CatnipOptions` class.

## Features

- Automatic sharding
- Proper support for REST / ratelimits, `RESUME`, ...

