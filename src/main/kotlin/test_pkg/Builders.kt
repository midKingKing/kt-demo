package test_pkg

import test_pkg.Dispatchers.BlockingDispatcher
import test_pkg.Dispatchers.Default
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine

private var coroutineIdx = AtomicInteger(0)

fun launch(context: CoroutineContext = EmptyCoroutineContext, block: suspend () -> Unit): Job {
    val completion = StandardCoroutine(newCoroutineContext(context))
    block.startCoroutine(completion)
    return completion
}

fun <T> async(context: CoroutineContext = EmptyCoroutineContext, block: suspend () -> T): Deferred<T> {
    val completion = DeferredCoroutine<T>(newCoroutineContext(context))
    block.startCoroutine(completion)
    return completion
}

fun <T> runBlocking(context: CoroutineContext = EmptyCoroutineContext, block: suspend () -> T): T {
    val newContext = context + DispatcherContext(BlockingDispatcher)
    val completion = BlockCoroutine<T>(newContext, Dispatchers.blockQueue)
    block.startCoroutine(completion)
    return completion.joinBlocking()
}

fun newCoroutineContext(context: CoroutineContext): CoroutineContext {
    val combined = context + CoroutineName("@coroutine#${coroutineIdx.incrementAndGet()}")
    return if (combined !== Default || context[ContinuationInterceptor] == null) {
        combined + Default
    } else combined
}