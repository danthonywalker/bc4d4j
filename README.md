# bc4d4j
Better Commands 4 Discord4J is an easy-to-use, flexible, annotation-based command framework specifically designed for [Discord4J](https://github.com/austinv11/Discord4J) written in Kotlin.

## Getting Started
Setting up BC4D4J is as simple as activating the module. Either add the **.jar** to your `modules` folder or add the following code to your program:
```kotlin
val client: IDiscordClient = ...
client.moduleLoader.loadModule(BC4D4J())
```
>If you're using the latter method, you must add BC4D4J to your CLASSPATH. The simplest way to do so is through a dependency management system such as [Maven](https://maven.apache.org/) or [Gradle](https://gradle.org/) and using the [jitpack.io](https://jitpack.io/#danthonywalker/bc4d4j) repository.

### MainCommand
Commands are registered through instances of functions that are annotated with the `@MainCommand` annotation.
```kotlin
class PingCommand {
    @MainCommand(
        prefix = "~",
        name = "ping",
        aliases = arrayOf("ping"))
    fun ping(context: CommandContext) {
        RequestBuffer.request { context.messageBuilder.withContent("Pong!").send() }
    }
}
```
The above code indicates that it wants the `ping()` function to be invoked when a user types `~ping`; in this case, the bot will respond back with a `Pong!` message.

However, before that can happen, the command must be registered. To register the above command, simply invoke the following code:
```kotlin
client.getCommandRegistry().registerCommands(PingCommand())
```

### SubCommand
Sometimes, your commands have strictly defined arguments that you wish to handle separately. Luckily, BC4D4J makes it intuitive to define such routines.

Say we want to define a sub-command for `ping` where if the user adds the word `pong` as an argument the message sent is `Bang!` instead. To do so, we can add the following to the `PingCommand` class.
```kotlin
@SubCommand(
    name = "pong",
    aliases = arrayOf("pong"))
fun pong(context: CommandContext) {
    RequestBuffer.request { context.messageBuilder.withContent("Bang!").send() }
}
```
>BC4D4J will allow you to define this annotation anywhere in your code. However, just like with the `MainCommand` example, the instance of that class must be registered as well (the order of registration does not matter).

Now we must change our `MainCommand` annotation to the following.
```kotlin
@MainCommand(
    prefix = "~",
    name = "ping",
    aliases = arrayOf("ping"),
    subCommands = arrayOf("pong"))
```
Notice that the String we provided to `subCommands` is the same String as the `name` for our `SubCommand`.

BC4D4J also supports nested sub-commands meaning your command arguments can span infinitely (or until you reach Discord's message limit).
##### Hierarchy
BC4D4J handles and passes commands in a hierarchy. It'll attempt to invoke the last *valid* annotation in the hierarchy where the first in the hierarchy is the one and only `MainCommand` and then the nested `SubCommand` structure(s). Unlike other libraries, other functions in the hierarchy are **not** invoked; meaning either one or zero functions will be invoked for any given command.

### ExceptionHandler
Sometimes, you don't want to liter your codebase with try-catch statements everywhere. Luckily, BC4D4J provides a way to centralize exception management.

```kotlin
@ExceptionHandler(name = "ping")
@ExceptionHandler(name = "pong")
fun pingExceptionHandler(context: ExceptionHandler) {...}
```
Notice that the String we provided to `name` is the same String as the `name` for our `MainCommand` and `SubCommand`.

Also, don't worry, BC4D4J will automatically log all exceptions for you whether you have this annotation or not.

### Command Properties
As mentioned earlier, BC4D4J will always attempt to invoke the last *valid* annotation in its command hierarchy. An annotation is considered to be valid if the message follows all the properties that the annotation defines. Here's a list of all the properties:

* `name` : A *globally unique* name to register a `MainCommand` or `SubCommand`. **Default: *none***
* `prefix` : The prefix required to invoke a command directly before its alias. **Default: *none* |** `MainCommand` ***Only***
* `usage` : How to use the command. This has no functional purpose. **Default: *""***
* `aliases` : The keyword(s) that activate a command. **Default: *none***
* `displayName` : A more user-friendly version of `name`. This has no functional purpose. **Default: *""***
* `description` : Describes what the command is/does. This has no functional purpose. **Default: *""***
* `ignoreDMs` : Ignore messages that come from Direct Messages (aka private messages). **Default: *true***
* `ignoreBots` : Ignore messages that come from bot users. **Default: *true***
* `ignoreCase` : Whether or not to be case-sensitive towards the aliases. **Default: *false***
* `ignoreGuilds` : Ignore messages that come from guilds. **Default: *false***
* `deleteMessage` : Delete the message that the command was (successfully) invoked with. **Default: *false***
* `requireMention` : Require the message to contain a mention towards the bot in order to use the command. This comes immediately before the prefix. **Default: *false* |** `MainCommand` ***Only***
* `subCommands` : An array of `name` properties for all the `SubCommand` annotations beneath this particular command in the hierarchy. **Default: *emptyArray()***
* `permissions` : Permissions that are required by the *user* to activate the command. **Default: *emptyArray()***