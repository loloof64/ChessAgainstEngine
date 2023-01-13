package logic

typealias StringCallback = (String) -> Unit
typealias FloatCallback = (Float) -> Unit

object UciEngineChannel {
    private var process: ProcessWrapper? = null

    private var bestMoveCallback: StringCallback? = null
    private var scoreCallback: FloatCallback? = null

    fun setBestMoveCallback(callback: StringCallback?) {
        bestMoveCallback = callback
    }

    fun setScoreCallback(callback: FloatCallback?) {
        scoreCallback = callback
    }

    fun isProcessStarted(): Boolean = process != null

    suspend fun stopCurrentComputation() {
        process?.sendCommand("stop\n")
    }

    @Suppress("RegExpRedundantEscape")
    private fun handleEngineOutput(engineOutput: String) {
        if (engineOutput.startsWith("bestmove")) {
            bestMoveCallback?.invoke(engineOutput.split(" ")[1])
        } else if (engineOutput.contains("score cp")) {
            val scorePartRegex = """score cp (\-?\d+)""".toRegex()
            val scoreMatch = scorePartRegex.find(engineOutput)
            val scoreString = scoreMatch?.groups?.get(1)?.value
            if (scoreString != null) {
                scoreCallback?.invoke(scoreString.toFloat() / 100f)
            }
        }
    }

    fun tryStartingEngineProcess(): Boolean {
        if (process != null) return false

        return if (PreferencesManager.getEnginePath().isNotEmpty()) {
            process = ProcessWrapper(
                command = PreferencesManager.getEnginePath(),
                outputCallback = ::handleEngineOutput,
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

    suspend fun getNewPositionEvaluation(position: String) {
        process?.sendCommand("position fen $position")
        process?.sendCommand("go movetime 10")
    }


    fun stopProcess() {
        process?.stopProcess()
        process = null
    }
}