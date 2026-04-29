package net.nullpointer.kmodvote.handler

import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.nullpointer.kmodvote.service.VoteService
import net.nullpointer.kmodvote.utils.Env

class MessageReactionHandler(
    private val voteService: VoteService
) : GenericEventHandler<MessageReactionAddEvent> {
    override suspend fun handle(event: MessageReactionAddEvent) {
        val member = event.retrieveMember().await()
        if (member.user.isBot) return
        if (member.roles.none { role -> role.idLong == Env.MODERATOR_ROLE }) return

        voteService.processReaction(event.retrieveMessage().await(), member.user, event.emoji)
    }
}