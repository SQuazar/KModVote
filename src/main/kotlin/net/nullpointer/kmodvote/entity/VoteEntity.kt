package net.nullpointer.kmodvote.entity

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.hours

@Serializable
data class VoteEntity(
    val messageId: Long,
    val authorId: Long,
    val description: String,
    val banReactions: HashSet<Long> = hashSetOf(),
    val unbanReactions: HashSet<Long> = hashSetOf(),
    val createdAt: Instant = Clock.System.now(),
    var status: VoteStatus = VoteStatus.OPEN,
    var result: VoteResult? = null
) {
    fun needMoreVotes(required: Int): Boolean =
        Clock.System.now() - createdAt >= 24.hours
                && (banReactions.size + unbanReactions.size) < required

    fun getTotalVotes() = unbanReactions.size + banReactions.size
}