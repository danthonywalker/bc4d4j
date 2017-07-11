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
package technology.yockto.bc4d4j.config

import sx.blah.discord.handle.obj.Permissions
import technology.yockto.bc4d4j.Scope
import technology.yockto.bc4d4j.Scope.Global

data class SubCommandConfig(
    val permissions: Set<Permissions> = emptySet(),
    val subCommands: Set<String> = emptySet(),
    val ignoreArguments: Boolean = false,
    val ignoreGuilds: Boolean = false,
    val ignoreHumans: Boolean = false,
    val requireOwner: Boolean = false,
    val ignoreCase: Boolean = false,
    val ignoreBots: Boolean = true,
    val ignoreDMs: Boolean = true,
    val description: String = "",
    val displayName: String = "",
    val scope: Scope = Global,
    val aliases: Set<String>,
    val usage: String = "",
    val name: String)