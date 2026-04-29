package net.nullpointer.kmodvote.schedule

import kotlinx.coroutines.CoroutineScope
import net.nullpointer.kmodvote.entity.VoteResult
import net.nullpointer.kmodvote.entity.VoteStatus
import net.nullpointer.kmodvote.service.VoteService
import net.nullpointer.kmodvote.utils.Env
import net.nullpointer.kmodvote.utils.schedule
import kotlin.math.max
import kotlin.time.Duration.Companion.minutes

class VoteCheckScheduler(
    private val scope: CoroutineScope,
    private val voteService: VoteService
) {
    fun start() {
        scope.schedule(5.minutes) {
            voteService.findFinishedVotes().forEach { vote ->
                val banPercentage = vote.banReactions.size.toDouble() / max(vote.getTotalVotes(), 1)
                val result = if (banPercentage >= Env.REQUIRED_PERCENTAGE) VoteResult.Ban() else VoteResult.Unban()
                voteService.closeVote(vote, result)
            }

            voteService.findAllVotes()
                .filter { vote -> vote.needMoreVotes(Env.REQUIRED_VOTES) && vote.status == VoteStatus.OPEN }
                .forEach { vote ->
                    vote.status = VoteStatus.REQUIRE_VOTES
                    voteService.updateVoteMessageEmbed(vote)
                    voteService.saveVote(vote)
                }

            voteService.findExpiredVotes().forEach { vote -> voteService.expireVote(vote) }
        }
    }
}