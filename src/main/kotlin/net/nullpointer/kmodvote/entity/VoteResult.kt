package net.nullpointer.kmodvote.entity

import kotlinx.serialization.Serializable

@Serializable
sealed class VoteResult {
    abstract val veto: Boolean
    abstract val judge: Long?

    @Serializable
    class Ban(override val judge: Long? = null, override val veto: Boolean = judge != null) : VoteResult()

    @Serializable
    class Unban(override val judge: Long? = null, override val veto: Boolean = judge != null) : VoteResult()
}