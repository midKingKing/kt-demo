package test_pkg

import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

val dateFormat = SimpleDateFormat("HH:mm:ss:SSS")

val now = {
    dateFormat.format(Date(System.currentTimeMillis()))
}

fun log(vararg msg: Any?) = println("${now()} [${Thread.currentThread().name}] ${msg.joinToString(" ")}")

suspend fun main() {
    val job = launch(Dispatchers.Default) {
        log("1")
        val result = hello()
        log(2, result)
    }

    log(job.isActive)
    job.join()
}

suspend fun hello() = suspendCoroutine<Int> {
    thread(isDaemon = true) {
        Thread.sleep(10000)
        it.resume(100)
    }
}