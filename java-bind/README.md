# BC4D4J Java Bind
This module provides convenient Java interfaces for the command functionality bindings provided by *bc4d4j-core*.

BC4D4J makes heavy use of Kotlin's [coroutine](https://kotlinlang.org/docs/reference/coroutines.html) mechanism to allow
commands to be processed in an efficient and parallel fashion to provide exceptional throughput and response.
Unfortunately, Kotlin coroutines do not interop with Java in an easy-to-use manner. Rather than sacrifice performance or
alienate Java users, this module bridges the complex gap between Kotlin's coroutines and Java while still allowing bot
owners to optionally take advantage of its parallel capabilities.

## Getting Started
Instead of implementing an `ArgumentFactory`, `CommandExecutor`, `CommandLimiter`, or `CommandRestrictor` directly;
implement their `Async` counterparts as provided by this module. For example, if you want to implement a
`CommandExecutor`, implement `AsyncCommandExecutor` instead. These async variants guarantee the same contracts as their
non-async counterparts with one notable exception, every single one requires the return of some
[CompletionStage](http://download.java.net/java/jdk9/docs/api/java/util/concurrent/CompletionStage.html).

```java
public class MyLimiter implements AsyncCommandLimiter {
    @Override
    public CompletionStage<Boolean> shouldLimit(final CommandContext context) {
        // If you do not know how to utilize a CompletionStage you may simply place code here as if it's any normal
        // method. It is imperative that you do not block the thread unless you wish to suffer throughput performance
        // across all commands. If possible, blocking code should be supplemented for CompletionStage solutions.

        // At the very minimum, something similar to this must be returned. Of course, if you know how to utilize a
        // CompletionStage then you may expand this to more complex and parallel friendly operations.
        return CompletableFuture.supplyAsync(() -> false);
    }
}
```
