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
package technology.yockto.bc4d4j

import sx.blah.discord.util.RequestBuffer
import technology.yockto.bc4d4j.api.CommandContext
import technology.yockto.bc4d4j.api.ExceptionContext
import technology.yockto.bc4d4j.api.ExceptionHandler
import technology.yockto.bc4d4j.api.MainCommand
import technology.yockto.bc4d4j.api.SubCommand

class PingCommand {

    @MainCommand(
        prefix = "~",
        name = "ping",
        aliases = arrayOf("ping"),
        subCommands = arrayOf("pingTwo"))
    fun ping(context: CommandContext) {
        RequestBuffer.request { context.messageBuilder.withContent("Pong!").send() }
    }

    @SubCommand(
        name = "pingTwo",
        aliases = arrayOf("two"))
    fun pingTwo(context: CommandContext) {
        RequestBuffer.request { context.messageBuilder.withContent("Pong Pong!").send() }
    }

    @MainCommand(
        prefix = " ",
        name = "mention",
        requireMention = true,
        aliases = arrayOf("mention"))
    fun pingMention(context: CommandContext) {
        RequestBuffer.request { context.messageBuilder.withContent("Mentioned!").send() }
    }

    @MainCommand(
        name = "exception",
        prefix = "exception",
        aliases = arrayOf(""))
    @Suppress("UNUSED_PARAMETER")
    fun exception(context: CommandContext): Nothing = throw RuntimeException("Exception!")

    @ExceptionHandler(name = "exception")
    fun exceptionHandle(context: ExceptionContext) {
        RequestBuffer.request { context.context.messageBuilder.withContent("Exception!").send() }
    }
}