package app.silberkorn.core

import java.util.concurrent.Callable
import java.util.concurrent.Executors

object Parallel {
    private val threads = Runtime.getRuntime().availableProcessors().coerceIn(1, 8)
    private val pool = Executors.newFixedThreadPool(threads) { r ->
        Thread(r, "develop").apply { isDaemon = true }
    }

    /** Splits 0 until [count] into chunks and blocks until all are done. */
    fun rows(count: Int, body: (from: Int, to: Int) -> Unit) {
        if (count <= 0) return
        val chunks = minOf(threads * 4, count)
        val tasks = (0 until chunks).map { i ->
            Callable { body(count * i / chunks, count * (i + 1) / chunks) }
        }
        pool.invokeAll(tasks).forEach { it.get() }
    }
}
