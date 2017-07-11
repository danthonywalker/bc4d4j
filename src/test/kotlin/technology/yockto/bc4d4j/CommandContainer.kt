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

import sx.blah.discord.util.RequestBuffer
import technology.yockto.bc4d4j.annotation.ExceptionHandler
import technology.yockto.bc4d4j.annotation.MainCommand
import technology.yockto.bc4d4j.annotation.SubCommand
import technology.yockto.bc4d4j.context.CommandContext
import technology.yockto.bc4d4j.context.CooldownContext
import technology.yockto.bc4d4j.context.CooldownException
import technology.yockto.bc4d4j.context.ExceptionContext
import technology.yockto.bc4d4j.context.ResultContext
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class CommandContainer {
    @MainCommand(
        prefix = "~",
        name = "prefix ping",
        aliases = arrayOf("ping"),
        subCommands = arrayOf("ping two", "ping exception", "ping result", "ping not-exists"))
    fun prefixPingCommandTest(context: CommandContext) {
        RequestBuffer.request { context.event.message.reply("Pong!") }
    }

    @MainCommand(
        prefix = " ",
        name = "mention ping",
        requireMention = true,
        aliases = arrayOf("ping"))
    fun mentionPingCommandTest(context: CommandContext) {
        RequestBuffer.request { context.event.message.reply("Pong!") }
    }

    @SubCommand(
        name = "ping two",
        aliases = arrayOf("two"))
    fun pingTwoCommandTest(context: CommandContext) {
        RequestBuffer.request { context.event.message.reply("Pong Pong!") }
    }

    @SubCommand(
        name = "ping result",
        aliases = arrayOf("result"))
    @Suppress("UNUSED_PARAMETER")
    fun cooldownCommandTest(context: CommandContext): ResultContext {
        return ResultContext( // Cast command again within 10 seconds
            cooldown = CooldownContext(duration = 10),
            deleteMessage = true)
    }

    @ExceptionHandler("ping result")
    fun resultHandler(context: ExceptionContext) {
        val exception = context.exception as CooldownException
        val timeLeft = ChronoUnit.SECONDS.between(LocalDateTime.now(), exception.cooldownContext.end)
        RequestBuffer.request { context.command.event.message.reply("${timeLeft}s time left!") }
    }

    @SubCommand(
        name = "ping exception",
        aliases = arrayOf("exception"))
    fun exceptionCommandTest(context: CommandContext): Nothing = throw RuntimeException(context.mainCommand.name)

    @ExceptionHandler(name = "ping exception")
    fun exceptionHandler(context: ExceptionContext) {
        RequestBuffer.request { context.command.event.message.reply("Exception!") }
    }
}