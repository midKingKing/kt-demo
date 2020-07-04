package corotinue

import kotlin.coroutines.*


fun log(msg: String, value: Any) {
    println("[${Thread.currentThread().name}] msg $value")
}

sealed class Status2 {
    class Created(val continuation: Continuation<Unit>) : Status2()
    class Yield<T>(val value: T, val continuation: Continuation<Unit>) : Status2()
    class Resumed<T>(val continuation: Continuation<Unit>) : Status2()
    object Dead : Status2()
}

class Coroutine<P, R>(
    override val context: CoroutineContext = EmptyCoroutineContext,
    private val block: suspend Coroutine<P, R>.(P) -> R
) : Continuation<R> {
    var isActive: Boolean

    private var status2: Status2

    var parameter: P? = null

    init {
        isActive = false
        val coro = suspend{ block(parameter!!) }.createCoroutine(this)
        status2 = Status2.Created(coro)
    }

    override fun resumeWith(result: Result<R>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    suspend fun yield(value: R): P = suspendCoroutine {
    }

    companion object {
        fun <P, R> create(block: suspend Coroutine<P, R>.(P) -> R): Coroutine<P, R> {
            return Coroutine(block = block)
        }
    }

    suspend fun resume(p: P) = suspendCoroutine<R> {
        when(status2) {

        }
    }
}

suspend fun main() {
    val producer = Coroutine.create<Unit, Int> {
        for (i in 0..3) {
            log("send", i)
            yield(i)
        }
        200
    }

    val consumer = Coroutine.create<Int, Unit> { param: Int ->
        log("start", param)
        for (i in 0..3) {
            val value = yield(Unit)
            log("receive", value)
        }
    }

    while (producer.isActive && consumer.isActive) {
        val result = producer.resume(Unit)
        consumer.resume(result)
    }
}