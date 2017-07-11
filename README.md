# BC4D4J
Better Commands 4 Discord4J is an easy-to-use, flexible, configuration/annotation-based command framework specifically designed for [Discord4J](https://github.com/austinv11/Discord4J) written in Kotlin (but it can be used in Java!).

## Getting Started
Setting up BC4D4J is as simple as activating the module. Either add the **.jar** to your `modules` folder or add the following code to your program:
```kotlin
client.moduleLoader.loadModule(BC4D4J())
```
>If you're using the latter method, you must add BC4D4J to your CLASSPATH. The simplest way to do so is through a dependency management system such as [Maven](https://maven.apache.org/) or [Gradle](https://gradle.org/) and using the [jitpack.io](https://jitpack.io/#danthonywalker/bc4d4j) repository.

### MainCommand
Commands are represented either by a `MainCommandConfig` instance or an instance of a function that is annotated with `@MainCommand`.
```kotlin
val ping = MainCommandConfig(
    aliases = setOf("ping"),
    name = "ping",
    prefix = "~")
```
```kotlin
class PingCommand {
    @MainCommand(
        aliases = arrayOf("ping"),
        name = "ping",
        prefix = "~")
    fun ping(context: CommandContext) {
        // Executable code goes here
    }
}
```
Both of these examples represent a command that should be called when the message `~ping` is received by the bot.

However, in order for these to be useful, we have to register them first. Registration is as simple as invoking the following code:
```kotlin
// For annotation "containers"
client.configRegistry.register(PingCommand())
```
```kotlin
// For config based instances
client.configRegistry.register(ping, {
    // Executable code goes here
})
```

### SubCommand
What if we want to execute completely different code if one of the "arguments" for `ping` is `pong` (i.e. the message received by the client is `~ping pong`)? Luckily, BC4D4J makes it intuitive to define such routines.

```kotlin
val pong = SubCommandConfig(
    aliases = setOf("pong"),
    name = "pong")
```
```kotlin
@SubCommand(
    aliases = arrayOf("pong"),
    name = "pong")
```
>BC4D4J will allow you to define the annotation anywhere in your code. However, just like with the `MainCommand` example, the instance of that class must be registered as well **(the order of registration does not matter).**

Now we must change our `MainCommand` to the following.
```kotlin
val ping = MainCommandConfig(
    subCommands = setOf("pong"),
    ...
```
```kotlin
@MainCommand(
    subCommands = arrayOf("pong"),
    ...
```
Notice that the String we provided to `subCommands` is the same *case-sensitive* String as the `name` for our `SubCommand`.

BC4D4J also supports nested sub-commands meaning your command arguments can span infinitely (or until you reach Discord's message limit).

#### Hierarchy
BC4D4J handles and passes commands in a hierarchy where the hierarchy is a tree where the *root* is the `MainCommand` and its children are `SubCommands`. With each *level* being an argument for a `SubCommand`, BC4D4J will attempt to only invoke the **last** valid level in the hierarchy. A valid level, is where there are either 1 or more *valid* commands on that level. A valid command is defined in ***Command Properties***.

### ExceptionHandler
Sometimes, you don't want to liter your codebase with try-catch statements everywhere or, maybe, you want to separate your error logic from your "execution" logic. Luckily, BC4D4J provides a way to centralize exception management.

```kotlin
val handler = ExceptionHandlerConfig(name = "ping")
```
```kotlin
@ExceptionHandler(name = "ping")
fun pingExceptionHandler(context: ExceptionHandler) {
    // Error logic
}
```
Notice that the String we provided to `name` is the same String as the *cast-sensitive* String as the `name` for our `MainCommand` (this will also work for `SubCommand` instances!).

Additionally, BC4D4J will automatically log all exceptions for you whether or not an `ExceptionHandler` is registered or not.

## Scope
One of the biggest benefits of BC4D4J over other command frameworks is to restrict commands down to *scopes*. When creating a command config instance or when registering your annotation container, you have the option of defining a `Scope`. After defining a scope, commands will only be executed if they are *within* that `Scope`.

For example, what if a guild wants to define its own prefix, but you still want to provide a default global prefix?
```kotlin
val guildPing = MainCommandConfig(
    scope = Scope( /* IGuild instance */ )),
    // other config options
```
```kotlin
client.configRegistry(PingCommand(), Scope( /* IGuild instance */ ))
```
Now the **by default global** PingCommand will only be executed if its from the *IGuild*. This feature can be incredibly powerful if one utilizes the configuration-based commands over annotations (as those can provide much greater flexibility and control). Currently, a `Scope` can be defined **globally**, by **region**, by **guild**, by **role**, by **text channel**, by **voice channel**, or by **user**.

## ResultContext
Some commands may require some processing after it's executed. BC4D4J will automatically manage some of these common processes via a `ResultContext`.
```kotlin
client.configRegistry.register(config, {
    // Executable code goes here
    return ResultContext(
        // By default, the time unit is in SECONDS
        cooldown = CooldownContext(duration = 5),
        deleteMessage = true)
})
```
```kotlin
/* annotation */
fun command(context: CommandContext): ResultContext {
    // Executable code goes here
    return ResultContext(
        // By default, the time unit is in SECONDS
        cooldown = CooldownContext(duration = 5),
        deleteMessage = true)
}
```
The above tells BC4D4J that after processing the code to *attempt* to delete the message that triggered the command as well as to put a *cooldown* of 5 seconds. If the command is re-executed within those 5 seconds, it will throw an exception.