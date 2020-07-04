package corotinue

import java.lang.IllegalStateException
import kotlin.coroutines.*

fun main() {
    val nums = generator { start: Int ->
        for (i in 0..5) {
            yield(start + i)
        }
    }

    val seq = nums(10)

    for (j in seq) {
        println(j)
    }
}

sealed class Status {
    class NotReady(val continuation: Continuation<Unit>) : Status()
    class Ready<T>(val value: T, val continuation: Continuation<Unit>) : Status()
    object Done : Status()
}

class Iter<T>(initialValue: T, block: suspend Iter<T>.(T) -> Unit) : Iterator<T> {
    private var status: Status

    init {
        val continuation = suspend { block(initialValue) }.createCoroutine(object : Continuation<Unit> {
            override val context: CoroutineContext = EmptyCoroutineContext

            override fun resumeWith(result: Result<Unit>) {
                status = Status.Done
                println("all done")
            }
        })
        status = Status.NotReady(continuation)
    }

    override fun hasNext(): Boolean {
        when (status) {
            is Status.NotReady -> {
                val current = status as Status.NotReady
                current.continuation.resume(Unit)
            }
        }
        return status is Status.Ready<*>
    }

    override fun next(): T {
        return when (status) {
            is Status.Ready<*> -> {
                val current = status as Status.Ready<T>
                status = Status.NotReady(current.continuation)
                current.value
            }
            else -> throw IllegalStateException()
        }
    }

    suspend fun yield(value: T) = suspendCoroutine<Unit> {
        status = Status.Ready(value, it)
    }
}

fun <T> generator(block: suspend Iter<T>.(T) -> Unit): (T) -> Iter<T> {
    return {
        Iter(it, block)
    }
}