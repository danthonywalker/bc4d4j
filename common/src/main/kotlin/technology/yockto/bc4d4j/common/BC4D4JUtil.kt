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
package technology.yockto.bc4d4j.common

import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IGuild
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IRole
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.handle.obj.IVoiceChannel
import sx.blah.discord.handle.obj.Permissions
import technology.yockto.bc4d4j.core.CommandRegistry
import technology.yockto.bc4d4j.core.command.Command
import technology.yockto.bc4d4j.core.command.CommandContext
import technology.yockto.bc4d4j.core.commandDispatcher

val CommandContext.client: IDiscordClient get() = event.client
val CommandContext.textChannel: IChannel get() = event.channel
val CommandContext.message: IMessage get() = event.message
val CommandContext.author: IUser get() = event.author
val CommandContext.guild: IGuild get() = event.guild

val CommandContext.voiceChannel: IVoiceChannel? get() = author.getVoiceStateForGuild(guild).channel
val CommandContext.roles: List<IRole> get() = guild.getRolesForUser(author)

val CommandContext.permissions: Set<Permissions> // To truly represent all possible permissions, combine text and voice
    get() = (textChannel.getModifiedPermissions(author) + (voiceChannel?.getModifiedPermissions(author) ?: emptySet()))

val IDiscordClient.commandRegistry: CommandRegistry get() = commandDispatcher.registry
val CommandContext.command: Command? get() = client.commandRegistry.commands[commandName]
val CommandContext.parentCommands: List<Command?> get() = parentCommandNames.map { client.commandRegistry.commands[it] }
