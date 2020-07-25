//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.channels.onReceiveOrNull
//import kotlinx.coroutines.asCoroutineDispatcher
//import kotlinx.coroutines.channels.*
//import kotlinx.coroutines.selects.select
//import kotlinx.coroutines.withContext
//import java.io.File
//import java.util.concurrent.Executors
//
//val KotlinFileFilter = { file: File -> file.isDirectory || file.name.endsWith(".kt") }
//
//data class FileLines(val file: File, val lines: Int) {
//    override fun toString(): String {
//        return "${file.name}: $lines"
//    }
//}
//
//suspend fun main() {
//    val result = lineCounter(File("."))
//    log(result)
//}
//
//suspend fun lineCounter(root: File): HashMap<File, Int> {
//    return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1).asCoroutineDispatcher()
//        .use {
//            withContext(it) {
//                val fileChannel = walkFile(root)
//
//                val fileLinesChannels = List(5) {
//                    fileLineCounter(fileChannel)
//                }
//
//                resultAggregator(fileLinesChannels)
//            }
//        }
//}
//
//fun CoroutineScope.walkFile(root: File): ReceiveChannel<File> {
//    return produce(capacity = Channel.BUFFERED) {
//        fileWalker(root)
//    }
//}
//
//suspend fun SendChannel<File>.fileWalker(file: File) {
//    if (file.isDirectory) {
//        file.listFiles()?.filter(KotlinFileFilter)?.forEach { fileWalker(it) }
//    } else {
//        send(file)
//    }
//}
//
//fun CoroutineScope.fileLineCounter(input: ReceiveChannel<File>): ReceiveChannel<FileLines> {
//    return produce(capacity = Channel.BUFFERED) {
//        for (file in input) {
//            file.useLines {
//                send(FileLines(file, it.count()))
//            }
//        }
//    }
//}
//
//suspend fun CoroutineScope.resultAggregator(channels: List<ReceiveChannel<FileLines>>): HashMap<File, Int> {
//    val map = HashMap<File, Int>()
//    channels.aggregate { filteredChannels ->
//        select<FileLines?> {
//            filteredChannels.forEach {
//                val onReceiveOrNull = it.onReceiveOrNull()
//                onReceiveOrNull { fileLines ->
//                    log("Received $fileLines")
//                    fileLines
//                }
//            }
//        }?.let {
//            map[it.file] = it.lines
//        }
//    }
//
//    return map
//}
//
//suspend fun List<ReceiveChannel<FileLines>>.aggregate(block: suspend (List<ReceiveChannel<FileLines>>) -> Unit) {
//    block(this)
//    filter { !it.isClosedForReceive }.takeIf { it.isNotEmpty() }?.aggregate(block)
//}