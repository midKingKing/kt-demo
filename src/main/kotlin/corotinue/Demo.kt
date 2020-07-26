package corotinue

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import log
import java.util.concurrent.Executors

suspend fun main() {
    //corotinue.flows()
    //corotinue.exception()

    val ints = flow {
        emit(1)
    }
    ints.collect { log(it) }
}

suspend fun flows() {
    val intFlow = flow {
        log("read to 1")
        emit(1)
        delay(1000)
        log("read to 2")
        emit(2)
        log("read to 3")
        emit(3)
    }

    intFlow.flowOn(Dispatchers.IO).collect {
        log(it)
    }

}

suspend fun flowDispatch() {
    val intFlow = flow {
        log("read to 1")
        emit(1)
        delay(1000)
        log("read to 2")
        emit(2)
        log("read to 3")
        emit(3)
    }

    val dispatcher = Executors.newSingleThreadExecutor { Thread(it, "MyThread!!").also { it.isDaemon = true } }
        .asCoroutineDispatcher()
    GlobalScope.launch(dispatcher) {
        intFlow.flowOn(Dispatchers.IO)
            .collect { log(it) }
    }.join()
}

suspend fun exception() {
    flow {
        emit(1)
        throw ArithmeticException("Div 0")
    }.catch { t: Throwable ->
        log("caught error: $t")
    }.onCompletion { t: Throwable? ->
        log("finally.")
    }.flowOn(Dispatchers.IO)
        .collect { log(it) }

//    flow { // bad!!!
//        try {
//            emit(1)
//            throw ArithmeticException("Div 0")
//        } catch (t: Throwable){
//            log("caught error: $t")
//        } finally {
//            log("finally.")
//        }
//    }
}

fun fromCollections() {
    listOf(1, 2, 3, 4).asFlow()
    setOf(1, 2, 3, 4).asFlow()
    flowOf(1, 2, 3, 4)
}

suspend fun fromChannel() {
    val channel = Channel<Int>()
    channel.consumeAsFlow()

//    flow { // bad!!
//        emit(1)
//        withContext(Dispatchers.IO){
//            emit(2)
//        }
//    }

    channelFlow {
        // good
        send(1)
        withContext(Dispatchers.IO) {
            send(2)
        }
    }
}

suspend fun backPressure() {
    flow {
        emit(1)
        delay(50)
        emit(2)
    }.collectLatest { value ->
        println("Collecting $value")
        delay(100) // Emulate work
        println("$value collected")
    }
}