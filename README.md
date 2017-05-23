# bc4d4j
BC4D4J is an annotation-based command framework specifically designed for [Discord4J](https://github.com/austinv11/Discord4J) written in Kotlin.

## Getting Started
Setting up BC4D4J is as simple as activating the module. Either add the **.jar** to your `modules` folder or add the following code to the start of your program.
```kotlin
val client: IDiscordClient = ...
client.moduleLoader.loadModule(BC4D4J())
```
**NOTE:** If you're using the latter method, you must add BC4D4J to your CLASSPATH! To do so, it is recommended to use the [jitpack.io](https://jitpack.io/#danthonywalker/bc4d4j) repository.

Now we need to design some commands. Let's make a *Ping -> Pong!* example.
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
Now we need to register the command.
```kotlin
client.getCommandRegistry().registerCommands(PingCommand())
```
And that is it! When a user types `~ping` your bot will respond with `Pong!`.
### SubCommands
BC4D4J supports sub-command based processing! This will allow you to handle pre-defined arguments in a more elegant and easier fashion as well splitting up your command functions.

First, define a `SubCommand` annotation.
```kotlin
@SubCommand(
    name = "aSubCommand",
    ...)
fun subCommand(context: CommandContext) {...}
```
Then all you have to do is register your SubCommand!
```kotlin
@MainCommand(
    ...
    subCommands = arrayOf("aSubCommand"))
```
And you're done! The `SubCommand` will be called whenever a command based off the `MainCommand` has a matching alias to your new command!

What's great about the `SubCommand` feature is it supports *nested sub-commands!* The process is exactly the same as registering with the `MainCommand`.

Additionally, each `SubCommand` only has the restrictions that it itself defines. For example, if your `MainCommand` has `ignoreBots` set to `true`, but your `SubCommand` has `ignoreBots` set to `false` and a message by a bot is sent then the `SubCommand` can still be called! While this makes setting up certain commands a bit more tedious, it allows a lot of flexibility.
### ExceptionHandler
The `ExceptionHandler` annotation does exactly what you expect it to do. Whenever an exception is thrown while processing your command, it'll be passed on to a linking `ExceptionHandler`. The use of this annotation is completely optional. Also, if an exception is thrown, BC4D4J will always automatically log the exception so you don't have to worry about that!

To use, simply register a function as follows:
```kotlin
@ExceptionHandler(name = "command")
fun exceptionHandler(context: ExceptionContext) {...}
```
Note that `name` is not the *name* of the `ExceptionHandler`, but the `name` of *any* `MainCommand` or `SubCommand`.

### Rules and Quirks
To help organize commands internally some restrictions are in-place. If properly designed, these restrictions should never interfere with how you organize your commands externally.
1. All `MainCommand` annotations must have an unique `name`. Any non-unique names are discarded.
2. All `SubCommand` annotations must have an unique `name`. Any non-unique names are discarded.
3. If `requireMention` is set to `true` it is wise to have your prefix set to a space.
4. Due to the flexibility of BC4D4J, commands are registered *globally*. Meaning commands in one class are intertwined with the commands from another. This allows commands to be split across classes, but can also have some sideffects if one is not careful. Choose your `name` for your commands wisely and avoid registering multiple instances of the same class.

### Command Settings
* `name`: The unique name to register the `MainCommand` or `SubCommand`.
* `prefix` *(`MainCommand` only)*: The prefix to a command, before its alias.
* `usage`: How to use the command. This has no functional purpose.
* `aliases`: The keyword(s) that activate the command. This is followed immediately after the `prefix`.
* `description`: Describes what the command is/does. This has no functional purpose.
* `ignoreDMs`: Ignore messages that come from a Direct Message (aka private message).
* `ignoreBots`: Ignore messages that come from bot users.
* `ignoreCase`: This applies to the case-sensitivity of aliases.
* `ignoreGuilds`: Ignore messages that come from guilds.
* `deleteMessage`: Delete the message that the command was invoked with.
* `requireMention` *(`MainCommand` only)*: Require the user to mention the bot to use the command. This comes before `prefix`.
* `subCommands`: `name`s for all the `SubCommand` annoations beneath this particular command in the hierarchy.
* `permissions`: Permissions that are required by the *user* to activate the command.
