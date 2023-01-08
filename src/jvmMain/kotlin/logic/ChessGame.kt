package logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import components.PendingPromotion
import components.PromotionType
import components.emptyCell
import io.github.wolfraam.chessgame.ChessGame
import io.github.wolfraam.chessgame.board.PieceType
import io.github.wolfraam.chessgame.board.Square
import io.github.wolfraam.chessgame.move.Move

const val defaultPosition = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
const val emptyPosition = "k/8/8/8/8/8/8/K w - - 0 1"

object ChessGameManager {
    private var _gameLogic by mutableStateOf(ChessGame(emptyPosition))
    private var _pendingPromotion by mutableStateOf(PendingPromotion.None)
    private var _pendingPromotionStartSquare by mutableStateOf<Square?>(null)
    private var _pendingPromotionEndSquare by mutableStateOf<Square?>(null)

    fun getPieces(): List<List<Char>> {
        val positionFen = _gameLogic.fen
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

    fun getPendingPromotion(): PendingPromotion = _pendingPromotion

    fun isWhiteTurn(): Boolean {
        val positionFen = _gameLogic.fen
        return positionFen.split(" ")[1] == "w"
    }


    fun resetGame(startPosition: String) {
        startPosition.testIfIsLegalChessFen()
        _gameLogic = ChessGame(startPosition)
        _pendingPromotion = PendingPromotion.None
        _pendingPromotionStartSquare = null
        _pendingPromotionEndSquare = null
    }

    fun playMove(
        startFile: Int, startRank: Int,
        endFile: Int, endRank: Int,
    ) {
        val startSquare = Square.values()[8 * startFile + startRank]
        val endSquare = Square.values()[8 * endFile + endRank]
        val move = Move(startSquare, endSquare)

        if (_gameLogic.isLegalMove(move)) {
            _gameLogic.playMove(move)
        } else {
            val isLegalPromotionMove = _gameLogic.isLegalMove(Move(startSquare, endSquare, PieceType.QUEEN))

            if (isLegalPromotionMove) {
                _pendingPromotion = if (isWhiteTurn()) PendingPromotion.White else PendingPromotion.Black
                _pendingPromotionStartSquare = startSquare
                _pendingPromotionEndSquare = endSquare
            }
        }
    }

    fun cancelPromotion() {
        _pendingPromotion = PendingPromotion.None
        _pendingPromotionStartSquare = null
        _pendingPromotionEndSquare = null
    }

    fun commitPromotion(pieceType: PromotionType) {
        if (_pendingPromotion == PendingPromotion.None) return
        val promotionPiece = when (pieceType) {
            PromotionType.Queen -> PieceType.QUEEN
            PromotionType.Rook -> PieceType.ROOK
            PromotionType.Bishop -> PieceType.BISHOP
            PromotionType.Knight -> PieceType.KNIGHT
        }
        val move = Move(_pendingPromotionStartSquare, _pendingPromotionEndSquare, promotionPiece)
        if (_gameLogic.isLegalMove(move)) {
            _gameLogic.playMove(move)
            _pendingPromotion = PendingPromotion.None
            _pendingPromotionStartSquare = null
            _pendingPromotionEndSquare = null
        }
    }

}