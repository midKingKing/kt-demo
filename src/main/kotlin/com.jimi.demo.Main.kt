import kotlin.concurrent.thread
import kotlin.coroutines.*

fun main() {
    val map = HashMap<String, Continuation<*>>()
    suspend {
        val a = test(map)
        println(a)
        123
    }.startCoroutine(object : Continuation<Any> {
        override val context: CoroutineContext = EmptyCoroutineContext

        override fun resumeWith(result: Result<Any>) {
            println(result.getOrNull())
        }
    })
    val continuation = map[""] as Continuation<Any>
    continuation.resumeWith(Result.success(111))
}

suspend fun test(map: MutableMap<String, Continuation<*>>) = suspendCoroutine<Int> {
    println("end resume test1")
    map[""] = it
}

//suspend fun corotinue.main() {
//    val map = HashMap<String, Continuation<*>>()
//    val a = test(map)
//    println(a)
//}
//
//suspend fun test(map: MutableMap<String, Continuation<*>>) = suspendCoroutine<Int> {
//    println("end resume test1")
//    thread {
//        Thread.sleep(5000)
//        map[""] = it
//        it.resume(101)
//    }
//}