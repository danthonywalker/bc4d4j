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

import sx.blah.discord.api.ClientBuilder

fun main(args: Array<String>) {
    val client = ClientBuilder().apply {
        withToken(args[0]) // Provided
    }.login()

    client.moduleLoader.loadModule(BC4D4J())
    client.configRegistry.register(CommandContainer())
}