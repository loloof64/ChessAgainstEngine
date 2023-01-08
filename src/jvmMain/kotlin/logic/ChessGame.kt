package logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.wolfraam.chessgame.ChessGame
import io.github.wolfraam.chessgame.board.Square
import io.github.wolfraam.chessgame.move.Move

const val defaultPosition = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
const val emptyPosition = "k/8/8/8/8/8/8/K w - - 0 1"

object ChessGameManager {
    private var gameLogic by mutableStateOf(ChessGame(emptyPosition))

    fun currentPosition(): String = gameLogic.fen

    fun resetGame(startPosition: String) {
        startPosition.testIfIsLegalChessFen()
        gameLogic = ChessGame(startPosition)
    }

    fun playMove(
        startFile: Int, startRank: Int,
        endFile: Int, endRank: Int,
    ) {
        val startSquare = Square.values()[8*startFile + startRank]
        val endSquare = Square.values()[8*endFile + endRank]
        val move = Move(startSquare, endSquare)

        if (gameLogic.isLegalMove(move)) {
            gameLogic.playMove(move)
        }
    }

}