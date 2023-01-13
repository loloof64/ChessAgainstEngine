package logic

object UciEngineChannel {
    private var process: ProcessWrapper? = null

    fun isProcessStarted(): Boolean = process != null

    fun tryStartingEngineProcess(): Boolean {
        if (process != null) return false

        return if (PreferencesManager.getEnginePath().isNotEmpty()) {
            process = ProcessWrapper(
                command = PreferencesManager.getEnginePath(),
                outputCallback = {
                    println(it)
                },
                errorCallback = {
                    println(it)
                }
            )
            true
        } else false
    }

    suspend fun getBestMoveForPosition(position: String) {
        process?.sendCommand("position fen $position")
        process?.sendCommand("go movetime ${PreferencesManager.getEngineThinkingTime()}")
    }


    fun stopProcess() {
        process?.stopProcess()
        process = null
    }
}