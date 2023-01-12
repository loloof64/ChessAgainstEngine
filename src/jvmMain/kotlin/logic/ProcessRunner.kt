package logic

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.BufferedSink
import okio.buffer
import okio.sink
import okio.source

class ProcessRunner(val path: String) {
    private var process: Process? = null
    private var outputFlow: Flow<String>? = null
    private var errorFlow: Flow<String>? = null
    private var inputSink: BufferedSink? = null

    init {
        process = ProcessBuilder(path).start()
        outputFlow = process?.inputStream?.source()?.buffer().use { buffer ->
            flow {
                while (true) {
                    val line = buffer?.readUtf8Line()
                    if (line != null) emit(line)
                }
            }
        }
        errorFlow = process?.errorStream?.source()?.buffer().use { buffer ->
            flow {
                while (true) {
                    val line = buffer?.readUtf8Line()
                    if (line != null) emit(line)
                }
            }
        }
        inputSink = process?.outputStream?.sink()?.buffer()
    }

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun start() {
        val job = GlobalScope.launch {
            withContext(Dispatchers.IO) {
                outputFlow?.collect {
                    println(it)
                }
                errorFlow?.collect {
                    println(it)
                }
                while (true) {
                }
            }
        }
        job.join()
        withContext(Dispatchers.IO) {
            process?.inputStream?.close()
            process?.outputStream?.close()
            process?.errorStream?.close()
        }
    }


    fun sendCommand(command: String) {
        inputSink?.writeUtf8(command)
        inputSink?.flush()
    }
}