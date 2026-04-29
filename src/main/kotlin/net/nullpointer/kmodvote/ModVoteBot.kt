package net.nullpointer.kmodvote

import com.mongodb.MongoClientSettings
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.jdabuilder.intents
import dev.minn.jda.ktx.jdabuilder.light
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import net.nullpointer.kmodvote.dao.impl.MongoVoteEntityRepository
import net.nullpointer.kmodvote.handler.MessageDeleteHandler
import net.nullpointer.kmodvote.handler.MessageReactionHandler
import net.nullpointer.kmodvote.handler.MessageReceiveHandler
import net.nullpointer.kmodvote.registry.CommandRegistry
import net.nullpointer.kmodvote.schedule.VoteCheckScheduler
import net.nullpointer.kmodvote.service.VoteService
import net.nullpointer.kmodvote.service.impl.VoteServiceImpl
import net.nullpointer.kmodvote.utils.Emojis
import net.nullpointer.kmodvote.utils.uploadApplicationEmojis
import org.bson.codecs.configuration.CodecRegistries

class ModVoteBot(
    private val BOT_TOKEN: String =
        System.getenv("BOT_TOKEN") ?: error("BOT_TOKEN is required")
) {
    private lateinit var mongo: MongoDatabase
    private lateinit var voteService: VoteService

    suspend fun start() {
        setupMongo()

        val jda = light(BOT_TOKEN, true) {
            intents += listOf(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGE_REACTIONS)
        }

        val repository = MongoVoteEntityRepository(mongo)
        voteService = VoteServiceImpl(jda, repository)

        jda.uploadApplicationEmojis()
        jda.retrieveApplicationEmojis().await().let { emojis ->
            Emojis.BAN_EMOJI = emojis.first { it.name == "ban" }
            Emojis.UNBAN_EMOJI = emojis.first { it.name == "unban" }
        }

        jda.listener<MessageDeleteEvent> { MessageDeleteHandler(voteService).handle(it) }
        jda.listener<MessageReceivedEvent> { MessageReceiveHandler(voteService).handle(it) }
        jda.listener<MessageReactionAddEvent> { MessageReactionHandler(voteService).handle(it) }

        CommandRegistry(jda, voteService).registerCommands()

        VoteCheckScheduler(CoroutineScope(Dispatchers.Default), voteService).start()
    }

    private fun setupMongo() {
        val codec = CodecRegistries.fromRegistries(
            MongoClientSettings.getDefaultCodecRegistry()
        )

        mongo = MongoClient.create(System.getenv("MONGO_URI") ?: error("MONGO_URI is required"))
            .getDatabase(System.getenv("MONGO_DB") ?: error("MONGO_DB is required"))
            .withCodecRegistry(codec)
    }
}