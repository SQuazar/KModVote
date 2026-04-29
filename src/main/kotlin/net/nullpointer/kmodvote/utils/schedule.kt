package net.nullpointer.kmodvote.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlin.time.Duration

fun CoroutineScope.schedule(interval: Duration, task: suspend () -> Unit) =
    launch {
        tickerFlow(interval).collect { task() }
    }

private fun tickerFlow(period: Duration) = flow {
    while (true) {
        emit(Unit)
        delay(period)
    }
}