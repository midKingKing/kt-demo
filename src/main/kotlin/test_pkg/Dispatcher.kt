package test_pkg

import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.Continuation
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext

interface Dispatcher {
    fun dispatch(block: () -> Unit)
}

class DispatcherContext(private val dispatcher: Dispatcher) : AbstractCoroutineContextElement(ContinuationInterceptor),
    ContinuationInterceptor {
    override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> =
        DispatcherContinuation(continuation, dispatcher)
}

class DispatcherContinuation<T>(private val delegate: Continuation<T>, private val dispatcher: Dispatcher) :
    Continuation<T> {
    override val context: CoroutineContext
        get() = delegate.context

    override fun resumeWith(result: Result<T>) {
        dispatcher.dispatch {
            delegate.resumeWith(result)
        }
    }
}