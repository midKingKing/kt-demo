package corotinue

import test_pkg.delay
import java.util.concurrent.TimeUnit
import kotlin.coroutines.*

fun main() {
    createCoroutine()
}


fun createCoroutine() {
    val coroutine = suspend {
        println("In Coroutine.")
        5
    }.createCoroutine(object : Continuation<Int> {
        override val context: CoroutineContext = EmptyCoroutineContext

        override fun resumeWith(result: Result<Int>) {
            println("Coroutine End: $result")
        }
    })

    coroutine.resume(Unit)
}

fun <T, R> launchCoroutine(receiver: R, block: suspend R.() -> T) {
    block.startCoroutine(receiver, object : Continuation<T> {
        override val context: CoroutineContext = EmptyCoroutineContext

        override fun resumeWith(result: Result<T>) {
            println("Coroutine End: $result")
        }
    })
}

fun <T> launchCoroutine(block: suspend () -> T) {
    block.startCoroutine(object : Continuation<T> {
        override val context: CoroutineContext = EmptyCoroutineContext

        override fun resumeWith(result: Result<T>) {
            println("Coroutine End: $result")
        }
    })
}

class ProducerScope<T> {
    suspend fun produce(value: T) {}
}

@RestrictsSuspension
class RestrictProducerScope<T> {
    suspend fun produce(value: T) {}
}

fun callLaunchCoroutine() {
    launchCoroutine(ProducerScope<Int>()) {
        produce(1)
        delay(1, TimeUnit.SECONDS)
        kotlinx.coroutines.delay(1000)
    }
}

fun callRestrictLaunchCoroutine() {
    launchCoroutine(RestrictProducerScope<Int>()) {
        produce(1)
//        delay(1, TimeUnit.SECONDS) //bad case
//        kotlinx.coroutines.delay(1000) //bad case
    }
}