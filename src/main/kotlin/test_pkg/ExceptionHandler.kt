// Copyright 2020 ALO7 Inc. All rights reserved.

package test_pkg

import kotlin.coroutines.CoroutineContext

/**
 * @author Jimi.feng
 */
interface ExceptionHandler : CoroutineContext.Element {
    companion object Key : CoroutineContext.Key<ExceptionHandler>

    fun handleException(context: CoroutineContext, exception: Throwable)
}

inline fun ExceptionHandler(crossinline handler: (CoroutineContext, Throwable) -> Unit) = object : ExceptionHandler {
    override val key: CoroutineContext.Key<*> = ExceptionHandler

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        handler(context, exception)
    }
}