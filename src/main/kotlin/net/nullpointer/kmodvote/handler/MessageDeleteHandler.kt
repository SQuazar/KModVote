package net.nullpointer.kmodvote.handler

import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.nullpointer.kmodvote.service.VoteService

class MessageDeleteHandler(
    private val voteService: VoteService,
) : GenericEventHandler<MessageDeleteEvent> {
    override suspend fun handle(event: MessageDeleteEvent) {
        voteService.findVote(event.messageIdLong)?.let { vote -> voteService.deleteVote(vote) }
    }
}