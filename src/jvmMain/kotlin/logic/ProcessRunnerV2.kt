package logic

import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream

class ProcessRunnerV2 {
    private var process: Process? = null
    private var inputStream: OutputStream? = null
    private var outputStream: InputStream? = null
    private var errorStream: InputStream? = null
    private var outputJob: Job? = null
    private var errorJob: Job? = null
    suspend fun start(path: String) {
        process = withContext(Dispatchers.IO) {
            Runtime.getRuntime().exec(path)
        }
        inputStream = process!!.outputStream
        outputStream = process!!.inputStream
        errorStream = process!!.errorStream

        launchReaders()
    }

    suspend fun stop() {
        outputJob?.cancel()
        errorJob?.cancel()
        delay(50)
        process?.destroy()
        delay(50)
        withContext(Dispatchers.IO) {
            inputStream?.close()
            outputStream?.close()
            errorStream?.close()
        }
        println("Process stopped.")
    }

    suspend fun sendCommand(command: String) {
        withContext(Dispatchers.IO) {
            inputStream?.write(command.toByteArray())
            inputStream?.flush()
        }
    }

    private suspend fun readOutput() {
        if (outputStream == null) return
        val reader = BufferedReader(InputStreamReader(outputStream!!))
        withContext(Dispatchers.IO) {
            while (isActive) {
                val line =
                    reader.readLine()
                        ?: break
                println(line)
            }
        }
    }

    private suspend fun readError() {
        if (errorStream == null) return
        val reader = BufferedReader(InputStreamReader(errorStream!!))
        withContext(Dispatchers.IO) {
            while (isActive) {
                val line =
                    reader.readLine()
                        ?: break
                println(line)
            }
        }
    }

    private suspend fun launchReaders() = coroutineScope {
        outputJob = launch {
            readOutput()
        }
        errorJob = launch {
            readError()
        }
    }
}