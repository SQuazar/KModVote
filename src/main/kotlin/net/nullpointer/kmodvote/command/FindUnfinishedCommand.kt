package net.nullpointer.kmodvote.command

import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.internal.utils.Helpers
import net.nullpointer.kmodvote.service.VoteService
import net.nullpointer.kmodvote.utils.Env

class FindUnfinishedCommand(
    val jda: JDA,
    val name: String,
    private val voteService: VoteService
) : GenericCommand<SlashCommandInteractionEvent>(
    jda,
    name,
    SlashCommandInteractionEvent::class.java
) {
    override suspend fun onCommand(event: SlashCommandInteractionEvent): Boolean {
        val res = voteService.findUnfinishedVotes()
        if (res.isEmpty()) event.reply("Незавершенные голосования не найдены")
            .setEphemeral(true).await()
        else event.reply(
            "Список незавершенных голосований:\n" +
                    res.joinToString("\n") { vote -> getMessageUrl(vote.messageId) })
            .await()
        return true
    }

    private fun getMessageUrl(id: Long): String {
        return Helpers.format(Message.JUMP_URL, Env.GUILD_ID, Env.CHANNEL_ID, id)
    }
}