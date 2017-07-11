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

import com.scalified.tree.TreeNode
import com.scalified.tree.multinode.ArrayMultiTreeNode
import mu.KLogging
import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.MessageTokenizer
import sx.blah.discord.util.MessageTokenizer.UserMentionToken
import sx.blah.discord.util.RequestBuffer
import technology.yockto.bc4d4j.config.ExceptionHandlerConfig
import technology.yockto.bc4d4j.config.MainCommandConfig
import technology.yockto.bc4d4j.config.SubCommandConfig
import technology.yockto.bc4d4j.context.CommandContext
import technology.yockto.bc4d4j.context.CooldownContext
import technology.yockto.bc4d4j.context.CooldownException
import technology.yockto.bc4d4j.context.ExceptionContext
import technology.yockto.bc4d4j.context.ResultContext
import technology.yockto.bc4d4j.util.withinScope
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutionException
import kotlin.LazyThreadSafetyMode.NONE

internal class ConfigDispatcher : IListener<MessageReceivedEvent> {
    val exceptionHandlers = ConcurrentHashMap<ExceptionHandlerConfig, (ExceptionContext) -> Any>()
    val mainSubCommands = ConcurrentHashMap<MainCommandConfig, MutableSet<SubCommandConfig>>()
    val subSubCommands = ConcurrentHashMap<SubCommandConfig, MutableSet<SubCommandConfig>>()
    val mainCommands = ConcurrentHashMap<MainCommandConfig, (CommandContext) -> Any>()
    val subCommands = ConcurrentHashMap<SubCommandConfig, (CommandContext) -> Any>()
    private val cooldowns = ConcurrentHashMap<CommandContext, CooldownContext>()

    override fun handle(event: MessageReceivedEvent) {
        // Prevents the IllegalArgumentException when parsing content
        event.message.content.takeIf(String::isEmpty)?.let { return }

        val mention = event.message.tokenize().takeIf(MessageTokenizer::hasNextMention)?.nextMention()?.takeIf {
            it.startIndex == 0 // requireMention() specifies to only accept mentions at the start of the message
        }?.let { it as? UserMentionToken }

        val mentionedUser = mention?.mentionObject // requireMention() specifies mention if to a bot (client)
        val mentionedName = mentionedUser?.takeIf { it == event.client.ourUser }?.mention(mention.isNickname)

        var prefix: String? = null
        val possibleMainCommands = mainCommands.keys.filter {
            var doesFormat = (it.requireMention == (mentionedName != null))
            if(doesFormat) { // Check for the mention if config requests it

                val requiredPrefix = "${mentionedName ?: ""}${it.prefix}"
                doesFormat = event.message.content.startsWith(requiredPrefix)

                // Prefix for message is static so it can be set if valid
                requiredPrefix.takeIf { doesFormat }?.let { prefix = it }
            }

            doesFormat
        }

        val userPermissions by lazy(NONE, { event.channel.getModifiedPermissions(event.author) })
        val commandAndArguments by lazy(NONE, { event.message.content.removePrefix(prefix!!).split(" ") })
        val arguments by lazy(NONE, { commandAndArguments.subList(1, commandAndArguments.size) })
        val command by lazy(NONE, { commandAndArguments[0] })

        fun MainCommandConfig.isValid(): Boolean { // Only evaluates the command portion
            return !(requireOwner && (event.client.applicationOwner != event.author)) &&
                aliases.any { it.equals(command, ignoreCase) } &&
                !(ignoreArguments && arguments.isNotEmpty()) &&
                !(ignoreGuilds && !event.channel.isPrivate) &&
                userPermissions.containsAll(permissions) &&
                !(ignoreDMs && event.channel.isPrivate) &&
                !(ignoreHumans && !event.author.isBot) &&
                !(ignoreBots && event.author.isBot) &&
                scope.withinScope(event.message)
        }

        fun SubCommandConfig.checkValidation(index: Int): Boolean { // Avoid VerifyError
            return !(requireOwner && (event.client.applicationOwner != event.author)) &&
                aliases.any { it.equals(arguments.getOrNull(index), ignoreCase) } &&
                !(ignoreArguments && (arguments.size > (index + 1))) &&
                !(ignoreGuilds && !event.channel.isPrivate) &&
                userPermissions.containsAll(permissions) &&
                !(ignoreDMs && event.channel.isPrivate) &&
                !(ignoreHumans && !event.author.isBot) &&
                !(ignoreBots && event.author.isBot) &&
                scope.withinScope(event.message)
        }

        fun SubCommandConfig.createHierarchy(tree: TreeNode<Any>) {
            // Prevents false positives by adding commands that are in the arguments
            if(aliases.any { it.equals(arguments.getOrNull(tree.level()), true) }) {

                val node = ArrayMultiTreeNode<Any>(this)
                tree.add(node) // Becomes leaf unless other commands exist
                subSubCommands[this]?.forEach { it.createHierarchy(node) }
            }
        }

        possibleMainCommands.filter {
            it.aliases.any { it.equals(command, true) }
        }.map { // Only process intended configurations
            ArrayMultiTreeNode<Any>(it)
        }.forEach { rootNode ->

            val mainCommand = rootNode.data() as MainCommandConfig
            mainSubCommands[mainCommand]?.forEach { it.createHierarchy(rootNode) }

            for(level in rootNode.height() downTo 0) {
                var valid = false // Flag the level that is valid
                rootNode.filter { it.level() == level }.forEach {

                    val subCommand = it.data() as? SubCommandConfig
                    val subArguments = arguments.subList(level, arguments.size)
                    val context = CommandContext(mainCommand, subCommand, event, subArguments)

                    if((subCommand == null) && mainCommand.isValid()) {
                        mainCommands[mainCommand]?.let { dispatch(context, it) }
                        valid = true

                    } else if(subCommand?.checkValidation(level - 1) == true) {
                        subCommands[subCommand]?.let { dispatch(context, it) }
                        valid = true
                    }
                }

                // Label won't work
                if(valid) { break }
            }
        }
    }

    private fun Throwable.handle(context: CommandContext) {
        logger.info(this, { "CommandContext: $context" })

        exceptionHandlers.filterKeys { // Use name of the lower-tired command
            val name = (context.subCommand?.name ?: context.mainCommand.name)
            (it.name == name) && it.scope.withinScope(context.event.message)

        }.forEach { config, handler -> // It's optional that an ExceptionHandler returns its own result
            handler.invoke(ExceptionContext(config, context, this)).let { it as? ResultContext }?.let {

                // Ignore delete context as that may have caused the original exception
                // However, it is perfectly valid to set a new cooldown if an error occurs
                it.cooldown.takeIf { it.duration > 0 }?.let { cooldowns.put(context, it) }
            }
        }
    }

    private fun dispatch(context: CommandContext, action: (CommandContext) -> Any) {
        try { // Attempt to dispatch the configuration taking into account cooldowns

            cooldowns.filterKeys { // Either 0 or 1 keys are returned due to scope's limitations
                (it.mainCommand == context.mainCommand) && (it.subCommand == context.subCommand)
            }.forEach { command, cooldown ->

                if(context.event.message.timestamp >= cooldown.end) {
                    cooldowns.remove(command, cooldown) // Let GC run

                } else { // TODO Find better solution
                    throw CooldownException(cooldown)
                }
            }

            action.invoke(context).let { it as? ResultContext }?.let {
                it.cooldown.takeIf { it.duration > 0 }?.let { cooldowns.put(context, it) }
                if(it.deleteMessage) { // Use the get() method so the exception propagates
                    RequestBuffer.request { context.event.message.delete() }.get()
                }
            }

        } catch(wrapperException: InvocationTargetException) {
            logger.trace(wrapperException, { "Before unwrapping" })
            (wrapperException.cause as Throwable).handle(context)

        } catch(wrapperException: ExecutionException) {
            logger.trace(wrapperException, { "Before unwrapping" })
            (wrapperException.cause as Throwable).handle(context)

        } catch(throwable: Throwable) {
            throwable.handle(context)
        }
    }

    private companion object : KLogging()
}