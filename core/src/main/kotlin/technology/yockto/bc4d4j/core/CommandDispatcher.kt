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
package technology.yockto.bc4d4j.core

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.CoroutineDispatcher
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import technology.yockto.bc4d4j.core.command.Command
import technology.yockto.bc4d4j.core.command.CommandContext
import technology.yockto.bc4d4j.core.command.CommandRestrictor
import technology.yockto.bc4d4j.core.command.Failable
import java.util.concurrent.atomic.AtomicReference

class CommandDispatcher internal constructor(val registry: CommandRegistry) : IListener<MessageReceivedEvent> {
    private val backingCoroutineDispatcher: AtomicReference<CoroutineDispatcher> = AtomicReference(CommonPool)

    var coroutineDispatcher: CoroutineDispatcher
        get() = backingCoroutineDispatcher.get()
        set(value) = backingCoroutineDispatcher.set(value)

    override fun handle(event: MessageReceivedEvent) = registry.mainCommands.forEach {
        launch(coroutineDispatcher) { // Quickly frees up the event dispatching thread

            val emptyContext = CommandContext(emptyList(), "", event, it.config.name)
            val argumentFactory = it.config.argumentFactory!! // Main commands guarantee never null
            val arguments = argumentFactory.attempt(emptyContext) { it.getArguments(emptyContext) }

            argumentFactory.takeIf { arguments?.isEmpty() == true }?.onFail(emptyContext)
            it.takeIf { arguments?.isNotEmpty() == true }?.process(emptyContext.copy(arguments = arguments!!))
        }
    }

    private suspend fun <F : Failable, T> F.attempt(context: CommandContext, block: suspend (F) -> T) = try {
        block(this) // Failable methods are allowed to throw exceptions (they're also always suspendable)

    } catch(exception: Exception) {
        onFail(context, exception)
        null
    }

    private suspend fun Command.process(oldContext: CommandContext) {
        val newArguments = oldContext.arguments.subList(1, oldContext.arguments.size)
        val newArgument = oldContext.arguments[0] // Guaranteed that the list will never be empty
        val newContext = CommandContext(newArguments, newArgument, oldContext.event, config.name)

        val limiters = config.limiters.associateBy {
            async(coroutineDispatcher) { // Fork the limiter workload
                it.attempt(newContext) { it.shouldLimit(newContext) }
            }

        }.filter { // Process all forked workload
            val result = (it.key.await() == true)
            it.value.takeIf { result }?.onFail(newContext)

            result
        }.values

        if(limiters.filterIsInstance<CommandRestrictor>().isEmpty()) {
            subCommands.takeIf { newArguments.isNotEmpty() }?.forEach {
                launch(coroutineDispatcher) { it.process(newContext) }
            }

            // Execute last as execution may take a while so allow the SubCommands to start processing first
            config.takeIf { limiters.isEmpty() }?.executor?.attempt(newContext) { it.onExecute(newContext) }
        }
    }
}
