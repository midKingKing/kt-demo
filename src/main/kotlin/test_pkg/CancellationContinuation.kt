package test_pkg

import java.lang.IllegalStateException
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.Continuation
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.intrinsics.intercepted
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn

class CancellationContinuation<T>(private val delegate: Continuation<T>) : Continuation<T> by delegate {
    private val state = AtomicReference<CancelState>(CancelState.InComplete)

    private val cancelHandlers = CopyOnWriteArrayList<OnCancel>()

    val isCompleted: Boolean
        get() = state.get() is CancelState.Complete<*>

    val isActive: Boolean
        get() = state.get() is CancelState.InComplete

    override fun resumeWith(result: Result<T>) {
        state.updateAndGet { prev ->
            when (prev) {
                CancelState.InComplete -> {
                    delegate.resumeWith(result)
                    CancelState.Complete(result.getOrNull(), result.exceptionOrNull())
                }
                is CancelState.Complete<*> -> throw IllegalStateException("")
                CancelState.Cancelled -> {
                    CancellationException("Cancelled").let {
                        delegate.resumeWith(Result.failure(it))
                        CancelState.Complete(null, it)
                    }
                }
            }
        }
    }

    fun invokeOnCancel(onCancel: OnCancel) {
        cancelHandlers += onCancel
    }

    fun cancel() {
        if (isActive) return
        val parent = delegate.context[Job] ?: return
        parent.cancel()
    }

    fun getResult(): Any? {
        installCancelHandler()
        return when (val currentState = state.get()) {
            CancelState.InComplete -> COROUTINE_SUSPENDED
            is CancelState.Complete<*> -> {
                currentState.exception?.let { throw it } ?: currentState.value
            }
            CancelState.Cancelled -> throw CancellationException("continuation is cancel")
        }
    }

    private fun installCancelHandler() {
        if (!isActive) return
        val parent = delegate.context[Job] ?: return
        parent.invokeOnCancel {
            doCancel()
        }
    }

    private fun doCancel() {
        state.updateAndGet { prev ->
            when (prev) {
                CancelState.InComplete -> {
                    CancelState.Cancelled
                }
                is CancelState.Complete<*>,
                CancelState.Cancelled -> {
                    prev
                }
            }
        }

        cancelHandlers.forEach(OnCancel::invoke)
        cancelHandlers.clear()
    }
}

suspend inline fun <T> suspendCancellableCoroutine(
    crossinline block: (CancellationContinuation<T>) -> Unit
): T = suspendCoroutineUninterceptedOrReturn { c: Continuation<T> ->
    val cancel = CancellationContinuation(c.intercepted())
    block(cancel)
    cancel.getResult()
}