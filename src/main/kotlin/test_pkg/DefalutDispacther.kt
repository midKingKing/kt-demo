package test_pkg

import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.atomic.AtomicInteger

object Dispatchers {
    private val threadGroup: ThreadGroup = ThreadGroup("default-dispatcher")
    private val threadIndex: AtomicInteger = AtomicInteger(0)

    private val threadPool = Executors.newFixedThreadPool(10) {
        Thread(threadGroup, it, "${threadGroup.name}-worker-${threadIndex.getAndIncrement()}")
            .apply {
                isDaemon = true
            }
    }

    val Default by lazy {
        DispatcherContext(object : Dispatcher {
            override fun dispatch(block: () -> Unit) {
                threadPool.submit(block)
            }
        })
    }

    val blockQueue: LinkedBlockingDeque<() -> Unit> = LinkedBlockingDeque()

    val BlockingDispatcher by lazy {
        object : Dispatcher {
            override fun dispatch(block: () -> Unit) {
                blockQueue.offer(block)
            }
        }
    }
}