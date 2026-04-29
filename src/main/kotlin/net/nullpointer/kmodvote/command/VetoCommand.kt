package net.nullpointer.kmodvote.command

import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import net.nullpointer.kmodvote.entity.VoteResult
import net.nullpointer.kmodvote.service.VoteService

class VetoCommand(
    jda: JDA,
    name: String,
    private val voteService: VoteService,
    private val resultProvider: (MessageContextInteractionEvent) -> VoteResult
) : GenericCommand<MessageContextInteractionEvent>(
    jda,
    name,
    MessageContextInteractionEvent::class.java,
) {
    override suspend fun onCommand(event: MessageContextInteractionEvent): Boolean {
        event.deferReply(true).await()
        voteService.findVote(event.target.idLong)?.let { vote ->
            voteService.closeVote(vote, resultProvider(event))
            event.hook.editOriginal(
                "Вы восопльзовались правом вето!"
            ).await()
        } ?: event.hook.editOriginal("Голосование не найдено или завершено")

        return true
    }
}