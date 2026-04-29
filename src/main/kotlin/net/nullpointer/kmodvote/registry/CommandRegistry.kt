package net.nullpointer.kmodvote.registry

import dev.minn.jda.ktx.interactions.commands.slash
import dev.minn.jda.ktx.interactions.commands.updateCommands
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.commands.Command
import net.nullpointer.kmodvote.command.FindUnfinishedCommand
import net.nullpointer.kmodvote.command.VetoCommand
import net.nullpointer.kmodvote.entity.VoteResult
import net.nullpointer.kmodvote.service.VoteService
import net.nullpointer.kmodvote.utils.contextCommand

class CommandRegistry(
    private val jda: JDA,
    private val voteService: VoteService
) {
    fun registerCommands() {
        jda.updateCommands {
            contextCommand(Command.Type.MESSAGE, "Нарушает (право вето)") {
                setContexts(InteractionContextType.GUILD)
                VetoCommand(jda, name, voteService) { event -> VoteResult.Ban(event.user.idLong) }
            }
            contextCommand(Command.Type.MESSAGE, "Не нарушает (право вето)") {
                setContexts(InteractionContextType.GUILD)
                VetoCommand(jda, name, voteService) { event -> VoteResult.Unban(event.user.idLong) }
            }
            slash("unfinished", "Список незавершенных голосований") {
                setContexts(InteractionContextType.GUILD)
                FindUnfinishedCommand(jda, "unfinished", voteService)
            }
        }.queue()
    }
}