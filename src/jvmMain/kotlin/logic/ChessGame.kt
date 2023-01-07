package logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.wolfraam.chessgame.ChessGame

const val defaultPosition = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
const val emptyPosition = "k/8/8/8/8/8/8/K w - - 0 1"

object ChessGameManager {
    private var gameLogic by mutableStateOf(ChessGame(emptyPosition))

    fun currentPosition(): String = gameLogic.fen

    fun resetGame(startPosition: String) {
        startPosition.testIfIsLegalChessFen()
        gameLogic = ChessGame(startPosition)
    }

}