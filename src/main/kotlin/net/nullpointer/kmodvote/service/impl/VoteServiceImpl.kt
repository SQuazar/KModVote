package net.nullpointer.kmodvote.service.impl

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.send
import kotlinx.coroutines.future.await
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.emoji.EmojiUnion
import net.dv8tion.jda.api.utils.FileUpload
import net.nullpointer.kmodvote.dao.VoteEntityRepository
import net.nullpointer.kmodvote.entity.VoteEntity
import net.nullpointer.kmodvote.entity.VoteResult
import net.nullpointer.kmodvote.entity.VoteStatus
import net.nullpointer.kmodvote.service.VoteService
import net.nullpointer.kmodvote.utils.Emojis
import net.nullpointer.kmodvote.utils.Env
import net.nullpointer.kmodvote.utils.addPollReactions
import net.nullpointer.kmodvote.utils.asEmbed

class VoteServiceImpl(
    private val jda: JDA,
    private val voteRepository: VoteEntityRepository
) : VoteService {
    override suspend fun createVote(message: Message) {
        val vote = VoteEntity(
            messageId = message.idLong,
            authorId = message.author.idLong,
            description = message.contentDisplay
        )
        val res = message.channel.send {
            embeds += vote.asEmbed(jda)
            files += message.attachments.map {
                FileUpload.fromData(it.proxy.download().await(), it.proxy.fileName)
            }
        }.await()
        res.addPollReactions().await()
        res.createThreadChannel("Обсуждение")
            .setAutoArchiveDuration(ThreadChannel.AutoArchiveDuration.TIME_1_WEEK)
            .await()

        val save = vote.copy(messageId = res.idLong)
        voteRepository.save(save)

        message.delete().await()
    }

    override suspend fun processReaction(
        message: Message,
        user: User,
        emoji: EmojiUnion
    ) {
        val vote = voteRepository.findByMessageId(message.idLong) ?: return
        message.removeReaction(emoji, user).await()

        val changed = when (emoji.asCustom().idLong) {
            Emojis.BAN_EMOJI.idLong -> {
                if (vote.banReactions.add(user.idLong)) {
                    vote.unbanReactions.remove(user.idLong)
                    true
                } else false
            }

            Emojis.UNBAN_EMOJI.idLong -> {
                if (vote.unbanReactions.add(user.idLong)) {
                    vote.banReactions.remove(user.idLong)
                    true
                } else false
            }

            else -> return
        }

        if (changed) {
            message.editMessageEmbeds(vote.asEmbed(message.jda)).await()
            voteRepository.save(vote)
        } else user.openPrivateChannel().await()
            .sendMessage("${message.jumpUrl} Вы уже голосовали за этот вариант!").await()
    }

    override suspend fun updateVoteMessageEmbed(vote: VoteEntity) {
        val channel = jda.getTextChannelById(Env.CHANNEL_ID) ?: return
        val message = channel.retrieveMessageById(vote.messageId).await()
        message.editMessageEmbeds(vote.asEmbed(jda)).await()
    }

    override suspend fun expireVote(vote: VoteEntity) {
        val channel = jda.getTextChannelById(Env.CHANNEL_ID) ?: return
        val message = channel.retrieveMessageById(vote.messageId).await()

        vote.status = VoteStatus.EXPIRED

        message.editMessageEmbeds(vote.asEmbed(jda)).await()
        message.clearReactions().await()
        message.startedThread?.let { thread ->
            if (!thread.isArchived) {
                thread.manager
                    .setArchived(true)
                    .setLocked(true)
                    .await()
            }
        }

        voteRepository.save(vote)
    }

    override suspend fun closeVote(
        vote: VoteEntity,
        result: VoteResult
    ) {
        val channel = jda.getTextChannelById(Env.CHANNEL_ID) ?: return
        val message = channel.retrieveMessageById(vote.messageId).await()
        vote.status = VoteStatus.FINISHED
        vote.result = result

        message.editMessageEmbeds(vote.asEmbed(message.jda)).await()
        message.clearReactions().await()

        message.startedThread?.let { thread ->
            if (!thread.isArchived) {
                thread.manager
                    .setArchived(true)
                    .setLocked(true)
                    .await()
            }
        }

        val res = when (result) {
            is VoteResult.Ban -> "**Нарушает**"
            is VoteResult.Unban -> "**Не нарушает**"
        }

        jda.retrieveUserById(vote.authorId).await()
            .openPrivateChannel().await()
            .sendMessage("${message.jumpUrl} По вашей ситуации был вынесен вердикт: $res").await()

        voteRepository.save(vote)
    }

    override suspend fun findVote(messageId: Long) =
        voteRepository.findByMessageId(messageId)

    override suspend fun findAllVotes() =
        voteRepository.findAll()

    override suspend fun findUnfinishedVotes(): List<VoteEntity> =
        voteRepository.findAllByState(VoteStatus.REQUIRE_VOTES)

    override suspend fun findFinishedVotes(): List<VoteEntity> =
        voteRepository.findAllFinished(Env.REQUIRED_VOTES)

    override suspend fun findExpiredVotes(): List<VoteEntity> = voteRepository.findAllExpired(Env.TTL)

    override suspend fun saveVote(vote: VoteEntity) = voteRepository.save(vote)

    override suspend fun deleteVote(vote: VoteEntity) = voteRepository.delete(vote)
}