package net.nullpointer.kmodvote.utils

object Env {
    val CHANNEL_ID: Long = System.getenv("CHANNEL")?.toLongOrNull() ?: error("CHANNEL is required")
    val GUILD_ID: Long = System.getenv("GUILD_ID")?.toLongOrNull() ?: error("GUILD_ID is required")
    val MODERATOR_ROLE: Long = System.getenv("MODERATOR_ROLE")?.toLongOrNull() ?: error("MODERATOR_ROLE is required")

    val TTL: Int = System.getenv("VOTING_TTL")?.toIntOrNull() ?: 90
    val REQUIRED_VOTES: Int = System.getenv("REQUIRED_VOTES")?.toIntOrNull() ?: 1
    val REQUIRED_PERCENTAGE: Double = System.getenv("REQUIRED_PERCENTAGE")?.toDoubleOrNull() ?: 0.6
}