package logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import components.*
import io.github.wolfraam.chessgame.ChessGame
import io.github.wolfraam.chessgame.board.PieceType
import io.github.wolfraam.chessgame.board.Side
import io.github.wolfraam.chessgame.board.Square
import io.github.wolfraam.chessgame.move.Move
import io.github.wolfraam.chessgame.notation.NotationType
import io.github.wolfraam.chessgame.result.ChessGameResult
import io.github.wolfraam.chessgame.result.ChessGameResultType
import io.github.wolfraam.chessgame.result.DrawType

const val defaultPosition = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
const val emptyPosition = "k/8/8/8/8/8/8/K w - - 0 1"

object ChessGameManager {
    private var _gameInProgress by mutableStateOf(false)
    private var _gameLogic by mutableStateOf(ChessGame(emptyPosition))
    private var _pendingPromotion by mutableStateOf(PendingPromotion.None)
    private var _pendingPromotionStartSquare by mutableStateOf<Square?>(null)
    private var _pendingPromotionEndSquare by mutableStateOf<Square?>(null)
    private var _lastMoveArrow by mutableStateOf<LastMoveArrow?>(null)
    private var _historyElements by mutableStateOf<MutableList<ChessHistoryItem>>(mutableListOf())

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

    fun getHistoryElements(): List<ChessHistoryItem> = _historyElements

    fun getLastMoveArrow(): LastMoveArrow? = _lastMoveArrow

    fun isGameInProgress(): Boolean = _gameInProgress

    fun getPendingPromotion(): PendingPromotion = _pendingPromotion

    fun getPendingPromotionStartSquare(): Square? = _pendingPromotionStartSquare

    fun getPendingPromotionEndSquare(): Square? = _pendingPromotionEndSquare

    fun isWhiteTurn(): Boolean {
        val positionFen = _gameLogic.fen
        return positionFen.split(" ")[1] == "w"
    }


    fun resetGame(startPosition: String) {
        startPosition.testIfIsLegalChessFen()
        _gameLogic = ChessGame(startPosition)
        _historyElements = mutableListOf()
        _pendingPromotion = PendingPromotion.None
        _pendingPromotionStartSquare = null
        _pendingPromotionEndSquare = null
        _lastMoveArrow = null
        _gameInProgress = true
    }

    fun playMove(
        startFile: Int, startRank: Int,
        endFile: Int, endRank: Int,
        onCheckmate: (Boolean) -> Unit,
        onStalemate: () -> Unit,
        onThreeFoldsRepetition: () -> Unit,
        onInsufficientMaterial: () -> Unit,
        onFiftyMovesRuleDraw: () -> Unit
    ) {
        val startSquare = Square.values()[8 * startFile + startRank]
        val endSquare = Square.values()[8 * endFile + endRank]
        val move = Move(startSquare, endSquare)

        if (_gameLogic.isLegalMove(move)) {
            addMoveToHistory(move = move)
            _gameLogic.playMove(move)
            _lastMoveArrow = LastMoveArrow(
                startFile = startFile,
                startRank = startRank,
                endFile = endFile,
                endRank = endRank
            )
            handleGameEndingStatus(
                onCheckmate = onCheckmate,
                onStalemate = onStalemate,
                onThreeFoldsRepetition = onThreeFoldsRepetition,
                onInsufficientMaterial = onInsufficientMaterial,
                onFiftyMovesRuleDraw = onFiftyMovesRuleDraw,
            )
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

    fun commitPromotion(
        pieceType: PromotionType,
        onCheckmate: (Boolean) -> Unit,
        onStalemate: () -> Unit,
        onThreeFoldsRepetition: () -> Unit,
        onInsufficientMaterial: () -> Unit,
        onFiftyMovesRuleDraw: () -> Unit
    ) {
        if (_pendingPromotion == PendingPromotion.None) return
        val promotionPiece = when (pieceType) {
            PromotionType.Queen -> PieceType.QUEEN
            PromotionType.Rook -> PieceType.ROOK
            PromotionType.Bishop -> PieceType.BISHOP
            PromotionType.Knight -> PieceType.KNIGHT
        }
        val move = Move(_pendingPromotionStartSquare, _pendingPromotionEndSquare, promotionPiece)
        if (_gameLogic.isLegalMove(move)) {
            addMoveToHistory(move = move)
            _gameLogic.playMove(move)
            _pendingPromotion = PendingPromotion.None
            _pendingPromotionStartSquare = null
            _pendingPromotionEndSquare = null

            _lastMoveArrow = LastMoveArrow(
                startFile = move.from.x,
                startRank = move.from.y,
                endFile = move.to.x,
                endRank = move.to.y
            )


            handleGameEndingStatus(
                onCheckmate = onCheckmate,
                onStalemate = onStalemate,
                onThreeFoldsRepetition = onThreeFoldsRepetition,
                onInsufficientMaterial = onInsufficientMaterial,
                onFiftyMovesRuleDraw = onFiftyMovesRuleDraw,
            )
        }
    }

    private fun addMoveToHistory(move: Move) {
        val moveSan = _gameLogic.getNotation(NotationType.SAN, move)
        _historyElements.add(
            ChessHistoryItem.MoveItem(
                san = moveSan, positionFen = "", isWhiteMove = _gameLogic.sideToMove == Side.WHITE
            )
        )
    }

    private fun handleGameEndingStatus(
        onCheckmate: (Boolean) -> Unit,
        onStalemate: () -> Unit,
        onThreeFoldsRepetition: () -> Unit,
        onInsufficientMaterial: () -> Unit,
        onFiftyMovesRuleDraw: () -> Unit,
    ) {
        val gameResult: ChessGameResult? = _gameLogic.gameResult
        when (gameResult?.chessGameResultType) {
            ChessGameResultType.WHITE_WINS -> {
                _gameInProgress = false
                onCheckmate(true)
            }

            ChessGameResultType.BLACK_WINS -> {
                _gameInProgress = false
                onCheckmate(false)
            }

            ChessGameResultType.DRAW -> {
                when (gameResult.drawType) {
                    DrawType.STALE_MATE -> {
                        _gameInProgress = false
                        onStalemate()
                    }

                    DrawType.THREEFOLD_REPETITION -> {
                        _gameInProgress = false
                        onThreeFoldsRepetition()
                    }

                    DrawType.INSUFFICIENT_MATERIAL -> {
                        _gameInProgress = false
                        onInsufficientMaterial()
                    }

                    DrawType.FIFTY_MOVE_RULE -> {
                        _gameInProgress = false
                        onFiftyMovesRuleDraw()
                    }

                    else -> throw RuntimeException("Not in a draw state.")
                }
            }

            else -> {}
        }
    }
}