/*
 * Better Commands 4 Discord4J
 * Copyright (C) 2017  danthonywalker
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package technology.yockto.bc4d4j

import mu.KLogging
import sx.blah.discord.api.IDiscordClient
import technology.yockto.bc4d4j.Scope.Global
import technology.yockto.bc4d4j.annotation.ExceptionHandler
import technology.yockto.bc4d4j.annotation.MainCommand
import technology.yockto.bc4d4j.annotation.SubCommand
import technology.yockto.bc4d4j.config.ExceptionHandlerConfig
import technology.yockto.bc4d4j.config.MainCommandConfig
import technology.yockto.bc4d4j.config.SubCommandConfig
import technology.yockto.bc4d4j.context.CommandContext
import technology.yockto.bc4d4j.context.ExceptionContext
import technology.yockto.bc4d4j.util.getConfig
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredMemberFunctions

class ConfigRegistry internal constructor(val client: IDiscordClient): AutoCloseable {
    private val dispatcher = ConfigDispatcher().apply {
        // Allow the ConfigDispatcher to receive events
        client.dispatcher.registerListener(this)
    }

    val exceptionHandlers: Set<ExceptionHandlerConfig> = dispatcher.exceptionHandlers.keys
    val mainCommands: Set<MainCommandConfig> = dispatcher.mainCommands.keys
    private val subCommands = dispatcher.subCommands.keys

    fun register(container: Any, scope: Scope = Global): Boolean {
        var added = true // Default true since "everything" registered
        container::class.declaredMemberFunctions.forEach { function ->
            function.annotations.forEach { annotation ->
                when(annotation) {

                    is ExceptionHandler -> { // TODO Verify the method signature
                        val registered = register(annotation.getConfig(scope), {
                            function.call(container, it)!!
                        })

                        added = (added && registered)
                    }

                    is MainCommand -> { // TODO Verification of method signature
                        val registered = register(annotation.getConfig(scope), {
                            function.call(container, it)!!
                        })

                        added = (added && registered)
                    }

                    is SubCommand -> { // TODO Verification of method signature
                        val registered = register(annotation.getConfig(scope), {
                            function.call(container, it)!!
                        })

                        added = (added && registered)
                    }
                }
            }
        }

        return added
    }

    fun register(mainCommand: MainCommandConfig, action: (CommandContext) -> Any): Boolean {
        val added = dispatcher.mainCommands.putIfAbsent(mainCommand, action) == null
        logger.takeIf { added }?.debug { "Successfully registered $mainCommand" }
        linkCommands()
        return added
    }

    fun register(subCommand: SubCommandConfig, action: (CommandContext) -> Any): Boolean {
        val added = dispatcher.subCommands.putIfAbsent(subCommand, action) == null
        logger.takeIf { added }?.debug { "Successfully registered $subCommand" }
        linkCommands()
        return added
    }

    fun register(exceptionHandler: ExceptionHandlerConfig, action: (ExceptionContext) -> Any): Boolean {
        val added = dispatcher.exceptionHandlers.putIfAbsent(exceptionHandler, action) == null
        logger.takeIf { added }?.debug { "Successfully registered $exceptionHandler" }
        return added
    }

    fun unregister(container: Any, scope: Scope = Global): Boolean {
        var removed = true // Default false since "everything" unregistered
        container::class.declaredMemberFunctions.flatMap(KFunction<*>::annotations).forEach {

            when(it) {
                is ExceptionHandler -> removed = (unregister(it.getConfig(scope)) && removed)
                is MainCommand -> removed = (unregister(it.getConfig(scope)) && removed)
                is SubCommand -> removed = (unregister(it.getConfig(scope)) && removed)
            }
        }

        return removed
    }

    fun unregister(mainCommand: MainCommandConfig): Boolean {
        val removed = dispatcher.mainCommands.remove(mainCommand) != null
        synchronized(this, { dispatcher.mainSubCommands.remove(mainCommand) })
        logger.takeIf { removed }?.debug { "Successfully unregistered $mainCommand" }

        return removed
    }

    fun unregister(subCommand: SubCommandConfig): Boolean {
        val removed = dispatcher.subCommands.remove(subCommand) != null
        synchronized(this, { dispatcher.subSubCommands.remove(subCommand) })
        logger.takeIf { removed }?.debug { "Successfully unregistered $subCommand" }

        return removed
    }

    fun unregister(exceptionHandler: ExceptionHandlerConfig): Boolean {
        val removed = dispatcher.exceptionHandlers.remove(exceptionHandler) != null
        logger.takeIf { removed }?.debug { "Successfully unregistered $exceptionHandler" }
        return removed
    }

    fun getSubCommands(command: MainCommandConfig): Set<SubCommandConfig> {
        return dispatcher.mainSubCommands.computeIfAbsent(command, { ConcurrentHashMap.newKeySet() })
    }

    fun getSubCommands(command: SubCommandConfig): Set<SubCommandConfig> {
        return dispatcher.subSubCommands.computeIfAbsent(command, { ConcurrentHashMap.newKeySet() })
    }

    private fun linkCommands() = synchronized(this, {
        subCommands.forEach { subCommand ->

            mainCommands.filter { it.subCommands.contains(subCommand.name) }.forEach {
                val subCommands = dispatcher.mainSubCommands.computeIfAbsent(it, { ConcurrentHashMap.newKeySet() })
                val added = subCommands.add(subCommand) // It's possible this may exist by virtue of previous calls

                logger.takeIf { added }?.debug { "Linked SubCommand, $subCommand, to MainCommand, $it" }
            }

            subCommands.filter { it.subCommands.contains(subCommand.name) }.forEach {
                val subCommands = dispatcher.subSubCommands.computeIfAbsent(it, { ConcurrentHashMap.newKeySet() })
                val added = subCommands.add(subCommand) // It's possible this may exist by virtue of previous calls

                logger.takeIf { added }?.debug { "Linked SubCommand, $subCommand, to MainCommand, $it" }
            }
        }
    })

    override fun close() = client.dispatcher.unregisterListener(dispatcher)
    private companion object : KLogging()
}