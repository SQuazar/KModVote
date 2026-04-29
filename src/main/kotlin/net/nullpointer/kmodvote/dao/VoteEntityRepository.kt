package net.nullpointer.kmodvote.dao

import net.nullpointer.kmodvote.entity.VoteEntity
import net.nullpointer.kmodvote.entity.VoteStatus

interface VoteEntityRepository {
    suspend fun findAll(): List<VoteEntity>

    suspend fun findAllByState(state: VoteStatus): List<VoteEntity>

    suspend fun findAllFinished(requiredVotes: Int): List<VoteEntity>

    suspend fun findAllExpired(ttl: Int): List<VoteEntity>

    suspend fun findByMessageId(messageId: Long): VoteEntity?

    suspend fun save(entity: VoteEntity)

    suspend fun delete(entity: VoteEntity)
}