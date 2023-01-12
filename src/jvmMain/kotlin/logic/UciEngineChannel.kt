package logic

import kotlinx.coroutines.*
import java.io.IOException
import java.io.PrintWriter
import java.util.*

object UciEngineChannel {
    private var process: Process? = null
    private var processOut: Scanner? = null
    private var processIn: PrintWriter? = null

    private var outputReaderJob: Job? = null

    fun isProcessStarted(): Boolean = process != null

    suspend fun stopProcess() {
        outputReaderJob?.cancel()
        delay(100)
        processOut = null
        processIn = null
        process = null
        outputReaderJob = null
        println("Uci engine process stopped.")
    }

    suspend fun tryStartingEngine(): Boolean {
        if (PreferencesManager.getEnginePath().isEmpty()) return false
        if (process != null) return false

        try {
            return withContext(Dispatchers.IO) {
                process = ProcessBuilder(PreferencesManager.getEnginePath()).start()
                processOut = Scanner(process!!.inputStream)
                processIn = PrintWriter(process!!.outputStream)
                outputReaderJob = launch {
                    while (isActive) {
                        try {
                            val currentLine = processOut!!.nextLine()
                            println(currentLine)
                        } catch (ex: IllegalStateException) {
                            println(ex)
                            return@launch
                        } catch (ex: NoSuchElementException) {
                            println(ex)
                            return@launch
                        }
                    }
                }

                outputReaderJob!!.join()

                return@withContext true
            }

        } catch (ex: IOException) {
            println(ex)
            return false
        }

    }

    fun sendCommandToEngine(command: String) {
        println("process is $process")
        if (process != null) {
            processIn!!.write("$command\n")
            processIn!!.flush()
        }
    }
}