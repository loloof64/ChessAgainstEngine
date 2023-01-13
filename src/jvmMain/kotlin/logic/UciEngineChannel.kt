package logic

import kotlinx.coroutines.Dispatchers
import net.andreinc.neatchess.client.UCI
import net.andreinc.neatchess.client.UCIResponse
import net.andreinc.neatchess.client.model.BestMove

object UciEngineChannel {
    private var uci: UCI? = null

    fun isProcessStarted(): Boolean = uci != null

    fun tryStartingEngineProcess(): Boolean {
        if (uci != null) return false

        return if (PreferencesManager.getEnginePath().isNotEmpty()) {
            uci = UCI()
            uci?.start(PreferencesManager.getEnginePath())
            true
        } else false
    }

    fun getBestMoveForPosition(position: String) : UCIResponse<BestMove>? {
        return with(Dispatchers.Default) {
            uci?.positionFen(position)
            uci?.bestMove(PreferencesManager.getEngineThinkingTime().toLong())
        }
    }


    fun stopProcess() {
        uci?.close()
        uci = null
    }
}