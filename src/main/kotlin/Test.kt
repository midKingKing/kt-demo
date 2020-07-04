import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun log(msg: Any) {
    println("${Thread.currentThread().name} $msg")
}

fun main() {
    GlobalScope.launch(Dispatchers.IO) {
        log("before hello")
        hello()
        launch {
            log("before hello1")
            hello()
            log("after hello")

        }
        log("after hello")
    }

    Thread.sleep(10000)
}

suspend fun hello() = suspendCoroutine<Unit>{
    thread {
        Thread.sleep(3000)
        log("in hello")
        it.resume(Unit)
    }
}