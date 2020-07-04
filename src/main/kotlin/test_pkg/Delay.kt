// Copyright 2020 ALO7 Inc. All rights reserved.

package test_pkg

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * @author Jimi.feng
 */
private val executor = Executors.newScheduledThreadPool(1) { runnable ->
    Thread(runnable, "Delay-Scheduler").apply { isDaemon = true }
}

suspend fun delay(time: Long, timeUnit: TimeUnit) = suspendCoroutine<Unit> {
    executor.schedule({
        it.resume(Unit)
    }, time, timeUnit)
}