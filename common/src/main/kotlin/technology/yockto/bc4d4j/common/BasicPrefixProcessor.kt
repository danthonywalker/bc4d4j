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

import sx.blah.discord.util.MessageTokenizer
import sx.blah.discord.util.MessageTokenizer.UserMentionToken
import technology.yockto.bc4d4j.core.command.ArgumentFactory
import technology.yockto.bc4d4j.core.command.CommandContext

class BasicPrefixProcessor(
    val prefix: String,

    val requireMention: Boolean = false,
    val ignoreCase: Boolean = false
) : ArgumentFactory {

    override suspend fun getArguments(context: CommandContext): List<String> {
        context.message.content.takeIf(String::isEmpty)?.let { return emptyList() }

        val mention = context.message.tokenize().takeIf(MessageTokenizer::hasNextMention)?.nextMention()?.takeIf {
            it.startIndex == 0 // requireMention specifies to accept mentions that are at the start of the message
        }?.let { it as? UserMentionToken }

        val mentionedUser = mention?.mentionObject // requireMention specifies mention if toward a bot (client)
        val mentionedName = mentionedUser?.takeIf { it == context.client.ourUser }?.mention(mention.isNickname)

        val valid = ((requireMention && (mentionedName != null)) || (!requireMention && (mentionedName == null)))
        val requiredPrefix = "${mentionedName ?: ""}$prefix" // Checks if message has prefix
        return if(valid && context.message.content.startsWith(requiredPrefix, ignoreCase)) {
            context.message.content.substring(requiredPrefix.length).split("\\s+")

        } else {
            emptyList()
        }
    }
}
