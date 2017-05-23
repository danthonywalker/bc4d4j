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
package technology.yockto.bc4d4j.api

import sx.blah.discord.handle.obj.Permissions
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FUNCTION

@Repeatable
@MustBeDocumented
@Target(FUNCTION)
@Retention(RUNTIME)
annotation class SubCommand(
    val name: String,
    val usage: String = "",
    val aliases: Array<String>,
    val description: String = "",
    val ignoreDMs: Boolean = true,
    val ignoreBots: Boolean = true,
    val ignoreCase: Boolean = false,
    val ignoreGuilds: Boolean = false,
    val deleteMessage: Boolean = false,
    val subCommands: Array<String> = emptyArray(),
    val permissions: Array<Permissions> = emptyArray())