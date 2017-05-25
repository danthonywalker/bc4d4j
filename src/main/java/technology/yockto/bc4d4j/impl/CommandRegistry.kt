/*
 * Better Commands 4 Discord4J
 * Copyright (C) 2017  danthonywalker
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package technology.yockto.bc4d4j.impl

import mu.KLogging
import sx.blah.discord.api.IDiscordClient
import technology.yockto.bc4d4j.api.ExceptionHandler
import technology.yockto.bc4d4j.api.MainCommand
import technology.yockto.bc4d4j.api.SubCommand
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.reflect.full.declaredMemberFunctions

class CommandRegistry internal constructor(val client: IDiscordClient) : AutoCloseable {
    private val dispatcher = CommandDispatcher().apply {
        //Allows the CommandDispatcher to receive events
        client.dispatcher.registerListener(this)
    }

    val mainCommands: Set<MainCommand> = dispatcher.mainCommands.keys
    private val subCommands = dispatcher.subCommands.keys
    private val lock = ReentrantLock()

    fun registerCommands(commands: Any): Unit = lock.withLock {
        commands::class.declaredMemberFunctions.forEach {
            it.annotations.forEach { annotation ->

                val functionContext = FunctionContext(commands, it) //Gives context to call functions
                if((annotation is MainCommand) && mainCommands.none { it.name == annotation.name }) {
                    dispatcher.mainCommands.put(annotation, functionContext)
                    logger.info { "Registered $annotation" }

                } else if((annotation is SubCommand) && subCommands.none { it.name == annotation.name }) {
                    dispatcher.subCommands.put(annotation, functionContext)
                    logger.info { "Registered $annotation" }

                } else if(annotation is ExceptionHandler) {
                    dispatcher.exceptionHandlers.put(annotation, functionContext)
                    logger.info { "Registered $annotation" }
                }
            }
        }

        subCommands.forEach { subCommand -> //Registers SubCommands under other commands.
            mainCommands.singleOrNull { it.subCommands.contains(subCommand.name) }?.let {
                dispatcher.mainSubCommands.computeIfAbsent(it, { ConcurrentHashMap.newKeySet() }).add(subCommand)
                logger.info { "Added SubCommand, ${subCommand.name}, as a SubCommand to MainCommand, ${it.name}" }
            }

            subCommands.singleOrNull { it.subCommands.contains(subCommand.name) }?.let {
                dispatcher.subSubCommands.computeIfAbsent(it, { ConcurrentHashMap.newKeySet() }).add(subCommand)
                logger.info { "Added SubCommand, ${subCommand.name}, as a SubCommand to SubCommand, ${it.name}" }
            }
        }
    }

    fun unregisterCommands(commands: Any): Unit = lock.withLock {
        commands::class.declaredMemberFunctions.flatMap { it.annotations }.forEach {

            when(it) {
                is MainCommand -> {
                    dispatcher.mainSubCommands.remove(it)
                    dispatcher.mainCommands.remove(it)
                    logger.info { "Unregistered $it" }
                }

                is SubCommand -> {
                    dispatcher.subSubCommands.remove(it)
                    dispatcher.subCommands.remove(it)
                    logger.info { "Unregistered $it" }
                }

                is ExceptionHandler ->  {
                    dispatcher.exceptionHandlers.remove(it)
                    logger.info { "Unregistered $it" }
                }
            }
        }
    }

    fun getSubCommands(command: MainCommand): Set<SubCommand> { //May be null so use a normal default
        return dispatcher.mainSubCommands.computeIfAbsent(command, { ConcurrentHashMap.newKeySet() })
    }

    fun getSubCommands(command: SubCommand): Set<SubCommand> { //May be null so use a normal default
        return dispatcher.subSubCommands.computeIfAbsent(command, { ConcurrentHashMap.newKeySet() })
    }

    override fun close() = client.dispatcher.unregisterListener(dispatcher)
    private companion object : KLogging()
}