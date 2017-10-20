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
package technology.yockto.bc4d4j.common

import technology.yockto.bc4d4j.core.command.ArgumentFactory
import technology.yockto.bc4d4j.core.command.Command
import technology.yockto.bc4d4j.core.command.CommandConfig
import technology.yockto.bc4d4j.core.command.CommandExecutor
import technology.yockto.bc4d4j.core.command.CommandLimiter

data class CommandBuilder private constructor(
    val commandLimiters: Set<CommandLimiter>,
    val argumentFactory: ArgumentFactory?,
    val commandExecutor: CommandExecutor?,
    val subCommands: Set<String>,
    val name: String?) {

    constructor() : this(emptySet(), null, null, emptySet(), null)

    fun removeCommandLimiter(limiter: CommandLimiter): CommandBuilder = setCommandLimiters(commandLimiters - limiter)
    fun addCommandLimiter(limiter: CommandLimiter): CommandBuilder = setCommandLimiters(commandLimiters + limiter)

    fun removeSubCommand(subCommand: String): CommandBuilder = setSubCommands(subCommands - subCommand)
    fun addSubCommand(subCommand: String): CommandBuilder = setSubCommands(subCommands + subCommand)

    fun removeSubCommand(subCommand: Command): CommandBuilder = removeSubCommand(subCommand.config.name)
    fun addSubCommand(subCommand: Command): CommandBuilder = addSubCommand(subCommand.config.name)

    fun setArgumentFactory(argumentFactory: ArgumentFactory?): CommandBuilder = copy(argumentFactory = argumentFactory)
    fun setCommandLimiters(limiters: Set<CommandLimiter>): CommandBuilder = copy(commandLimiters = limiters)
    fun setCommandExecutor(executor: CommandExecutor): CommandBuilder = copy(commandExecutor = executor)
    fun setSubCommands(subCommands: Set<String>): CommandBuilder = copy(subCommands = subCommands)
    fun setName(name: String): CommandBuilder = copy(name = name)

    fun build(): Command { // For maximum convenience, allow null up to this point
        val name = name ?: throw IllegalStateException("\"name\" cannot be null!")
        val executor = commandExecutor ?: throw IllegalStateException("\"CommandExecutor\" cannot be null!")

        return Command(CommandConfig(name, executor, commandLimiters, argumentFactory, subCommands))
    }
}
