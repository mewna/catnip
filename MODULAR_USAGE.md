# Modular usage

catnip was designed to be used in a modular way. That is, catnip is MEANT to be
used in small pieces (for, say, microservices), or all in a single package. The
beauty of how it's designed is that **modular usage is exactly the same as "normal" usage**.
Everything can be done through the same catnip instance that you create!

## Shards-only usage

If you want to use catnip for shards only, then you don't need to change 
anything! Just keep using catnip the exact same way, and maybe register an
extension or two so that it'll be usable.

## REST-only usage

REST-only usage with catnip is a bit unusual, as there are two ways to do it:

1. Use the convenience methods on all the entity objects.
2. Send REST requests yourself and fill out all the parameters.

Either way, you still set up your catnip instance like normal, but with one 
major difference - **You do not call `catnip.connect()`**.

The first case is likely the more common one. As long as you have a working
entity cache, you can just keep using the convenience methods on the entity
objects as normal. Do note that a few things - like voice connections, member
chunking, ... - are unavailable in REST-only mode due to the lack of a gateway
connection. This also means that `catnip.on()` will have no effect.

The second case is a bit more complicated - You have to know what the REST 
methods you want to call are categorized as. catnip organizes REST methods in
the same order as the Discord API documentation, so when in doubt, just check
there to get an idea of the right direction to go. 

To use REST methods directly, you can simply do something like

```Java
catnip.rest().channel().sendMessage("channel id", "some message");
```

to send a message. Note thatcatnip does NOT document the REST methods, as the
Discord documentation should be good enough for the most part. If you find a
problem with our implementations, please
[open an issue](https://github.com/mewna/catnip/issues/new) about it.