# Caching

Caching in catnip is about as customizable as it gets - you can mess with every
little detail of how it works if you want, or just stick with the 
tried-and-true cache implementations and maybe report some bugs. For some use
cases it might be important to have a partial cache - or no cache at all. Thus,
catnip allows full customization of caching. 

## Things to know

Before writing your own cache implementation, you should be aware of a few
important things:

- catnip caches are **asynchronous**. Your usage of them should NOT block the
  event loop, as much as possible. This is done so that ex. people who store
  caches externally in something like redis can avoid blocking the event loop.
- The easiest base to start from for a customizable cache is 
  CustomizableEntityCache, which stubs out all the cache methods, which will
  help you keep your implementation clean.
- If you want to be able to use the convenience methods on entity objects, you
  **must** provide support for all the cache methods required.
- catnip does use a small portion of the cache internally. Specifically, the
  `DoubleEvent`s (`GUILD_UPDATE`, `USER_UPDATE`, `PRESENCE_UPDATE`, ...) all
  use the cache to provide the information used in them. If you don't want to
  store the information used in them, you should stub out the methods to 
  provide an acceptable form of `null`. You can find all the events that behave
  like this in the `DiscordEvent` class.