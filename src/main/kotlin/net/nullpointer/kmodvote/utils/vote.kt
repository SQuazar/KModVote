package net.nullpointer.kmodvote.utils

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.Embed
import kotlinx.datetime.*
import kotlinx.datetime.format.char
import net.dv8tion.jda.api.JDA
import net.nullpointer.kmodvote.entity.VoteEntity
import net.nullpointer.kmodvote.entity.VoteResult
import net.nullpointer.kmodvote.entity.VoteStatus
import kotlin.time.Duration.Companion.hours

suspend fun VoteEntity.asEmbed(jda: JDA) = Embed {
    title = when (status) {
        VoteStatus.OPEN -> "Новое голосование!"
        VoteStatus.EXPIRED -> "Голосование истекло"
        VoteStatus.REQUIRE_VOTES -> "Недостаточно голосов"
        VoteStatus.FINISHED -> "Голосование завершено!"
    }
    author {
        val member = jda.retrieveUserById(authorId).await()
        name = member.effectiveName
        iconUrl = member.effectiveAvatarUrl
    }
    color = when (status) {
        VoteStatus.OPEN -> Colors.VOTE_COLOR
        VoteStatus.REQUIRE_VOTES -> Colors.VOTE_COLOR
        VoteStatus.EXPIRED -> Colors.EXPIRED_COLOR
        VoteStatus.FINISHED -> when (result) {
            is VoteResult.Ban -> Colors.BAN_COLOR
            is VoteResult.Unban -> Colors.UNBAN_COLOR
            null -> Colors.VOTE_COLOR
        }
    }.rgb

    field {
        name = "Описание ситуации"
        value = this@asEmbed.description.ifBlank { "Пусто" }
        inline = false
    }
    when (status) {
        VoteStatus.OPEN, VoteStatus.EXPIRED ->
            field("Количество голосов", getTotalVotes().toString(), false)

        VoteStatus.REQUIRE_VOTES ->
            field("Количество голосов", "${getTotalVotes()} / ${Env.REQUIRED_VOTES}", false)

        VoteStatus.FINISHED -> {
            field {
                name = "Вердикт"
                value = when (result) {
                    is VoteResult.Ban -> "Нарушает"
                    is VoteResult.Unban -> "Не нарушает"
                    else -> "Неизвестно"
                }
                inline = false
            }
            field(
                "Результаты голосования",
                "${banReactions.size} - нарушает, ${unbanReactions.size} - не нарушает",
                false
            )
            result?.judge?.let { id ->
                field("Решение принял", jda.retrieveUserById(id).await().asMention, false)
            }
        }
    }

    val endDate =
        if (result != null) Clock.System.now().toLocalDateTime(TimeZone.of("Europe/Moscow"))
        else createdAt.plus(24.hours).toLocalDateTime(TimeZone.of("Europe/Moscow"))

    field {
        name = "Дата окончания голосования"
        value = endDate
            .format(LocalDateTime.Format {
                date(LocalDate.Format {
                    dayOfMonth(); char('.')
                    monthNumber(); char('.')
                    year()
                })
                char(' ')
                hour(); char(':'); minute()
            })
        inline = false
    }
}