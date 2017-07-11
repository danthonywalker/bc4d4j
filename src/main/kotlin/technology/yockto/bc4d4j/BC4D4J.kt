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

import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.modules.IModule

class BC4D4J : IModule {
    private lateinit var client: IDiscordClient

    override fun enable(client: IDiscordClient): Boolean {
        this.client = client // Closes any previous instances
        instances.put(client, ConfigRegistry(client))?.close()
        return true
    }

    override fun disable(): Unit = instances.remove(client)?.close().let {}
    override fun getName(): String = "Better Commands 4 Discord4J"
    override fun getMinimumDiscord4JVersion(): String = "2.8.4"
    override fun getAuthor(): String = "danthonywalker"
    override fun getVersion(): String = "0.4.0"
}