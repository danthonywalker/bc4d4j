/*
 * This file is part of bc4d4j.
 *
 * bc4d4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * bc4d4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with bc4d4j.  If not, see <http://www.gnu.org/licenses/>.
 */
package technology.yockto.bc4d4j

import mu.KLogging
import technology.yockto.bc4d4j.command.Command
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class CommandRegistry internal constructor() {
    private val backingMainCommands = ConcurrentHashMap.newKeySet<Command>()
    private val backingCommands = ConcurrentHashMap<String, Command>()
    private val lock = ReentrantLock()

    val commands: Map<String, Command> = Collections.unmodifiableMap(backingCommands)
    val mainCommands: Set<Command> = Collections.unmodifiableSet(backingMainCommands)

    fun removeCommand(command: Command): Boolean = lock.withLock {
        val removedCommand = (backingCommands.remove(command.config.name) != null)
        logger.info { "Removal status for command, $command: $removedCommand" }

        if(removedCommand) { // Prevents unnecessary lookups for command
            val removedMainCommand = backingMainCommands.remove(command)
            logger.trace { "Removal status for main command, $command: $removedMainCommand" }

            command.backingParentCommands.removeIf { // Removes every element
                val removedSubCommand = it.backingSubCommands.remove(command)
                logger.trace { "Removal status of sub-command, $command, from $it: $removedSubCommand" }

                removedSubCommand
            }

            command.backingSubCommands.removeIf { // Removes every possible element
                val removedParentCommand = it.backingParentCommands.remove(command)
                logger.trace { "Removal status of parent command, $command, from $it: $removedParentCommand" }

                removedParentCommand
            }
        }

        removedCommand
    }

    fun addCommand(command: Command): Boolean = lock.withLock {
        val addedCommand = (backingCommands.putIfAbsent(command.config.name, command) == null)
        logger.info { "Addition status for command, $command: $addedCommand" }

        if(addedCommand) { // Do not register other components if need
            val mainCommand = (command.config.argumentFactory != null)
            val addedMainCommand = (backingMainCommands.takeIf { mainCommand }?.add(command) == true)
            logger.trace { "Addition status for main command, $command: $addedMainCommand" }

            commands.values.filter { it.config.subCommands.contains(command.config.name) }.forEach {
                it.addSubCommand(command) // Refresh older instances to link with function's command
            }

            command.config.subCommands.mapNotNull(commands::get).forEach { command.addSubCommand(it) }
        }

        addedCommand
    }

    private fun Command.addSubCommand(command: Command) {
        val addedParentCommand = command.backingParentCommands.add(this)
        val addedSubCommand = backingSubCommands.add(command)

        logger.trace { "Addition status of parent command, $this, from $command: $addedParentCommand" }
        logger.trace { "Addition status of sub-command, $command, from $this: $addedSubCommand" }
    }

    private companion object : KLogging()
}
