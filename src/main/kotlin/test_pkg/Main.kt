package test_pkg

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
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
        log(1)
        val result = hello()
        log(2, result)
        delay(1, TimeUnit.SECONDS)
        log(3)
    }

    log(job.isActive)
    job.cancel()
    log(job.isActive)
    job.join()
}

//suspend fun main() = runBlocking {
//    log(1)
//    delay(5, TimeUnit.SECONDS)
//    log(2)
//}

//suspend fun main() {
//    log(1)
//    val deferred = async {
//        log(2)
//        delay(3, TimeUnit.SECONDS)
//        log(3)
//        "Hello"
//        throw ArithmeticException("Div 0")
//    }
//    log(4)
//    try {
//        val result = deferred.await()
//        log(5, result)
//    } catch (e: Exception) {
//        log(6, e)
//    }
//}

suspend fun hello() = suspendCoroutine<Int> {
    thread(isDaemon = true) {
        Thread.sleep(5000)
        it.resume(100)
    }
}