package net.nullpointer.kmodvote.service

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.emoji.EmojiUnion
import net.nullpointer.kmodvote.entity.VoteEntity
import net.nullpointer.kmodvote.entity.VoteResult

interface VoteService {
    suspend fun createVote(message: Message)

    suspend fun processReaction(message: Message, user: User, emoji: EmojiUnion)

    suspend fun updateVoteMessageEmbed(vote: VoteEntity)

    suspend fun expireVote(vote: VoteEntity)

    suspend fun closeVote(vote: VoteEntity, result: VoteResult)

    suspend fun findVote(messageId: Long): VoteEntity?

    suspend fun findAllVotes(): List<VoteEntity>

    suspend fun findUnfinishedVotes(): List<VoteEntity>

    suspend fun findFinishedVotes(): List<VoteEntity>

    suspend fun findExpiredVotes(): List<VoteEntity>

    suspend fun saveVote(vote: VoteEntity)

    suspend fun deleteVote(vote: VoteEntity)
}