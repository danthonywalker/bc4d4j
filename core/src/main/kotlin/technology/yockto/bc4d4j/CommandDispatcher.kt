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

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.CoroutineDispatcher
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import technology.yockto.bc4d4j.command.Command
import technology.yockto.bc4d4j.command.CommandContext
import technology.yockto.bc4d4j.command.CommandLimiter
import technology.yockto.bc4d4j.command.Failable
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

            if(arguments?.isEmpty() == true) {
                argumentFactory.onFail(emptyContext)

            } else if(arguments?.isNotEmpty() == true) {
                it.process(emptyContext.copy(arguments = arguments))
            }
        }
    }

    private suspend fun <F : Failable, T> F.attempt(context: CommandContext, block: suspend (F) -> T) = try {
        block(this) // Failable methods are allowed to throw exceptions (they're also always suspendable)

    } catch(exception: Exception) {
        onFail(context, exception)
        null
    }

    private suspend fun Iterable<CommandLimiter>.doesLimit(context: CommandContext) = associateBy {
        async(coroutineDispatcher) { // Forks possible load
            it.attempt(context) { it.shouldLimit(context) }
        }

    }.filter { // Process all forked workload
        val result = (it.key.await() == true)
        it.value.takeIf { result }?.onFail(context)

        result
    }.any()

    private suspend fun Command.process(oldContext: CommandContext) {
        val newArguments = oldContext.arguments.subList(1, oldContext.arguments.size)
        val newArgument = oldContext.arguments[0] // Guaranteed that the list will never be empty
        val newContext = CommandContext(newArguments, newArgument, oldContext.event, config.name)

        if(!config.restrictors.doesLimit(newContext)) { // Execute SubCommand
            config.subCommands.mapNotNull { registry.commands[it] }.forEach {
                launch(coroutineDispatcher) { it.process(newContext) }
            }

            if(!config.limiters.doesLimit(newContext)) { // Starts the execution
                config.executor.attempt(newContext) { it.onExecute(newContext) }
            }
        }
    }
}
