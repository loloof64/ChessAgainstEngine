package logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import components.emptyCell
import io.github.wolfraam.chessgame.ChessGame
import io.github.wolfraam.chessgame.board.Square
import io.github.wolfraam.chessgame.move.Move

const val defaultPosition = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
const val emptyPosition = "k/8/8/8/8/8/8/K w - - 0 1"

object ChessGameManager {
    private var gameLogic by mutableStateOf(ChessGame(emptyPosition))

    fun getPieces(): List<List<Char>> {
        val positionFen = gameLogic.fen
        val lineParts = positionFen.split(" ")[0].split('/')

        return lineParts.map { line ->
            line.flatMap { value ->
                if (value.isDigit()) {
                    List(value.digitToInt()) { emptyCell }
                } else {
                    listOf(value)
                }
            }
        }
    }

    fun isWhiteTurn() : Boolean {
        val positionFen = gameLogic.fen
        return positionFen.split(" ")[1] == "w"
    }

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