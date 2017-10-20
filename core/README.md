# BC4D4J Core
This module provides the necessary components to designing and registering commands.

## Getting Started
Setting up BC4D4J is as simple as activating the module. Either add the `.jar` to your `modules` folder or add the
following code to your program:

Java:
```java
IDiscordClient client; // Initialized somewhere else
client.getModuleLoader().loadModule(new BC4D4JCore());
```

Kotlin:
```kotlin
val client: IDiscordClient // Initialized somewhere else
client.moduleLoader.loadModule(BC4D4JCore())
```

***

After initializing the module you may now start registering commands. To construct a command you must first construct a
`CommandConfig` which will allow you to construct a `Command`. The easiest way to accomplish this task is through the
`CommandBuilder` class in the *bc4d4j-common* module.

Java:
```java
CommandDispatcher dispatcher = BC4D4JFactory.getCommandDispatcher(client);
CommandRegistry registry = dispatcher.getRegistry();
Command command; // Initialized somewhere else
registry.addCommand(command);
```

Kotlin:
```kotlin
val dispatcher = client.commandDispatcher
val registry = dispatcher.registry
val command: Command // Initialized somewhere else
registry.addCommand(command)
```

To unregister a command simply call `CommandRegistry#removeCommand`. Both `addCommand` and `removeCommand` return a
boolean representing if a command was successfully added or removed (`true` for successful, `false` otherwise).
