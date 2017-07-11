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
package technology.yockto.bc4d4j.util

import sx.blah.discord.handle.obj.IMessage
import technology.yockto.bc4d4j.Scope
import technology.yockto.bc4d4j.Scope.*
import technology.yockto.bc4d4j.annotation.ExceptionHandler
import technology.yockto.bc4d4j.annotation.MainCommand
import technology.yockto.bc4d4j.annotation.SubCommand
import technology.yockto.bc4d4j.config.ExceptionHandlerConfig
import technology.yockto.bc4d4j.config.MainCommandConfig
import technology.yockto.bc4d4j.config.SubCommandConfig

fun MainCommand.getConfig(scope: Scope = Global): MainCommandConfig {
    return MainCommandConfig( // MainCommand fields gets adapted over
        ignoreArguments = ignoreArguments,
        permissions = permissions.toSet(),
        subCommands = subCommands.toSet(),
        requireMention = requireMention,
        ignoreGuilds = ignoreGuilds,
        ignoreHumans = ignoreHumans,
        requireOwner = requireOwner,
        aliases = aliases.toSet(),
        description = description,
        displayName = displayName,
        ignoreBots = ignoreBots,
        ignoreCase = ignoreCase,
        ignoreDMs = ignoreDMs,
        prefix = prefix,
        scope = scope,
        usage = usage,
        name = name)
}

fun SubCommand.getConfig(scope: Scope = Global): SubCommandConfig {
    return SubCommandConfig( // SubCommand fields gets adapted over
        permissions = permissions.toSet(),
        subCommands = subCommands.toSet(),
        ignoreArguments = ignoreArguments,
        ignoreGuilds = ignoreGuilds,
        ignoreHumans = ignoreHumans,
        requireOwner = requireOwner,
        aliases = aliases.toSet(),
        description = description,
        displayName = displayName,
        ignoreBots = ignoreBots,
        ignoreCase = ignoreCase,
        ignoreDMs = ignoreDMs,
        scope = scope,
        usage = usage,
        name = name)
}

fun ExceptionHandler.getConfig(scope: Scope = Global): ExceptionHandlerConfig {
    return ExceptionHandlerConfig( // ExceptionHandler fields gets adapted over
        scope = scope,
        name = name)
}

internal fun Scope.withinScope(message: IMessage): Boolean = when(this) {
    is VoiceChannel -> message.author.getVoiceStateForGuild(message.guild).channel == voiceChannel
    is Role -> message.author.getRolesForGuild(message.guild).contains(role)
    is TextChannel -> message.channel == textChannel
    is Region -> message.guild.region == region
    is Guild -> message.guild == guild
    is User -> message.author == user
    is Global -> true
}