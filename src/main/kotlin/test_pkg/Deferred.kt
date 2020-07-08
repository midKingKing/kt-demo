package test_pkg

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.suspendCoroutine

interface Deferred<T>: Job {
    suspend fun await(): T
}

class DeferredCoroutine<T>(context: CoroutineContext) : Deferred<T>, AbstractCoroutine<T>(context) {
    override suspend fun await(): T {
        return when(state.get()) {
            is CoroutineState.Cancelling,
            is CoroutineState.InComplete -> awaitSuspend()
            is CoroutineState.Complete<*> -> (state as CoroutineState.Complete<T>).value ?: throw state.exception!!
        }
    }

    private suspend fun awaitSuspend() = suspendCoroutine<T> { continuation ->
        doOnCompleted {
            continuation.resumeWith(it)
        }
    }
}