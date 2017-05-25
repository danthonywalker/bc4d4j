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
import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.MessageBuilder
import sx.blah.discord.util.MessageTokenizer
import sx.blah.discord.util.MessageTokenizer.UserMentionToken
import sx.blah.discord.util.RequestBuffer
import technology.yockto.bc4d4j.api.CommandContext
import technology.yockto.bc4d4j.api.ExceptionContext
import technology.yockto.bc4d4j.api.ExceptionHandler
import technology.yockto.bc4d4j.api.MainCommand
import technology.yockto.bc4d4j.api.SubCommand
import java.lang.Exception
import java.util.concurrent.ConcurrentHashMap

internal class CommandDispatcher : IListener<MessageReceivedEvent> {
    val exceptionHandlers = ConcurrentHashMap<ExceptionHandler, FunctionContext>()
    val mainSubCommands = ConcurrentHashMap<MainCommand, MutableSet<SubCommand>>()
    val subSubCommands = ConcurrentHashMap<SubCommand, MutableSet<SubCommand>>()
    val mainCommands = ConcurrentHashMap<MainCommand, FunctionContext>()
    val subCommands = ConcurrentHashMap<SubCommand, FunctionContext>()

    override fun handle(event: MessageReceivedEvent) {
        val message = event.message
        val client = message.client
        val content = message.content
        val messageId = message.longID

        val mentionToken = message.tokenize().takeIf(MessageTokenizer::hasNextMention)?.nextMention()?.let {
            it as? UserMentionToken //Grab a mention token only if the mention was at the start of a message
        }?.takeIf { it.startIndex == 0 }

        val mentionedUser = mentionToken?.mentionObject //Check to ensure that the mention is towards client
        val mentionedName = mentionedUser?.takeIf { it == client.ourUser }?.mention(mentionToken.isNickname)

        val possibleMainCommands = mainCommands.keys.filter {
            var doesFormat = it.requireMention == (mentionedName != null)
            if(doesFormat) { //Checks if message has a mention (if needed) and prefix
                doesFormat = content.startsWith("${mentionedName ?: ""}${it.prefix}")
            }

            doesFormat
        }

        if(possibleMainCommands.isEmpty()) { //Reaching here means user messed up prefixes
            logger.trace { "Message (ID: $messageId) has no suitable MainCommand prefix" }
            return
        }

        val formattedContent = content.removePrefix("${mentionedName ?: ""}${possibleMainCommands[0].prefix}")
        val commandAndArguments = formattedContent.split(" ") //After prefixes arguments are defined by spaces
        val arguments = commandAndArguments.subList(1, commandAndArguments.size)
        val command = commandAndArguments[0]

        val mainCommand = possibleMainCommands.singleOrNull { it.aliases.any { it.equals(command, true) } }
        if(mainCommand == null) { //The prefix was valid, but there's no matching command
            logger.trace { "Message (ID: $messageId) has no suitable MainCommand alias" }
            return
        }

        val author = message.author
        val botUser = author.isBot

        val channel = message.channel
        val privateMessage = channel.isPrivate
        val permissions = channel.getModifiedPermissions(author)

        fun MainCommand.isValid() = !(ignoreBots && botUser) &&
            permissions.containsAll(this.permissions.toSet()) &&
            aliases.any { it.equals(command, ignoreCase) } &&
            !(ignoreGuilds && !privateMessage) &&
            !(ignoreDMs && privateMessage)

        fun SubCommand.isValid(index: Int) = permissions.containsAll(this.permissions.toSet()) &&
            aliases.any { it.equals(arguments.getOrNull(index), ignoreCase) } &&
            !(ignoreGuilds && !privateMessage) &&
            !(ignoreDMs && privateMessage) &&
            !(ignoreBots && botUser)

        val subCommandHierarchy = mutableListOf<SubCommand>()
        fun SubCommand.findSubCommand(index: Int): SubCommand {
            subCommandHierarchy.add(this) //Higher in hierarchy

            //Get SubCommands under the current SubCommand and attempts to find the next one in the hierarchy
            return subSubCommands[this]?.firstOrNull { it.isValid(index) }?.findSubCommand(index + 1) ?: this
        }

        //Get SubCommands under the MainCommand and attempt to get the last SubCommand in the hierarchy
        val subCommand = mainSubCommands[mainCommand]?.firstOrNull { it.isValid(0) }?.findSubCommand(1)

        val messageBuilder = MessageBuilder(client).withChannel(channel)
        val commandContext = CommandContext(message, arguments, mainCommand, event, subCommandHierarchy, messageBuilder)

        if(subCommand != null) {
            subCommands[subCommand]?.let {

                try {
                    RequestBuffer.request { message.takeIf { subCommand.deleteMessage }?.delete() }
                    it.function.call(it.instance, commandContext)

                } catch(exception: Exception) {
                    logger.info(exception, { subCommand.toString() })
                    exceptionHandlers.filterKeys { it.name == subCommand.name }.forEach { handler, context ->
                        context.function.call(it.instance, ExceptionContext(exception, commandContext, handler))
                    }
                }
            }

        } else if(mainCommand.isValid()) {
            mainCommands[mainCommand]?.let {

                try {
                    RequestBuffer.request { message.takeIf { mainCommand.deleteMessage }?.delete() }
                    it.function.call(it.instance, commandContext)

                } catch(exception: Exception) {
                    logger.info(exception, { mainCommand.toString() })
                    exceptionHandlers.filterKeys { it.name == mainCommand.name }.forEach { handler, context ->
                        context.function.call(it.instance, ExceptionContext(exception, commandContext, handler))
                    }
                }
            }
        }
    }

    private companion object : KLogging()
}