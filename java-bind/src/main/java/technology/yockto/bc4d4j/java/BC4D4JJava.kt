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
package technology.yockto.bc4d4j.java

import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.modules.IModule

class BC4D4JJava : IModule {
    override fun getName(): String = "Better Commands 4 Discord4J Java-Bind"
    override fun getMinimumDiscord4JVersion(): String = "2.9.1"
    override fun getAuthor(): String = "danthonywalker"
    override fun getVersion(): String = "0.5.0"

    override fun enable(client: IDiscordClient): Boolean = true
    override fun disable() = Unit
}
