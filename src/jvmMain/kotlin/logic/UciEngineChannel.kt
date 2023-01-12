package logic

import kotlinx.coroutines.*

object UciEngineChannel {
    private var processRunner: ProcessRunnerV2? = null

    fun isProcessStarted(): Boolean = processRunner != null

    suspend fun tryStartingEngineProcess(): Boolean {
        if (processRunner != null) return false

        return if (PreferencesManager.getEnginePath().isNotEmpty()) {
            processRunner = ProcessRunnerV2()
            processRunner?.start(PreferencesManager.getEnginePath())
            true
        } else false
    }

    suspend fun sendCommand(command: String) {
        processRunner?.sendCommand("$command\n")
    }

    suspend fun stopProcess() {
        processRunner?.stop()
        processRunner = null
    }
}