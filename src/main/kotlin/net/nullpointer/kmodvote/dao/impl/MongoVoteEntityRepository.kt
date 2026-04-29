package net.nullpointer.kmodvote.dao.impl

import com.mongodb.client.model.Filters.*
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.Clock
import net.nullpointer.kmodvote.dao.VoteEntityRepository
import net.nullpointer.kmodvote.entity.VoteStatus
import net.nullpointer.kmodvote.entity.VoteEntity
import org.bson.Document
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

class MongoVoteEntityRepository(
    private val database: MongoDatabase
) : VoteEntityRepository {
    companion object {
        private const val COLLECTION_NAME = "DATA_votes"
    }

    override suspend fun findAll(): List<VoteEntity> =
        database.getCollection<VoteEntity>(COLLECTION_NAME)
            .find<VoteEntity>()
            .toList()

    override suspend fun findAllByState(state: VoteStatus): List<VoteEntity> =
        database.getCollection<VoteEntity>(COLLECTION_NAME)
            .find<VoteEntity>(eq(VoteEntity::status.name, state))
            .toList()

    override suspend fun findAllFinished(requiredVotes: Int): List<VoteEntity> {
        val timeFilter = lte(VoteEntity::createdAt.name, Clock.System.now() - 24.hours)
        val votesFilter = expr(
            Document(
                $$"$gte", listOf(
                    Document(
                        $$"$add", listOf(
                            Document($$"$size", $$"$banReactions"),
                            Document($$"$size", $$"$unbanReactions")
                        )
                    ),
                    requiredVotes
                )
            )
        )
        val filter = and(
            eq(VoteEntity::status.name, VoteStatus.REQUIRE_VOTES),
            timeFilter,
            votesFilter
        )

        return database.getCollection<VoteEntity>(COLLECTION_NAME)
            .find<VoteEntity>(filter)
            .toList()
    }

    override suspend fun findAllExpired(ttl: Int): List<VoteEntity> {
        val timeFilter = lte(VoteEntity::createdAt.name, Clock.System.now() - ttl.days)
        return database.getCollection<VoteEntity>(COLLECTION_NAME)
            .find<VoteEntity>(and(timeFilter, not(eq(VoteEntity::status.name, VoteStatus.FINISHED))))
            .toList()
    }

    override suspend fun findByMessageId(messageId: Long): VoteEntity? =
        database.getCollection<VoteEntity>(COLLECTION_NAME)
            .find<VoteEntity>(eq(VoteEntity::messageId.name, messageId))
            .firstOrNull()

    override suspend fun save(entity: VoteEntity) {
        database.getCollection<VoteEntity>(COLLECTION_NAME)
            .replaceOne(
                eq(VoteEntity::messageId.name, entity.messageId),
                entity,
                ReplaceOptions().upsert(true)
            )
    }

    override suspend fun delete(entity: VoteEntity) {
        database.getCollection<VoteEntity>(COLLECTION_NAME)
            .deleteOne(eq(VoteEntity::messageId.name, entity.messageId))
    }
}