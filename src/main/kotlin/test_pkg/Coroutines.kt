package test_pkg

import java.util.concurrent.LinkedBlockingDeque
import kotlin.coroutines.CoroutineContext

class StandardCoroutine(context: CoroutineContext) : AbstractCoroutine<Unit>(context)

class BlockCoroutine<T>(context: CoroutineContext, private val queue: LinkedBlockingDeque<()->Unit>) : AbstractCoroutine<T>(context) {
    fun joinBlocking(): T {
        while (isActive) {
            queue.take()()
        }
        return (state.get() as CoroutineState.Complete<T>).let {
            it.value ?: throw it.exception!!
        }
    }
}