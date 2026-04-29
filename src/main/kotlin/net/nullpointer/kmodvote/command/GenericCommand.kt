package net.nullpointer.kmodvote.command

import dev.minn.jda.ktx.events.onCommand
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

abstract class GenericCommand<T : GenericCommandInteractionEvent>(
    jda: JDA,
    name: String,
    eventClass: Class<T>
) {
    private val cooldowns = mutableMapOf<User, Long>()

    init {
        jda.onCommand(name) { event ->
            if (eventClass.isInstance(event)) {
                cleanupCooldowns()

                val user = event.user

                if (user in cooldowns) {
                    val until = getCooldownRemaining(user)
                    event.reply("Подождите ещё ${until.inWholeSeconds} секунд перед новой жалобой")
                        .setEphemeral(true).queue()
                    return@onCommand
                }

                if (onCommand(eventClass.cast(event)) && !bypassCooldown(event))
                    cooldown(user)
            }
        }
    }

    abstract suspend fun onCommand(event: T): Boolean

    private fun cleanupCooldowns() {
        val now = System.currentTimeMillis()
        cooldowns.entries.removeIf { (_, until) -> until < now }
    }

    private fun cooldown(user: User) {
        cooldowns[user] = System.currentTimeMillis() + getCooldownDuration().inWholeMilliseconds
    }

    private fun getCooldownRemaining(user: User): Duration {
        val until = cooldowns[user] ?: return Duration.ZERO
        return ((until - System.currentTimeMillis()).coerceAtLeast(0)).milliseconds
    }

    protected open fun getCooldownDuration(): Duration = Duration.ZERO

    protected open fun bypassCooldown(context: GenericCommandInteractionEvent): Boolean =
        context.member?.hasPermission(Permission.ADMINISTRATOR) ?: false
}