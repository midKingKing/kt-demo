import kotlin.concurrent.thread
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun main() {
    val map = HashMap<String, Continuation<*>>()
    for(i in 0..2) {
        val p = yield(i, map)
        println("[$p, ${Thread.currentThread().name}]")
    }
    println("wow")
}

suspend fun yield(value: Any, mutableMap: MutableMap<String, Continuation<*>>) = suspendCoroutine<Any> {
    println(value.toString())
    mutableMap[value.toString()] = it
    thread {
        Thread.sleep(3000)
        it.resumeWith(Result.success(value))
    }
}