package logic

import kotlinx.coroutines.*
import java.io.*

@OptIn(DelicateCoroutinesApi::class)
class ProcessWrapper(
    command: String,
    private val outputCallback: (String) -> Unit,
    private val errorCallback: (String) -> Unit
) {
    private var process: Process? = null
    private var processInput: BufferedWriter? = null
    private var processOutput: BufferedReader? = null
    private var processError: BufferedReader? = null

    init {
        process = Runtime.getRuntime().exec(command)
        processInput = BufferedWriter(OutputStreamWriter(process!!.outputStream))
        processOutput = BufferedReader(InputStreamReader(process!!.inputStream))
        processError = BufferedReader(InputStreamReader(process!!.errorStream))
        GlobalScope.launch {
            processOutput?.useLines { lines ->
                lines.forEach {
                    /////////////////////////////
                    println("@@@ $it")
                    /////////////////////////////
                    outputCallback(it)
                }
            }
        }
        GlobalScope.launch {
            processError?.useLines { lines ->
                lines.forEach {
                    errorCallback(it)
                }
            }
        }
    }

    suspend fun sendCommand(command: String) {
        withContext(Dispatchers.IO) {
            processInput?.write("$command\n")
            processInput?.flush()
        }
    }

    fun stopProcess() {
        processInput?.close()
        processOutput?.close()
        processError?.close()
        process?.destroy()
    }
}
