package net.nullpointer.kmodvote.handler

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.nullpointer.kmodvote.service.VoteService
import net.nullpointer.kmodvote.utils.Env

class MessageReceiveHandler(
    private val voteService: VoteService
) : GenericEventHandler<MessageReceivedEvent> {
    override suspend fun handle(event: MessageReceivedEvent) {
        if (event.channel.idLong != Env.CHANNEL_ID || event.guild.idLong != Env.GUILD_ID) return
        if (event.author.isBot) return
        if (event.message.contentRaw.startsWith(">")) return
        voteService.createVote(event.message)
    }
}