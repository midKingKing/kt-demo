package test_pkg

import java.lang.IllegalStateException
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

abstract class AbstractCoroutine<T>(context: CoroutineContext) : Job, Continuation<T> {
    protected val state = AtomicReference<CoroutineState>()
    override val context: CoroutineContext = context + this

    init {
        state.set(CoroutineState.InComplete())
    }

    override val isActive: Boolean
        get() = state.get() is CoroutineState.InComplete

    override val isCompleted: Boolean
        get() = state.get() is CoroutineState.Complete<*>

    override fun invokeOnCompletion(onComplete: OnComplete): Disposable {
        return doOnCompleted {
            onComplete()
        }
    }

    override fun remove(disposable: Disposable) {
        state.updateAndGet { prev ->
            when (prev) {
                is CoroutineState.InComplete -> {
                    CoroutineState.InComplete().from(prev).without(disposable)
                }
                is CoroutineState.Complete<*> -> prev
                is CoroutineState.Cancelling -> {
                    CoroutineState.Cancelling().from(prev).without(disposable)
                }
            }
        }
    }

    override suspend fun join() {
        when (state.get()) {
            is CoroutineState.Cancelling,
            is CoroutineState.InComplete -> {
                return joinSuspend()
            }
            is CoroutineState.Complete<*> -> {
                return
            }
        }
    }

    private suspend fun joinSuspend() = suspendCoroutine<Unit> { continuation ->
        doOnCompleted { result ->
            continuation.resume(Unit)
        }
    }

    protected fun doOnCompleted(block: (Result<T>) -> Unit): Disposable {
        val disposable = CompletionHandlerDisposable(this, block)
        val newState = state.updateAndGet { prev ->
            when (prev) {
                is CoroutineState.InComplete -> {
                    CoroutineState.InComplete().from(prev).with(disposable)
                }
                is CoroutineState.Complete<*> -> {
                    prev
                }
                is CoroutineState.Cancelling -> {
                    CoroutineState.Cancelling().from(prev).with(disposable)
                }
            }
        }

        (newState as? CoroutineState.Complete<T>)?.let {
            block(
                when {
                    it.value != null -> Result.success(it.value)
                    it.exception != null -> Result.failure(it.exception)
                    else -> throw IllegalStateException()
                }
            )
        }

        return disposable
    }

    override fun resumeWith(result: Result<T>) {
        val newState = state.updateAndGet { prev ->
            when (prev) {
                is CoroutineState.Cancelling,
                is CoroutineState.InComplete -> {
                    CoroutineState.Complete(result.getOrNull(), result.exceptionOrNull()).from(prev)
                }
                is CoroutineState.Complete<*> -> {
                    throw IllegalStateException("")
                }
            }
        }

        (newState as CoroutineState.Complete<T>).exception?.let(::tryHandlerException)
        newState.notifyCompletion(result)
        newState.clear()
    }

    private fun tryHandlerException(e: Throwable) {
        when(e) {
            is CancellationException -> Unit
            else -> handlerException(e)
        }
    }

    protected open fun handlerException(e: Throwable) {

    }

    override fun invokeOnCancel(onCancel: OnCancel): Disposable {
        val disposable = CancellationHandlerDisposable(this, onCancel)
        val newState = state.updateAndGet {
            prev ->
            when(prev) {
                is CoroutineState.InComplete -> {
                    CoroutineState.InComplete().from(prev).with(disposable)
                }
                is CoroutineState.Cancelling,
                is CoroutineState.Complete<*> -> {
                    prev
                }
            }
        }
        (newState as? CoroutineState.Cancelling)?.let {
            onCancel()
        }
        return disposable
    }

    override fun cancel() {
        val newState = state.updateAndGet {
            prev ->
            when(prev) {
                is CoroutineState.InComplete -> {
                    CoroutineState.Cancelling().from(prev)
                }
                is CoroutineState.Cancelling,
                is CoroutineState.Complete<*> -> prev
            }
        }
        if (newState is CoroutineState.Cancelling) {
            newState.notifyCancellation()
        }
    }

    override fun toString(): String {
        return "${context[CoroutineName]?.name}"
    }
}