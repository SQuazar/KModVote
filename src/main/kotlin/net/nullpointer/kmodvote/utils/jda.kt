package net.nullpointer.kmodvote.utils

import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Icon
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction

suspend fun JDA.uploadApplicationEmojis() {
    val exists = retrieveApplicationEmojis().await()
        .map { it.name }

    val actions = Resources.images("images")
        .filter { !exists.contains(it.first) }
        .map { (name, input) ->
            createApplicationEmoji(name, Icon.from(input))
        }

    if (actions.isNotEmpty()) RestAction.allOf(actions).await()
}

fun Message.addPollReactions() =
    RestAction.allOf(
        addReaction(Emojis.BAN_EMOJI),
        addReaction(Emojis.UNBAN_EMOJI)
    )

inline fun CommandListUpdateAction.contextCommand(
    type: Command.Type,
    name: String,
    builder: CommandData.() -> Unit = {}
) = addCommands(Commands.context(type, name).apply { builder() })