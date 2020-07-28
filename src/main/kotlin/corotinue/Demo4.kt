package corotinue

import kotlinx.coroutines.asCoroutineDispatcher
import test_pkg.log
import java.util.concurrent.Executors
import kotlin.coroutines.*

interface AsyncScope

fun async(block: suspend AsyncScope.() -> Unit) {
    val completion = AsyncCoroutine(Executors.newSingleThreadExecutor().asCoroutineDispatcher())
    block.startCoroutine(completion, completion)
}

class AsyncCoroutine(override val context: CoroutineContext = EmptyCoroutineContext): Continuation<Unit>, AsyncScope {
    override fun resumeWith(result: Result<Unit>) {
        result.getOrThrow()
    }
}

suspend fun <T> AsyncScope.await(block:() -> T) = suspendCoroutine<T> {
    it.resume(block())
}

suspend fun main() {
    //async await js style
    async {
        await {
            log("in resume")
        }
    }
}