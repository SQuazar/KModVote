package net.nullpointer.kmodvote.handler

import net.dv8tion.jda.api.events.GenericEvent

fun interface GenericEventHandler<T : GenericEvent> {
    suspend fun handle(event: T)
}