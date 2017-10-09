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
package technology.yockto.bc4d4j.command

import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

class Command(val config: CommandConfig) {
    internal val backingParentCommands = ConcurrentHashMap.newKeySet<Command>()
    internal val backingSubCommands = ConcurrentHashMap.newKeySet<Command>()

    val parentCommands: Set<Command> = Collections.unmodifiableSet(backingParentCommands)
    val subCommands: Set<Command> = Collections.unmodifiableSet(backingSubCommands)

    override fun equals(other: Any?): Boolean = (config.name == (other as? Command)?.config?.name)
    override fun hashCode(): Int = config.name.hashCode()
    override fun toString(): String = config.name
}
