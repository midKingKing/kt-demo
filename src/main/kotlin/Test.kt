import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.selects.select
import java.io.File
import java.util.concurrent.Executors
import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun log(msg: Any) {
    println("${Thread.currentThread().name} $msg")
}

val KtFileFilter = { file: File -> file.isDirectory || file.name.endsWith(".kt") }

data class FileLines(val file: File, val lines: Int) {
    override fun toString(): String {
        return "${file.name}: $lines"
    }
}

suspend fun main() {
    val result = lineCounter(File("."))
    log(result)
}

suspend fun lineCounter(rootFile: File): HashMap<File, Int> {
    return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1).asCoroutineDispatcher().let {
        withContext(it) {
            val walkFile: ReceiveChannel<FileLines> = walkFile(rootFile)
            agg(walkFile)
        }
    }
}

fun CoroutineScope.walkFile(file: File): ReceiveChannel<FileLines> {
    return produce(capacity = Channel.BUFFERED) {
        fileWalker(file)
    }
}

suspend fun SendChannel<FileLines>.fileWalker(file: File) {
    if (file.isDirectory) {
        file.listFiles()?.filter(KtFileFilter)?.forEach { fileWalker(it) }
    } else {
        send(FileLines(file, file.useLines { it.count() }))
    }
}

@UseExperimental(InternalCoroutinesApi::class)
suspend fun CoroutineScope.agg(walkFile: ReceiveChannel<FileLines>): HashMap<File, Int> {
    val map = HashMap<File, Int>()
    walkFile.agg {
        select<FileLines?> {
            walkFile.onReceiveOrClosed {
                it.valueOrNull
            }
        }?.let {
            map[it.file] = it.lines
        }
    }
    return map
}

suspend fun ReceiveChannel<FileLines>.agg(block: suspend ReceiveChannel<FileLines>.() -> Unit) {
    block(this)
    if (!isClosedForReceive) {
        agg(block)
    }
}